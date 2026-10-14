package com.codedocumentor.service;

import com.codedocumentor.model.FileContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Handles file extraction from uploaded MultipartFiles and ZIP archives.
 * Mirrors the Python helpers: allowed_file(), should_ignore_path(), extract_files_from_zip().
 */
@Service
public class FileProcessingService {

    private static final Logger log = LoggerFactory.getLogger(FileProcessingService.class);

    /** Supported code/config file extensions (mirrors Python SUPPORTED_EXTENSIONS set) */
    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of(
            "js", "py", "java", "cpp", "c", "html", "css", "php",
            "rb", "go", "rs", "ts", "jsx", "tsx", "vue", "svelte",
            "md", "txt", "json", "xml", "yaml", "yml", "toml", "ini",
            "sh", "bat", "ps1", "sql", "r", "scala", "kt", "swift"
    );

    /** Directories/files to skip (mirrors Python IGNORE_PATTERNS set) */
    private static final Set<String> IGNORE_PATTERNS = Set.of(
            ".git", "__pycache__", "node_modules", ".vscode", ".idea",
            "dist", "build", "target", "bin", "obj", "venv", "env",
            ".DS_Store", "Thumbs.db", ".gitignore", ".env"
    );

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Process a single uploaded {@link MultipartFile}.
     * If the file is a ZIP it is expanded; otherwise the file is returned directly.
     *
     * @param file the uploaded file
     * @return list of extracted {@link FileContent} objects
     * @throws IOException on read/decode errors
     */
    public List<FileContent> processUploadedFile(MultipartFile file) throws IOException {
        String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "";
        String safeName = Paths.get(originalName).getFileName().toString(); // strip path traversal

        if (safeName.toLowerCase().endsWith(".zip")) {
            return extractFilesFromZip(file.getInputStream());
        }

        if (!isAllowedFile(safeName)) {
            return Collections.emptyList();
        }

        String content;
        try {
            content = new String(file.getBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.warn("Cannot read file {} as UTF-8, skipping", safeName);
            return Collections.emptyList();
        }

        if (content.isBlank()) {
            return Collections.emptyList();
        }

        return List.of(FileContent.builder()
                .name(safeName)
                .content(content)
                .extension(getExtension(safeName))
                .build());
    }

    /**
     * Extract all supported code files from a ZIP {@link InputStream}.
     * Mirrors Python extract_files_from_zip().
     */
    public List<FileContent> extractFilesFromZip(InputStream zipInputStream) throws IOException {
        List<FileContent> results = new ArrayList<>();

        try (ZipInputStream zis = new ZipInputStream(zipInputStream, StandardCharsets.UTF_8)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName();

                if (entry.isDirectory() || shouldIgnorePath(name) || !isAllowedFile(name)) {
                    zis.closeEntry();
                    continue;
                }

                try {
                    byte[] bytes = zis.readAllBytes();
                    String content = new String(bytes, StandardCharsets.UTF_8);

                    if (!content.isBlank()) {
                        results.add(FileContent.builder()
                                .name(name)
                                .content(content)
                                .extension(getExtension(name))
                                .build());
                    }
                } catch (IllegalArgumentException e) {
                    log.debug("Skipping non-UTF8 entry: {}", name);
                } finally {
                    zis.closeEntry();
                }
            }
        }

        return results;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Returns true when the file extension is in the supported set.
     * Mirrors Python allowed_file().
     */
    public boolean isAllowedFile(String filename) {
        String ext = getExtension(filename);
        return !ext.isEmpty() && SUPPORTED_EXTENSIONS.contains(ext);
    }

    /**
     * Returns true when any path segment matches an ignore pattern.
     * Mirrors Python should_ignore_path().
     */
    public boolean shouldIgnorePath(String path) {
        // Normalise separators
        String normalised = path.replace('\\', '/');
        for (String part : normalised.split("/")) {
            if (IGNORE_PATTERNS.contains(part)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the lowercased extension of a filename without the leading dot.
     * Returns empty string for files with no extension.
     */
    public String getExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) return "";
        return filename.substring(dot + 1).toLowerCase();
    }
}
