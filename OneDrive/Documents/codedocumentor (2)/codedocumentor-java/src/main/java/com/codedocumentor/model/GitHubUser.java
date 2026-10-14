package com.codedocumentor.model;

/**
 * Lightweight representation of the authenticated GitHub user stored in the HTTP session.
 * Mirrors the Python session dict: {'login': ..., 'name': ..., 'email': ..., 'avatar_url': ...}
 */
public class GitHubUser {

    private String login;
    private String name;
    private String email;
    private String avatarUrl;

    public GitHubUser() {}

    public GitHubUser(String login, String name, String email, String avatarUrl) {
        this.login     = login;
        this.name      = name;
        this.email     = email;
        this.avatarUrl = avatarUrl;
    }

    public String getLogin()     { return login; }
    public String getName()      { return name; }
    public String getEmail()     { return email; }
    public String getAvatarUrl() { return avatarUrl; }

    public void setLogin(String login)         { this.login = login; }
    public void setName(String name)           { this.name = name; }
    public void setEmail(String email)         { this.email = email; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    // Builder pattern (replaces Lombok @Builder)
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String login;
        private String name;
        private String email;
        private String avatarUrl;

        public Builder login(String login)         { this.login = login;         return this; }
        public Builder name(String name)           { this.name = name;           return this; }
        public Builder email(String email)         { this.email = email;         return this; }
        public Builder avatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; return this; }

        public GitHubUser build() {
            return new GitHubUser(login, name, email, avatarUrl);
        }
    }
}
