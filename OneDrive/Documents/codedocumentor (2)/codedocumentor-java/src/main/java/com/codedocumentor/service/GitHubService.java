package com.codedocumentor.service;

import com.codedocumentor.model.FileContent;
import com.codedocumentor.model.GitHubUser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.kohsuke.github.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Handles all GitHub OAuth and API interactions.
 * Mirrors Python's GitHub OAuth routes, clone_github_repository(), and commit_documentation().
 */
@Service
public class GitHubService {

    private static final Logger log = LoggerFactory.getLogger(GitHubService.class);

    private static final String GITHUB_AUTH_URL  = "https://github.com/login/oauth/authorize";
    private static final String GITHUB_TOKEN_URL = "https://github.com/login/oauth/access_token";

    @Value("${GITHUB_CLIENT_ID:}")
    private String clientId;

    @Value("${GITHUB_CLIENT_SECRET:}")
    private String clientSecret;

    @Value("${GITHUB_REDIRECT_URI:http://localhost:8080/auth/github/callback}")
    private String redirectUri;

    private final FileProcessingService fileProcessingService;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public GitHubService(FileProcessingService fileProcessingService) {
        this.fileProcessingService = fileProcessingService;
        this.httpClient = new OkHttpClient.Builder()
                .callTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    // -------------------------------------------------------------------------
    // OAuth helpers
    // -------------------------------------------------------------------------

    /**
     * Build the GitHub OAuth authorisation URL.
     * Mirrors Python's github_auth() route.
     */
    public String buildAuthorizationUrl(String state) {
        return GITHUB_AUTH_URL + "?client_id=" + clientId
                + "&redirect_uri=" + redirectUri
                + "&scope=repo+user:email"
                + "&state=" + state;
    }

    /**
     * Exchange the OAuth code for an access token.
     * Mirrors Python's github_callback() token exchange.
     *
     * @param code  the code returned by GitHub
     * @return the access token string
     * @throws IOException if the exchange fails
     */
    public String exchangeCodeForToken(String code) throws IOException {
        RequestBody formBody = new FormBody.Builder()
                .add("client_id",     clientId)
                .add("client_secret", clientSecret)
                .add("code",          code)
                .add("redirect_uri",  redirectUri)
                .build();

        Request request = new Request.Builder()
                .url(GITHUB_TOKEN_URL)
                .post(formBody)
                .addHeader("Accept", "application/json")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String body = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) {
                throw new IOException("Token exchange failed: " + response.code() + " " + body);
            }
            JsonNode json = objectMapper.readTree(body);
            JsonNode tokenNode = json.get("access_token");
            if (tokenNode == null) {
                throw new IOException("Access token not received: " + body);
            }
            return tokenNode.asText();
        }
    }

    /**
     * Fetch basic profile info for the authenticated user.
     * Mirrors Python's GitHub(access_token).get_user() call.
     */
    public GitHubUser fetchUserInfo(String accessToken) throws IOException {
        GitHub github = new GitHubBuilder().withOAuthToken(accessToken).build();
        GHMyself me = github.getMyself();
        return GitHubUser.builder()
                .login(me.getLogin())
                .name(me.getName())
                .email(me.getEmail())
                .avatarUrl(me.getAvatarUrl())
                .build();
    }

    // -------------------------------------------------------------------------
    // Repository listing
    // -------------------------------------------------------------------------

    /**
     * Return a list of repository metadata maps for the authenticated user.
     * Each map mirrors the Python dict returned to /api/github/repositories.
     */
    public List<Map<String, Object>> listRepositories(String accessToken) throws IOException {
        GitHub github = new GitHubBuilder().withOAuthToken(accessToken).build();
        GHMyself me = github.getMyself();

        List<Map<String, Object>> repos = new ArrayList<>();
        for (GHRepository repo : me.listRepositories().toList()) {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("id",          repo.getId());
            r.put("name",        repo.getName());
            r.put("full_name",   repo.getFullName());
            r.put("description", repo.getDescription());
            r.put("private",     repo.isPrivate());
            r.put("html_url",    repo.getHtmlUrl() != null ? repo.getHtmlUrl().toString() : "");
            r.put("clone_url",   repo.getHttpTransportUrl());
            r.put("updated_at",  repo.getUpdatedAt() != null ? repo.getUpdatedAt().toString() : "");
            r.put("language",    repo.getLanguage());
            repos.add(r);
        }
        return repos;
    }

    // -------------------------------------------------------------------------
    // Clone & analyse
    // -------------------------------------------------------------------------

    /**
     * Clone a public GitHub repository via the ZIP download API and extract code files.
     * Mirrors Python's clone_github_repository() — uses the GitHub code ZIP download
     * so no git binary is required on the server.
     *
     * @param repoUrl  full GitHub URL e.g. https://github.com/owner/repo
     * @return list of extracted {@link FileContent} objects
     * @throws IOException on network or extraction errors
     */
    public List<FileContent> cloneRepository(String repoUrl) throws IOException {
        String normalised = normaliseUrl(repoUrl);
        if (!normalised.startsWith("https://github.com/")) {
            throw new IllegalArgumentException(
                    "Invalid GitHub URL. Please use https://github.com/username/repository format");
        }

        // Strip trailing .git
        String stripped = normalised.endsWith(".git")
                ? normalised.substring(0, normalised.length() - 4)
                : normalised;

        // Derive owner/repo from URL path
        String[] parts = stripped.replace("https://github.com/", "").split("/", 2);
        if (parts.length < 2 || parts[1].isBlank()) {
            throw new IllegalArgumentException("Could not parse owner/repo from URL: " + repoUrl);
        }
        String owner = parts[0];
        String repo  = parts[1];

        // Try main, then master
        for (String branch : List.of("main", "master")) {
            String zipUrl = "https://codeload.github.com/" + owner + "/" + repo
                    + "/zip/refs/heads/" + branch;
            try {
                byte[] zipBytes = downloadBytes(zipUrl);
                List<FileContent> files = fileProcessingService.extractFilesFromZip(
                        new ByteArrayInputStream(zipBytes));

                // Strip the top-level folder prefix that GitHub adds (e.g. "repo-main/")
                String prefix = repo + "-" + branch + "/";
                List<FileContent> normalised2 = new ArrayList<>();
                for (FileContent fc : files) {
                    String name = fc.getName().replace('\\', '/');
                    if (name.startsWith(prefix)) {
                        name = name.substring(prefix.length());
                    }
                    if (!name.isBlank()) {
                        normalised2.add(FileContent.builder()
                                .name(name)
                                .content(fc.getContent())
                                .extension(fc.getExtension())
                                .build());
                    }
                }
                return normalised2;
            } catch (IOException e) {
                log.debug("Could not fetch branch '{}' for {}/{}: {}", branch, owner, repo, e.getMessage());
            }
        }

        throw new IOException("Could not clone repository: " + repoUrl
                + ". Tried branches main and master.");
    }

    // -------------------------------------------------------------------------
    // Commit documentation
    // -------------------------------------------------------------------------

    /**
     * Create or update README.md in the specified repository.
     * Mirrors Python's commit_documentation() route.
     *
     * @param accessToken   GitHub OAuth token
     * @param repoFullName  "owner/repo" identifier
     * @param documentation the Markdown documentation string
     * @param commitMessage commit message
     * @param branch        target branch
     * @return result map with keys: success, message, repository, branch, html_url
     * @throws IOException on API errors
     */
    public Map<String, Object> commitDocumentation(
            String accessToken, String repoFullName, String documentation,
            String commitMessage, String branch) throws IOException {

        GitHub github = new GitHubBuilder().withOAuthToken(accessToken).build();
        GHRepository ghRepo;
        try {
            ghRepo = github.getRepository(repoFullName);
        } catch (IOException e) {
            throw new IOException("Repository not found: " + repoFullName);
        }

        byte[] docBytes = documentation.getBytes(StandardCharsets.UTF_8);
        String action;
        try {
            GHContent existing = ghRepo.getFileContent("README.md", branch);
            ghRepo.createContent()
                    .path("README.md")
                    .message(commitMessage)
                    .content(docBytes)
                    .sha(existing.getSha())
                    .branch(branch)
                    .commit();
            action = "updated";
        } catch (GHFileNotFoundException e) {
            ghRepo.createContent()
                    .path("README.md")
                    .message(commitMessage)
                    .content(docBytes)
                    .branch(branch)
                    .commit();
            action = "created";
        }

        String htmlUrl = ghRepo.getHtmlUrl() != null
                ? ghRepo.getHtmlUrl().toString() + "/blob/" + branch + "/README.md"
                : "";

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success",    true);
        result.put("message",    "Documentation " + action + " successfully in " + repoFullName);
        result.put("repository", repoFullName);
        result.put("branch",     branch);
        result.put("html_url",   htmlUrl);
        result.put("action",     action);
        return result;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private String normaliseUrl(String url) {
        if (url == null) return "";
        // Remove zero-width and non-breaking spaces (mirrors Python _normalize())
        url = url.replaceAll("[\u00A0\u200B\u200C\u200D\uFEFF]", "").strip();
        if (url.startsWith("http://github.com/"))  url = "https://" + url.substring(7);
        if (url.startsWith("github.com/"))          url = "https://" + url;
        return url;
    }

    private byte[] downloadBytes(String url) throws IOException {
        Request req = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", "CodeDocumentor/1.0")
                .build();
        try (Response resp = httpClient.newCall(req).execute()) {
            if (!resp.isSuccessful() || resp.body() == null) {
                throw new IOException("HTTP " + resp.code() + " for " + url);
            }
            return resp.body().bytes();
        }
    }
}
