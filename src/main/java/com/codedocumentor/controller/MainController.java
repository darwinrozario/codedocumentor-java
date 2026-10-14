package com.codedocumentor.controller;

import com.codedocumentor.model.FileContent;
import com.codedocumentor.service.DownloadStore;
import com.codedocumentor.service.FileProcessingService;
import com.codedocumentor.service.GeminiService;
import com.codedocumentor.service.GitHubService;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Primary Spring MVC controller.
 * Maps the same URLs as the Flask app so the existing JavaScript front-end
 * (fetch calls) works without any modification.
 *
 * Routes mirrored from Python:
 *   GET  /                                -> index page
 *   POST /generate-documentation          -> file/ZIP upload → Gemini
 *   POST /analyze-github                  -> GitHub repo clone → Gemini
 *   POST /download-documentation          -> return doc for client-side download
 *   GET  /download/<token>                -> server-side markdown file download
 *   POST /chatbot                         -> question-answering over documentation
 */
@Controller
public class MainController {

    private static final Logger log = LoggerFactory.getLogger(MainController.class);

    private final FileProcessingService fileProcessingService;
    private final GeminiService geminiService;
    private final GitHubService gitHubService;
    private final DownloadStore downloadStore;

    public MainController(FileProcessingService fileProcessingService,
                          GeminiService geminiService,
                          GitHubService gitHubService,
                          DownloadStore downloadStore) {
        this.fileProcessingService = fileProcessingService;
        this.geminiService         = geminiService;
        this.gitHubService         = gitHubService;
        this.downloadStore         = downloadStore;
    }

    // -------------------------------------------------------------------------
    // GET /
    // -------------------------------------------------------------------------

    /** Render the main page (mirrors Flask's index() route). */
    @GetMapping("/")
    public String index() {
        return "index_with_github"; // Thymeleaf template name
    }

    // -------------------------------------------------------------------------
    // POST /generate-documentation
    // -------------------------------------------------------------------------

