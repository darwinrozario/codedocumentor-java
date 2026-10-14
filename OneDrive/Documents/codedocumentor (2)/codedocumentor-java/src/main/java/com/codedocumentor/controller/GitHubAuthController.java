package com.codedocumentor.controller;

import com.codedocumentor.model.GitHubUser;
import com.codedocumentor.service.GitHubService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;

/**
 * Handles the GitHub OAuth dance and repository API calls.
 * Mirrors Flask routes: /auth/github, /auth/github/callback, /auth/github/logout,
 * /api/github/repositories, /api/github/commit-documentation.
 */
@Controller
public class GitHubAuthController {

    private static final Logger log = LoggerFactory.getLogger(GitHubAuthController.class);

    // Session attribute keys (mirrors Python session dict keys)
    static final String SESSION_GITHUB_TOKEN = "github_token";
    static final String SESSION_GITHUB_STATE = "github_state";
    static final String SESSION_GITHUB_USER  = "github_user";

    private final GitHubService gitHubService;

    public GitHubAuthController(GitHubService gitHubService) {
        this.gitHubService = gitHubService;
    }

    // -------------------------------------------------------------------------
    // GET /auth/github  — initiate OAuth flow
    // -------------------------------------------------------------------------

    /**
     * Redirect the user to GitHub's authorisation page.
     * Mirrors Python's github_auth() route.
     */
    @GetMapping("/auth/github")
    public String githubAuth(HttpSession session) {
        String state = UUID.randomUUID().toString();
        session.setAttribute(SESSION_GITHUB_STATE, state);
        String authUrl = gitHubService.buildAuthorizationUrl(state);
        return "redirect:" + authUrl;
    }

    // -------------------------------------------------------------------------
    // GET /auth/github/callback  — OAuth callback
    // -------------------------------------------------------------------------

    /**
     * Handle the callback from GitHub, exchange the code for a token, and redirect home.
     * Mirrors Python's github_callback() route.
     */
    @GetMapping("/auth/github/callback")
    public String githubCallback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            HttpSession session) {

        String savedState = (String) session.getAttribute(SESSION_GITHUB_STATE);
        if (state == null || !state.equals(savedState)) {
            log.warn("GitHub OAuth state mismatch");
            return "redirect:/?error=invalid_state";
        }

        if (code == null) {
            return "redirect:/?error=no_code";
        }

        try {
            String accessToken = gitHubService.exchangeCodeForToken(code);
            GitHubUser user    = gitHubService.fetchUserInfo(accessToken);

            session.setAttribute(SESSION_GITHUB_TOKEN, accessToken);
            session.setAttribute(SESSION_GITHUB_USER,  user);

            return "redirect:/";
        } catch (IOException e) {
            log.error("GitHub OAuth callback failed: {}", e.getMessage());
            return "redirect:/?error=auth_failed";
        }
    }

    // -------------------------------------------------------------------------
    // GET /auth/github/logout
    // -------------------------------------------------------------------------

    /**
     * Clear GitHub session data and redirect to home.
     * Mirrors Python's github_logout() route.
     */
    @GetMapping("/auth/github/logout")
    public String githubLogout(HttpSession session) {
        session.removeAttribute(SESSION_GITHUB_TOKEN);
        session.removeAttribute(SESSION_GITHUB_USER);
        session.removeAttribute(SESSION_GITHUB_STATE);
        return "redirect:/";
    }

    // -------------------------------------------------------------------------
    // GET /api/github/repositories
    // -------------------------------------------------------------------------

    /**
     * Return the authenticated user's GitHub repositories as JSON.
     * Mirrors Python's get_github_repositories() route.
     */
    @GetMapping(value = "/api/github/repositories", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getRepositories(HttpSession session) {
        String token = (String) session.getAttribute(SESSION_GITHUB_TOKEN);
        if (token == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated with GitHub"));
        }

        try {
            List<Map<String, Object>> repos = gitHubService.listRepositories(token);
            return ResponseEntity.ok(Map.of("repositories", repos));
        } catch (IOException e) {
            log.error("Failed to fetch repositories: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to fetch repositories: " + e.getMessage()));
        }
    }

    // -------------------------------------------------------------------------
    // POST /api/github/commit-documentation
    // -------------------------------------------------------------------------

    /**
     * Commit (create or update) the generated documentation as README.md.
     * Mirrors Python's commit_documentation() route.
     */
    @PostMapping(value = "/api/github/commit-documentation", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> commitDocumentation(
            @RequestBody Map<String, String> body,
            HttpSession session) {

        String token = (String) session.getAttribute(SESSION_GITHUB_TOKEN);
        if (token == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated with GitHub"));
        }

        String repoName     = Optional.ofNullable(body.get("repository")).orElse("");
        String documentation= Optional.ofNullable(body.get("documentation")).orElse("");
        String commitMessage= Optional.ofNullable(body.get("commit_message"))
                                      .filter(s -> !s.isBlank())
                                      .orElse("Update project documentation");
        String branch       = Optional.ofNullable(body.get("branch"))
                                      .filter(s -> !s.isBlank())
                                      .orElse("main");

        if (repoName.isBlank() || documentation.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Repository name and documentation are required"));
        }

        try {
            Map<String, Object> result = gitHubService.commitDocumentation(
                    token, repoName, documentation, commitMessage, branch);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            log.error("Failed to commit documentation to {}: {}", repoName, e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to commit documentation: " + e.getMessage()));
        }
    }
}
