package com.codedocumentor.model;

/**
 * Represents one code file extracted from an upload or GitHub clone.
 * Mirrors the Python dict: {'name': ..., 'content': ..., 'extension': ...}
 */
public class FileContent {

    /** Relative file path (e.g. "src/main.py" or just "app.py") */
    private String name;
    /** Full UTF-8 text content of the file */
    private String content;
    /** Lowercase extension without dot (e.g. "py", "java", "ts") */
    private String extension;

    public FileContent() {}

    public FileContent(String name, String content, String extension) {
        this.name      = name;
        this.content   = content;
        this.extension = extension;
    }

    public String getName()      { return name; }
    public String getContent()   { return content; }
    public String getExtension() { return extension; }

    public void setName(String name)           { this.name = name; }
    public void setContent(String content)     { this.content = content; }
    public void setExtension(String extension) { this.extension = extension; }

    // Builder pattern (replaces Lombok @Builder)
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String name;
        private String content;
        private String extension;

        public Builder name(String name)           { this.name = name;           return this; }
        public Builder content(String content)     { this.content = content;     return this; }
        public Builder extension(String extension) { this.extension = extension; return this; }

        public FileContent build() {
            return new FileContent(name, content, extension);
        }
    }
}