    /**
     * Accept file or ZIP uploads, call Gemini, return JSON with documentation.
     * Mirrors Flask's generate_documentation() route.
     */
    @PostMapping(value = "/generate-documentation", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> generateDocumentation(
            @RequestParam("files") List<MultipartFile> files) {

        if (files == null || files.isEmpty() || files.get(0).isEmpty()) {
            return badRequest("No files selected");
        }

        List<FileContent> fileContents = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;
            try {
                fileContents.addAll(fileProcessingService.processUploadedFile(file));
            } catch (IOException e) {
                log.warn("Error processing uploaded file {}: {}", file.getOriginalFilename(), e.getMessage());
                return badRequest("Error processing file: " + e.getMessage());
            }
        }

        if (fileContents.isEmpty()) {
            return badRequest("No valid code files found in the uploaded content.");
        }

        try {
            String documentation = geminiService.generateDocumentation(fileContents);
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success",         true);
            response.put("documentation",   documentation);
            response.put("files_processed", fileContents.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Documentation generation failed", e);
            return serverError(e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // POST /analyze-github
    // -------------------------------------------------------------------------

    /**
     * Clone a GitHub repository and generate documentation.
     * Mirrors Flask's analyze_github() route.
     */
    @PostMapping(value = "/analyze-github", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> analyzeGithub(
            @RequestBody Map<String, String> body) {

        String repoUrl = Optional.ofNullable(body.get("repo_url")).orElse("").strip();
        if (repoUrl.isEmpty()) {
            return badRequest("No repository URL provided");
        }

        try {
            List<FileContent> fileContents = gitHubService.cloneRepository(repoUrl);
            if (fileContents.isEmpty()) {
                return badRequest("No valid code files found in the repository.");
            }

            String documentation = geminiService.generateDocumentation(fileContents);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success",         true);
            response.put("documentation",   documentation);
            response.put("files_processed", fileContents.size());
            response.put("repo_url",        repoUrl);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("GitHub analysis failed for {}: {}", repoUrl, e.getMessage());
            return serverError(e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // POST /download-documentation
    // -------------------------------------------------------------------------

    /**
     * Echo the documentation back so the JavaScript can trigger a client-side download.
     * Mirrors Flask's download_documentation() route.
     */
    @PostMapping(value = "/download-documentation", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> downloadDocumentation(
            @RequestBody Map<String, String> body) {

        String documentation = Optional.ofNullable(body.get("documentation")).orElse("");
        if (documentation.isBlank()) {
            return badRequest("No documentation provided");
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success",       true);
        response.put("documentation", documentation);
        response.put("filename",      "project-documentation.md");
        return ResponseEntity.ok(response);
    }

    // -------------------------------------------------------------------------
    // GET /download/{token}
    // -------------------------------------------------------------------------

    /**
     * Serve a previously stored documentation blob as a downloadable Markdown file.
     * Mirrors Flask's download_markdown() route.
     */
    @GetMapping("/download/{token}")
    public void downloadMarkdown(@PathVariable String token, HttpServletResponse response)
            throws IOException {

        String content = downloadStore.get(token);
        if (content == null) {
            response.sendError(404, "Invalid or expired download link");
            return;
        }

        response.setContentType("text/markdown; charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=project-documentation.md");
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        response.setContentLength(bytes.length);
        response.getOutputStream().write(bytes);
    }

    // -------------------------------------------------------------------------
    // POST /chatbot
    // -------------------------------------------------------------------------

    /**
     * Answer a user question about the project using the generated documentation.
     * Mirrors Flask's chatbot() route.
     */
    @PostMapping(value = "/chatbot", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> chatbot(@RequestBody Map<String, String> body) {
        String question      = Optional.ofNullable(body.get("question")).orElse("").strip();
        String documentation = Optional.ofNullable(body.get("documentation")).orElse("");

        if (question.isEmpty()) {
            return badRequest("No question provided");
        }
        if (documentation.isBlank()) {
            return badRequest("No documentation context provided");
        }

        try {
            String answer = geminiService.answerChatbotQuestion(question, documentation);
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("answer",  answer);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Chatbot request failed", e);
            return serverError(e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // SalesIQ webhook  (mirrors Python salesiq_webhook route)
    // -------------------------------------------------------------------------

    @RequestMapping(value = "/webhooks/salesiq", method = {
            RequestMethod.HEAD, RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public ResponseEntity<Map<String, Object>> salesIqWebhook(
            @RequestBody(required = false) Map<String, Object> payload) {

        final Map<String, Object> safePayload = (payload != null) ? payload : Collections.emptyMap();

        String action = Optional.ofNullable((String) safePayload.get("action"))
                .or(() -> Optional.ofNullable((String) safePayload.get("event")))
                .orElse("render")
                .toLowerCase();

        if (action.equals("render") || action.equals("open")) {
            return ResponseEntity.ok(widgetLayoutInitial());
        }

        if (action.equals("generate") || action.equals("submit")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) safePayload.getOrDefault("data", Collections.emptyMap());
            String repoUrl = Optional.ofNullable((String) safePayload.get("repo_url"))
                    .or(() -> Optional.ofNullable((String) data.get("repo_url")))
                    .orElse("").strip();

            if (repoUrl.isEmpty()) {
                return ResponseEntity.ok(widgetLayoutError("Please enter a valid GitHub repository URL."));
            }

            try {
                List<FileContent> files = gitHubService.cloneRepository(repoUrl);
                if (files.isEmpty()) {
                    return ResponseEntity.ok(widgetLayoutError("No valid code files found in the repository."));
                }
                String documentation = geminiService.generateDocumentation(files);
                String token = downloadStore.save(documentation);
                String downloadUrl = "/download/" + token;
                return ResponseEntity.ok(widgetLayoutSuccess(downloadUrl));
            } catch (Exception e) {
                return ResponseEntity.ok(widgetLayoutError(e.getMessage()));
            }
        }

        return ResponseEntity.ok(widgetLayoutInitial());
    }

    // -------------------------------------------------------------------------
    // SalesIQ widget layout helpers (mirrors Python _widget_layout_* functions)
    // -------------------------------------------------------------------------

    private Map<String, Object> widgetLayoutInitial() {
        return Map.of("type", "widget_detail", "sections", List.of(
                Map.of("name", "docForm", "layout", "form", "title", "Code Documentor",
                        "fields", List.of(Map.of("id", "repo_url", "label", "GitHub Repository URL",
                                "type", "text", "placeholder", "https://github.com/owner/repo")),
                        "buttons", List.of(Map.of("id", "generate", "label", "Generate Documentation",
                                "style", "primary", "action", "generate")))));
    }

    private Map<String, Object> widgetLayoutError(String message) {
        return Map.of("type", "widget_detail", "sections", List.of(
                Map.of("name", "status", "layout", "info", "title", "Code Documentor",
                        "data", List.of(
                                Map.of("label", "Status",  "value", "Error"),
                                Map.of("label", "Message", "value", message))),
                Map.of("name", "docForm", "layout", "form", "title", "Try Again",
                        "fields", List.of(Map.of("id", "repo_url", "label", "GitHub Repository URL",
                                "type", "text", "placeholder", "https://github.com/owner/repo")),
                        "buttons", List.of(Map.of("id", "generate", "label", "Generate Again",
                                "style", "primary", "action", "generate")))));
    }

    private Map<String, Object> widgetLayoutSuccess(String downloadUrl) {
        return Map.of("type", "widget_detail", "sections", List.of(
                Map.of("name", "status", "layout", "info", "title", "Success",
                        "data", List.of(Map.of("label", "Status",
                                "value", "Documentation generated successfully."))),
                Map.of("name", "actions", "layout", "actions",
                        "buttons", List.of(Map.of("id", "download_md", "label", "Download Markdown",
                                "style", "primary", "href", downloadUrl, "target", "_blank"))),
                Map.of("name", "docForm", "layout", "form", "title", "Generate Again",
                        "fields", List.of(Map.of("id", "repo_url", "label", "GitHub Repository URL",
                                "type", "text", "placeholder", "https://github.com/owner/repo")),
                        "buttons", List.of(Map.of("id", "generate", "label", "Generate Again",
                                "style", "secondary", "action", "generate")))));
    }

    // -------------------------------------------------------------------------
    // Zobot endpoint  (mirrors Python zobot_analyze_repo route)
    // -------------------------------------------------------------------------

    @PostMapping(value = "/zobot/analyze-repo", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> zobotAnalyzeRepo(
            @RequestBody(required = false) Map<String, Object> body) {

        if (body == null) body = Collections.emptyMap();
        String repoUrl = Optional.ofNullable((String) body.get("repo_url")).orElse("").strip();
        if (repoUrl.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status",  "error",
                    "message", "Missing 'repo_url' in request body."));
        }

        try {
            List<FileContent> files = gitHubService.cloneRepository(repoUrl);
            if (files.isEmpty()) throw new IllegalStateException("No valid code files found.");

            String documentation = geminiService.generateDocumentation(files);

            // Derive modules (top-level folders)
            Set<String> modulesSet = new LinkedHashSet<>();
            for (FileContent fc : files) {
                String[] parts = fc.getName().replace('\\', '/').split("/", 2);
                if (parts.length > 1) modulesSet.add(parts[0]);
            }
            List<String> modules = modulesSet.stream().sorted().limit(10).toList();
            if (modules.isEmpty()) modules = List.of("Root");

            // Key-file heuristic (same keyword list as Python)
            String[] keywords = {"readme","setup","requirements","package.json","app","main",
                    "index","server","api","router","route","controller","model",
                    "service","config","settings","utils","helper"};
            List<String> keyFiles = files.stream()
                    .map(FileContent::getName)
                    .sorted(Comparator.comparingInt((String n) -> scoreFile(n, keywords)).reversed())
                    .distinct().limit(10).toList();

            // Summary: first 8 non-heading lines of the documentation
            String summary = extractSummary(documentation);

            // Save for download
            String token      = downloadStore.save(documentation);
            String downloadUrl = "/download/" + token;

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("status",       "success");
            result.put("summary",      summary);
            result.put("modules",      modules);
            result.put("key_files",    keyFiles);
            result.put("download_url", downloadUrl);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Zobot analysis failed", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status",  "error",
                    "message", e.getMessage()));
        }
    }

    // -------------------------------------------------------------------------
    // Error response helpers
    // -------------------------------------------------------------------------

    private static ResponseEntity<Map<String, Object>> badRequest(String message) {
        return ResponseEntity.badRequest().body(Map.of("error", message));
    }

    private static ResponseEntity<Map<String, Object>> serverError(String message) {
        return ResponseEntity.internalServerError().body(Map.of("error", message));
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private int scoreFile(String name, String[] keywords) {
        String lower = name.toLowerCase();
        int score = 0;
        for (int i = 0; i < keywords.length; i++) {
            if (lower.contains(keywords[i])) score += keywords.length - i;
        }
        long depth = name.replace('\\', '/').chars().filter(c -> c == '/').count();
        return score - (int) depth;
    }

    private String extractSummary(String documentation) {
        String[] lines = documentation.split("\n");
        int startIdx = 0;
        for (int i = 0; i < lines.length; i++) {
            String l = lines[i].trim();
            if (l.matches("^#.*Project Overview.*")) { startIdx = i + 1; break; }
        }
        List<String> picked = new ArrayList<>();
        for (int i = startIdx; i < lines.length && picked.size() < 8; i++) {
            String l = lines[i].trim();
            if (!l.isEmpty() && !l.startsWith("#") && !l.startsWith("---")) picked.add(l);
        }
        if (picked.size() < 4) {
            picked.clear();
            for (String line : lines) {
                String l = line.trim();
                if (!l.isEmpty() && !l.startsWith("#") && !l.startsWith("---")) picked.add(l);
                if (picked.size() >= 6) break;
            }
        }
        return String.join("\n", picked.subList(0, Math.min(8, picked.size())));
    }
}
