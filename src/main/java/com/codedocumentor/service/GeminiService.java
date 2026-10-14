package com.codedocumentor.service;

import com.codedocumentor.model.FileContent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Wraps the Google Gemini generative language REST API.
 * Mirrors Python's call_gemini_api() and create_documentation_prompt() functions.
 */
@Service
public class GeminiService {

    private static final Logger log = LoggerFactory.getLogger(GeminiService.class);

    private static final String GEMINI_BASE_URL =
            "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent";

    @Value("${GEMINI_API_KEY:}")
    private String geminiApiKey;

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public GeminiService() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Generate project documentation from a list of code files.
     * Mirrors Python's generate_documentation route logic.
     *
     * @param files list of extracted file contents
     * @return Markdown string with the generated documentation
     * @throws IOException on API or network errors
     */
    public String generateDocumentation(List<FileContent> files) throws IOException {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("No valid files found to analyze.");
        }
        String prompt = createDocumentationPrompt(files);
        return callGeminiApi(prompt);
    }

    /**
     * Answer a free-form question about a project given its documentation as context.
     * Mirrors Python's chatbot route.
     *
     * @param question       the user's question
     * @param documentation  the previously generated Markdown documentation
     * @return Markdown/HTML answer from Gemini
     * @throws IOException on API or network errors
     */
    public String answerChatbotQuestion(String question, String documentation) throws IOException {
        if (geminiApiKey == null || geminiApiKey.isBlank()) {
            throw new IllegalStateException(
                    "GEMINI_API_KEY is not configured. Set it in the .env file or as an environment variable.");
        }

        String prompt = """
You are an AI assistant helping developers understand their code project. \
Based on the following project documentation, please answer the user's question in a CONCISE and helpful way.

Project Documentation:
%s

User Question: %s

IMPORTANT INSTRUCTIONS:
1. Keep your response SHORT and FOCUSED (maximum 3-4 sentences)
2. Answer directly what the user asked
3. If referencing code, mention specific function/class names
4. Use HTML formatting: <strong>bold text</strong> and <em>italic text</em> (NOT markdown **text** or *text*)
5. If the question is about usage, provide a brief example
6. If troubleshooting, give a concise solution
7. Be conversational but professional

Format your response in clean HTML with proper tags like <p>, <strong>, <em>, <code>, <ul>, <li>.
""".formatted(documentation, question);

        return callGeminiApi(prompt);
    }

    // -------------------------------------------------------------------------
    // Prompt construction  (mirrors Python create_documentation_prompt)
    // -------------------------------------------------------------------------

    /**
     * Build the Gemini prompt from the list of file contents.
     * Files are grouped by their parent directory, mirroring the Python implementation.
     */
    String createDocumentationPrompt(List<FileContent> files) {
        StringBuilder sb = new StringBuilder();
        sb.append("""
Please analyze the following code files and generate comprehensive, well-structured documentation \
using Markdown formatting.

For each file, provide:

1. **Purpose and Overview**: What the file does and its role in the project
2. **Key Functions/Classes**: Main components with their purpose and functionality
3. **Dependencies**: External libraries, modules, or frameworks used
4. **Usage Examples**: How to use key functions/classes with code examples
5. **Code Comments**: Explain complex logic, algorithms, and important decisions
6. **API Documentation**: For functions, include parameters, return values, and exceptions

Format the output in clean, professional Markdown with:
- Don't include "Here's a comprehensive, well-structured documentation for the provided codebase"
- Clear hierarchical headings (# ## ### ####)
- Proper code blocks with syntax highlighting using ```language
- Bold text for emphasis using **text**
- Italic text using *text*
- Clean spacing and organization
- Tables where appropriate using Markdown table syntax
- Lists using - or 1. for better readability
- Proper line breaks and spacing

Here are the code files to analyze:

---

""");

        // Group by parent directory (mirrors Python files_by_dir logic)
        Map<String, List<FileContent>> byDir = new LinkedHashMap<>();
        for (FileContent fc : files) {
            String parent = Path.of(fc.getName()).getParent() != null
                    ? Path.of(fc.getName()).getParent().toString().replace('\\', '/')
                    : "Root";
            byDir.computeIfAbsent(parent, k -> new ArrayList<>()).add(fc);
        }

        List<String> dirs = new ArrayList<>(byDir.keySet());
        Collections.sort(dirs);
        // Ensure "Root" is first
        if (dirs.remove("Root")) dirs.add(0, "Root");

        for (String dir : dirs) {
            if (!"Root".equals(dir)) {
                sb.append("## 📁 ").append(dir).append("/\n\n");
            }
            for (FileContent fc : byDir.get(dir)) {
                sb.append("### 📄 ").append(fc.getName()).append("\n\n");
                sb.append("**File Type**: ").append(fc.getExtension().toUpperCase()).append("\n\n");
                sb.append("```").append(fc.getExtension()).append("\n")
                  .append(fc.getContent()).append("\n```\n\n---\n\n");
            }
        }

        sb.append("""

Please provide a comprehensive project documentation that includes:

# 📋 Project Overview
- Project purpose and goals
- Technology stack and architecture
- Project structure and organization

# 📚 File Documentation
Individual file analysis (as requested above)

# 🚀 Setup and Installation
- Prerequisites and dependencies
- Installation steps
- Configuration requirements

# 💻 Usage Examples
- How to run the project
- Common use cases
- Code examples

# 🔧 API Reference
- Function and class documentation
- Parameter descriptions
- Return value explanations
- Error handling

# 🤝 Contributing Guidelines
- How to contribute to the project
- Code style and standards
- Testing requirements

Make the documentation clear, professional, and useful for developers who want to understand \
and work with this codebase. Use proper Markdown formatting with **bold text**, *italic text*, \
and clear organization.""");

        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // Low-level HTTP call  (mirrors Python call_gemini_api)
    // -------------------------------------------------------------------------

    /**
     * Send a prompt to the Gemini REST endpoint and return the generated text.
     *
     * @param prompt the full prompt string
     * @return the model's text response
     * @throws IOException on HTTP or parsing errors
     */
    String callGeminiApi(String prompt) throws IOException {
        if (geminiApiKey == null || geminiApiKey.isBlank()) {
            throw new IllegalStateException(
                    "GEMINI_API_KEY is not configured. Set it in the .env file or as an environment variable.");
        }

        // Build request body
        ObjectNode body = objectMapper.createObjectNode();
        ArrayNode contents = body.putArray("contents");
        ObjectNode contentObj = contents.addObject();
        ArrayNode parts = contentObj.putArray("parts");
        parts.addObject().put("text", prompt);

        ObjectNode genConfig = body.putObject("generationConfig");
        genConfig.put("temperature", 0.2);
        genConfig.put("topK", 32);
        genConfig.put("topP", 0.95);
        genConfig.put("maxOutputTokens", 16384);

        String url = GEMINI_BASE_URL + "?key=" + geminiApiKey;

        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(
                        objectMapper.writeValueAsString(body),
                        MediaType.parse("application/json; charset=utf-8")))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";

            if (!response.isSuccessful()) {
                throw new IOException("Gemini API request failed: " + response.code() + " " + responseBody);
            }

            JsonNode data = objectMapper.readTree(responseBody);
            log.debug("Gemini API response keys: {}", data.fieldNames());

            // Parse candidates[0].content.parts[0].text
            JsonNode candidates = data.path("candidates");
            if (candidates.isArray() && !candidates.isEmpty()) {
                JsonNode text = candidates.get(0)
                        .path("content").path("parts").path(0).path("text");
                if (!text.isMissingNode()) {
                    return text.asText();
                }
            }

            // Fallback paths
            if (data.has("text")) return data.get("text").asText();
            if (data.has("error")) throw new IOException("Gemini API error: " + data.get("error"));

            throw new IOException("Unexpected Gemini API response structure: " + responseBody);
        }
    }
}
