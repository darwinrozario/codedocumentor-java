# 📋 Project Overview

This project, "Code Auto Documenter — Java / Spring Boot," is a Java rewrite of an original Python/Flask application. Its primary purpose is to **automatically generate comprehensive, well-structured documentation for codebases using an AI model (Google Gemini)**. It features robust **GitHub integration**, allowing users to analyze public repositories, list their own repositories, and commit generated documentation directly to a chosen repository as a `README.md` file. The application also includes a **chatbot feature** for interactive Q&A over the generated documentation and integrates with external platforms like SalesIQ and Zobot via webhooks.

The core goal is to provide developers with an AI-powered tool to quickly understand and document their projects, streamlining the documentation process and fostering better code comprehension.

## Technology Stack and Architecture

The project is built on the **Spring Boot 3** framework, leveraging its capabilities for rapid web application development. It follows a traditional Model-View-Controller (MVC) architecture, with controllers handling requests, services encapsulating business logic, and models representing data structures.

| Layer             | Spring Boot (this project)       |
| :---------------- | :------------------------------- |
| **Web framework** | Spring Boot 3 (Spring MVC)       |
| **Templating**    | Thymeleaf                        |
| **HTTP client**   | OkHttp 4                         |
| **GitHub API**    | `kohsuke/github-api`             |
| **Env vars**      | `dotenv-java`                    |
| **Build tool**    | Maven                            |
| **Java version**  | 17 (configured to 21 in `pom.xml`)|
| **AI Model**      | Google Gemini (via REST API)     |
| **Frontend**      | HTML, CSS (Tailwind CSS), JavaScript (Marked.js, Font Awesome) |

The architecture is designed to be stateless where possible, with session management handled by Spring Session for GitHub authentication. File processing and AI calls are delegated to dedicated service layers, ensuring separation of concerns. The frontend JavaScript interacts with the backend via REST endpoints, maintaining compatibility with the original Flask application's frontend.

## Project Structure and Organization

The project follows a standard Maven and Spring Boot directory structure:

```
codedocumentor-java/
├── .env.example
├── pom.xml
├── README.md
├── run.bat
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── codedocumentor/
        │           ├── config/
        │           │   └── AppConfig.java          # Application-wide configuration
        │           ├── controller/
        │           │   ├── GitHubAuthController.java # Handles GitHub OAuth and API
        │           │   └── MainController.java       # Main application endpoints
        │           ├── model/
        │           │   ├── FileContent.java          # Data model for code files
        │           │   └── GitHubUser.java           # Data model for GitHub user
        │           ├── service/
        │           │   ├── DownloadStore.java        # In-memory store for downloads
        │           │   ├── FileProcessingService.java# Handles file/ZIP extraction
        │           │   ├── GeminiService.java        # Interacts with Google Gemini API
        │           │   └── GitHubService.java        # Low-level GitHub API interactions
        │           └── CodeDocumentorApplication.java # Main Spring Boot entry point
        └── resources/
            ├── static/
            │   ├── css/
            │   │   └── styles.css                    # Custom CSS styles
            │   └── js/
            │       ├── chatbot.js                    # Chatbot frontend logic
            │       ├── github-integration.js         # GitHub frontend logic
            │       └── script.js                     # Main frontend logic
            └── templates/
                └── index_with_github.html            # Thymeleaf main HTML template
```

-   **`com.codedocumentor`**: The root package for all Java source code.
-   **`config`**: Contains configuration classes for the Spring application.
-   **`controller`**: Houses Spring MVC controllers that handle incoming HTTP requests and return responses.
-   **`model`**: Defines Plain Old Java Objects (POJOs) used for data transfer and representation.
-   **`service`**: Contains business logic, interacting with external APIs (Gemini, GitHub) and performing core application tasks (file processing, documentation generation).
-   **`resources/static`**: Stores static assets like CSS and JavaScript files, served directly by Spring.
-   **`resources/templates`**: Contains Thymeleaf HTML templates for server-side rendering.

This structure promotes modularity, making the codebase easier to understand, maintain, and extend.

---

# 📚 File Documentation

## 📁 OneDrive/Documents/codedocumentor (2)/codedocumentor-java/

### 📄 OneDrive/Documents/codedocumentor (2)/codedocumentor-java/README.md

**File Type**: MD

#### Purpose and Overview
This `README.md` file serves as the primary documentation for the Java/Spring Boot rewrite of the `codedocumentor` project. It provides a high-level overview of the project's purpose, its technological stack, prerequisites, configuration instructions, and how to run or build the application locally or with Docker. Crucially, it lists all exposed API endpoints, emphasizing their identical nature to the original Flask version to ensure frontend compatibility.

#### Key Sections
-   **Code Auto Documenter — Java / Spring Boot**: Introduces the project as a Java rewrite maintaining API compatibility.
-   **Tech stack**: A table comparing the original Flask stack with the current Spring Boot stack, highlighting key technology changes.
-   **Prerequisites**: Lists necessary software (JDK 17+, Maven 3.8+).
-   **Configuration**: Instructions for setting up environment variables using a `.env` file.
-   **Running locally**: Commands to start the application using Maven.
-   **Building a JAR**: Commands to package the application into an executable JAR.
-   **Docker**: Instructions for building and running a Docker image.
-   **API endpoints**: A comprehensive table detailing all HTTP methods, paths, and descriptions of the REST endpoints exposed by the application.

#### Dependencies
None directly within the Markdown file, but it documents the project's dependencies and configuration requirements.

#### Usage Examples
The `README.md` provides direct command-line usage examples for running, building, and containerizing the application:
```bash
# Running locally
mvn spring-boot:run

# Building a JAR
mvn package -DskipTests
java -jar target/codedocumentor-1.0.0.jar

# Docker
docker build -t codedocumentor-java .
docker run -p 8080:8080 --env-file .env codedocumentor-java
```

#### Code Comments
The `README.md` itself acts as a high-level comment for the entire project, explaining its design decisions (e.g., API compatibility) and operational instructions.

#### API Documentation
The "API endpoints" table serves as a concise API reference, listing:
-   **Method**: HTTP verb (GET, POST, etc.)
-   **Path**: The URL endpoint.
-   **Description**: A brief explanation of what the endpoint does.

### 📄 OneDrive/Documents/codedocumentor (2)/codedocumentor-java/pom.xml

**File Type**: XML

#### Purpose and Overview
The `pom.xml` (Project Object Model) is the core configuration file for Maven, the build automation tool used in this Spring Boot project. It defines the project's metadata, dependencies, build plugins, and overall structure. It specifies how the project is built, what libraries it relies on, and how it's packaged.

#### Key Elements
-   **`<modelVersion>`**: Specifies the version of the POM model being used (4.0.0 for Maven 2 and 3).
-   **`<parent>`**: Defines the parent POM, `spring-boot-starter-parent`, which provides sensible defaults for Spring Boot projects, including dependency management and plugin configurations.
-   **`<groupId>`, `<artifactId>`, `<version>`**: Unique identifiers for the project.
-   **`<name>`, `<description>`**: Human-readable metadata about the project.
-   **`<properties>`**: Defines project-wide properties, such as the Java version.
-   **`<dependencies>`**: Lists all external libraries (JARs) required by the project.
-   **`<build>`**: Configures the build process, including plugins.

#### Dependencies
The `pom.xml` explicitly lists all external libraries used by the project:

| GroupId                  | ArtifactId                 | Version | Scope    | Description                                   |
| :----------------------- | :------------------------- | :------ | :------- | :-------------------------------------------- |
| `org.springframework.boot` | `spring-boot-starter-web`  | (parent)|          | Core Spring Boot web application starter (Spring MVC, Tomcat) |
| `org.springframework.boot` | `spring-boot-starter-thymeleaf` | (parent)|          | Thymeleaf templating engine for views (replaces Jinja2) |
| `org.springframework.session` | `spring-session-core`      | (parent)|          | Provides core Spring Session functionalities (replaces Flask session) |
| `org.kohsuke`            | `github-api`               | 1.321   |          | Java client for the GitHub API (replaces PyGithub) |
| `com.squareup.okhttp3`   | `okhttp`                   | 4.12.0  |          | Efficient HTTP client for API calls (e.g., Gemini) |
| `com.fasterxml.jackson.core` | `jackson-databind`         | (parent)|          | High-performance JSON processor for Java |
| `io.github.cdimascio`    | `dotenv-java`              | 3.0.0   |          | Loads environment variables from `.env` files (replaces python-dotenv) |
| `org.springframework.boot` | `spring-boot-starter-test` | (parent)| `test`   | Starter for testing Spring Boot applications |

#### Build Configuration
-   **`spring-boot-maven-plugin`**: This plugin is essential for Spring Boot applications. It repackages the project JAR into an executable JAR, making it easy to run the application using `java -jar`. It also handles dependency resolution and classpath setup.

#### Usage Examples
The `pom.xml` is used implicitly by Maven commands:
-   `mvn clean install`: Cleans the project, compiles code, runs tests, and packages the application.
-   `mvn spring-boot:run`: Runs the Spring Boot application directly from Maven.
-   `mvn package -DskipTests`: Packages the application into a JAR, skipping tests.

#### Code Comments
The `pom.xml` includes XML comments explaining the purpose of certain dependencies and their Python counterparts in the original Flask project, aiding in understanding the migration.

### 📄 OneDrive/Documents/codedocumentor (2)/codedocumentor-java/run.bat

**File Type**: BAT

#### Purpose and Overview
The `run.bat` file is a simple Windows batch script designed to set up the necessary environment variables (`JAVA_HOME`, `PATH`) and then execute the Spring Boot application using Maven. It provides a convenient way to start the application on a Windows system without manually configuring the environment each time.

#### Key Commands
-   `@echo off`: Prevents commands from being displayed in the console.
-   `set JAVA_HOME=C:\Program Files\Java\jdk-25`: Sets the `JAVA_HOME` environment variable to the specified JDK installation path. This is crucial for Maven and Java applications to locate the Java runtime.
-   `set PATH=%JAVA_HOME%\bin;C:\tools\maven\bin;%PATH%`: Modifies the system's `PATH` environment variable to include the Java `bin` directory and the Maven `bin` directory, allowing `java` and `mvn` commands to be executed from any directory.
-   `C:\tools\maven\bin\mvn.cmd spring-boot:run`: Executes the Maven command to run the Spring Boot application. This command starts the embedded Tomcat server and deploys the application.

#### Dependencies
-   **JDK (Java Development Kit)**: The script assumes a JDK is installed at `C:\Program Files\Java\jdk-25`.
-   **Maven**: The script assumes Maven is installed at `C:\tools\maven`.

#### Usage Examples
To use this script, simply navigate to the `codedocumentor-java` directory in a Windows command prompt and execute:
```bash
run.bat
```
This will start the Spring Boot application, typically accessible at `http://localhost:8080`.

#### Code Comments
The script is self-commenting through its clear commands. It explicitly sets paths, which is a common practice in batch scripts for environment setup.

---

## 📁 OneDrive/Documents/codedocumentor (2)/codedocumentor-java/src/main/java/com/codedocumentor/

### 📄 OneDrive/Documents/codedocumentor (2)/codedocumentor-java/src/main/java/com/codedocumentor/CodeDocumentorApplication.java

**File Type**: JAVA

#### Purpose and Overview
This is the main entry point for the Spring Boot application. The `CodeDocumentorApplication` class is responsible for bootstrapping the entire Spring context, scanning for components, and launching the embedded web server. It's a standard Spring Boot application class.

#### Key Functions/Classes
-   **`CodeDocumentorApplication` class**:
    -   Annotated with `@SpringBootApplication`, which is a convenience annotation that combines:
        -   `@Configuration`: Tags the class as a source of bean definitions.
        -   `@EnableAutoConfiguration`: Tells Spring Boot to start adding beans based on classpath settings, other beans, and various property settings.
        -   `@ComponentScan`: Tells Spring to look for other components, configurations, and services in the `com.codedocumentor` package, allowing it to discover controllers, services, and other beans.
-   **`main(String[] args)` method**:
    -   The standard Java entry point.
    -   Calls `SpringApplication.run(CodeDocumentorApplication.class, args);` to start the Spring Boot application. This method creates and refreshes an appropriate `ApplicationContext`, which is the core of the Spring framework.

#### Dependencies
-   `org.springframework.boot.SpringApplication`: The class responsible for bootstrapping a Spring application.
-   `org.springframework.boot.autoconfigure.SpringBootApplication`: The main annotation for Spring Boot applications.

#### Usage Examples
This class is executed directly as a Java application. From the command line (after building with Maven):
```bash
java -jar target/codedocumentor-1.0.0.jar
```
Or via Maven:
```bash
mvn spring-boot:run
```

#### Code Comments
The class is minimal and self-explanatory for anyone familiar with Spring Boot. No complex logic requires additional comments within this file.

#### API Documentation
This class does not expose any public API in the traditional sense; it serves as the application's entry point.

---

## 📁 OneDrive/Documents/codedocumentor (2)/codedocumentor-java/src/main/java/com/codedocumentor/config/

### 📄 OneDrive/Documents/codedocumentor (2)/codedocumentor-java/src/main/java/com/codedocumentor/config/AppConfig.java

**File Type**: JAVA

#### Purpose and Overview
The `AppConfig` class provides application-wide configuration for the Spring Boot application. Its primary responsibilities are to load environment variables from a `.env` file (if present) and to configure static resource handling, mirroring common patterns from the original Flask application.

#### Key Functions/Classes
-   **`AppConfig` class**:
    -   Annotated with `@Configuration`, marking it as a source of bean definitions and configuration for the Spring application context.
    -   Implements `WebMvcConfigurer`, which provides callbacks to customize the Java-based configuration for Spring MVC.
-   **Static Initializer Block (`static {}`)**:
    -   This block executes once when the class is loaded.
    -   It uses the `dotenv-java` library to load environment variables from a `.env` file located in the working directory.
    -   It iterates through the loaded environment variables and sets them as system properties *if they are not already set as system environment variables*. This ensures that Spring's `@Value` annotations can pick up these configuration values, and that explicit environment variables take precedence over `.env` file values.
    -   Includes error handling to ignore `DotenvException` if the `.env` file is missing, making its presence optional.
-   **`addResourceHandlers(ResourceHandlerRegistry registry)` method**:
    -   Overrides a method from `WebMvcConfigurer`.
    -   Configures a resource handler to serve static content. Specifically, it maps requests to `/static/**` URLs to resources found in the `classpath:/static/` directory. This mirrors how Flask typically serves static files.

#### Dependencies
-   `io.github.cdimascio.dotenv.Dotenv`: Used for loading environment variables from a `.env` file.
-   `org.springframework.context.annotation.Configuration`: Spring annotation to mark a class as a configuration source.
-   `org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry`: Used to configure static resource handling.
-   `org.springframework.web.servlet.config.annotation.WebMvcConfigurer`: Interface for customizing Spring MVC configuration.

#### Usage Examples
-   **Environment Variable Loading**: By placing a `.env` file in the project's root directory (e.g., `GEMINI_API_KEY=your_key`), these values will be automatically loaded and made available to Spring components via `@Value("${GEMINI_API_KEY}")`.
-   **Static Resource Serving**: Frontend assets like `styles.css` or `script.js` placed in `src/main/resources/static/css/` and `src/main/resources/static/js/` respectively, can be accessed in HTML templates using paths like `/static/css/styles.css`.

#### Code Comments
-   The static initializer block includes comments explaining its purpose: "Load .env file from the working directory (mirrors Python's load_dotenv())" and "`.env` is optional; environment variables take precedence."
-   The `addResourceHandlers` method also clarifies its intent: "Serve `/static/**` from classpath (mirrors Flask's static folder)."

#### API Documentation
This class primarily configures the application and does not expose a public API for direct consumption by other services within the application. Its methods are part of the Spring framework's configuration lifecycle.

---

## 📁 OneDrive/Documents/codedocumentor (2)/codedocumentor-java/src/main/java/com/codedocumentor/controller/

### 📄 OneDrive/Documents/codedocumentor (2)/codedocumentor-java/src/main/java/com/codedocumentor/controller/GitHubAuthController.java

**File Type**: JAVA

#### Purpose and Overview
The `GitHubAuthController` class is a Spring MVC controller responsible for handling all GitHub-related authentication and API interactions. It manages the OAuth 2.0 flow with GitHub, allowing users to log in, fetch their repositories, and commit documentation. The routes are designed to be identical to the original Flask application's GitHub endpoints for seamless frontend compatibility.

#### Key Functions/Classes
-   **`GitHubAuthController` class**:
    -   Annotated with `@Controller`, indicating it's a Spring MVC controller.
    -   Uses `org.slf4j.Logger` for logging events and errors.
    -   Injects `GitHubService` to delegate actual GitHub API calls and OAuth logic.
-   **`SESSION_GITHUB_TOKEN`, `SESSION_GITHUB_STATE`, `SESSION_GITHUB_USER`**: Static final strings used as keys for storing GitHub-related data in the HTTP session.
-   **`githubAuth(HttpSession session)`**: Initiates the GitHub OAuth flow by generating a unique state, storing it in the session, and redirecting the user to GitHub's authorization page.
-   **`githubCallback(String code, String state, HttpSession session)`**: Handles the callback from GitHub after user authorization. It validates the `state` parameter, exchanges the authorization `code` for an access token using `GitHubService`, fetches user information, stores the token and user details in the session, and redirects to the home page.
-   **`githubLogout(HttpSession session)`**: Clears all GitHub-related session attributes and redirects the user to the home page, effectively logging them out of GitHub within the application.
-   **`getRepositories(HttpSession session)`**: Returns a JSON list of the authenticated user's GitHub repositories. It retrieves the access token from the session and uses `GitHubService` to fetch the repository data.
-   **`commitDocumentation(Map<String, String> body, HttpSession session)`**: Handles committing generated documentation to a specified GitHub repository. It retrieves the access token, repository name, documentation content, commit message, and branch from the request body and session, then uses `GitHubService` to perform the commit.

#### Dependencies
-   `com.codedocumentor.model.GitHubUser`: Model for storing GitHub user information.
-   `com.codedocumentor.service.GitHubService`: Service layer for GitHub API interactions.
-   `jakarta.servlet.http.HttpSession`: For managing user session data.
-   `org.slf4j.Logger`, `org.slf4j.LoggerFactory`: For logging.
-   `org.springframework.http.MediaType`, `org.springframework.http.ResponseEntity`: For building HTTP responses.
-   `org.springframework.stereotype.Controller`, `org.springframework.web.bind.annotation.*`: Spring MVC annotations for defining controllers and request mappings.

#### Usage Examples
-   **Initiate OAuth**: A user navigates to `/auth/github`.
-   **OAuth Callback**: GitHub redirects to `/auth/github/callback?code=...&state=...`.
-   **Logout**: A user navigates to `/auth/github/logout`.
-   **Fetch Repositories**: A frontend JavaScript client makes a `GET` request to `/api/github/repositories`.
-   **Commit Documentation**: A frontend JavaScript client makes a `POST` request to `/api/github/commit-documentation` with a JSON body containing `repository`, `documentation`, `commit_message`, and `branch`.

#### Code Comments
-   Class-level comment: "Handles the GitHub OAuth dance and repository API calls. Mirrors Flask routes..."
-   Method-level comments: Each method explicitly states which Python Flask route it mirrors, aiding in understanding the migration.
-   Session attribute keys are commented as mirroring Python session dict keys.
-   Logic for state validation and error handling in `githubCallback` is clear.

#### API Documentation

##### `githubAuth(HttpSession session)`
-   **Purpose**: Redirects the user to GitHub's authorization page to initiate the OAuth flow.
-   **Parameters**:
    -   `session`: `HttpSession` - The current HTTP session to store the OAuth state.
-   **Returns**: `String` - A redirect URL to GitHub's authorization page.
-   **Exceptions**: None explicitly thrown, but underlying `UUID.randomUUID()` could theoretically throw `UnsupportedOperationException` (highly unlikely in practice).

##### `githubCallback(String code, String state, HttpSession session)`
-   **Purpose**: Handles the callback from GitHub after the user authorizes the application. Exchanges the authorization code for an access token and stores user info in the session.
-   **Parameters**:
    -   `code`: `String` (optional, `@RequestParam`) - The authorization code provided by GitHub.
    -   `state`: `String` (optional, `@RequestParam`) - The state parameter for CSRF protection, echoed back by GitHub.
    -   `session`: `HttpSession` - The current HTTP session.
-   **Returns**: `String` - A redirect URL to the home page (`/`) or an error page (`/?error=...`).
-   **Exceptions**:
    -   `IOException`: If the token exchange or user info fetching fails (handled internally, redirects to error page).

##### `githubLogout(HttpSession session)`
-   **Purpose**: Clears all GitHub-related data from the session, effectively logging out the user from GitHub within the application.
-   **Parameters**:
    -   `session`: `HttpSession` - The current HTTP session.
-   **Returns**: `String` - A redirect URL to the home page (`/`).
-   **Exceptions**: None.

##### `getRepositories(HttpSession session)`
-   **Purpose**: Retrieves and returns a JSON list of repositories for the authenticated GitHub user.
-   **Parameters**:
    -   `session`: `HttpSession` - The current HTTP session, expected to contain the GitHub access token.
-   **Returns**: `ResponseEntity<Map<String, Object>>` - A JSON response containing a list of repositories or an error message.
    -   **Success (200 OK)**: `{"repositories": [...]}`
    -   **Error (401 Unauthorized)**: `{"error": "Not authenticated with GitHub"}`
    -   **Error (500 Internal Server Error)**: `{"error": "Failed to fetch repositories: ..."}`
-   **Exceptions**:
    -   `IOException`: If fetching repositories from GitHub API fails (handled internally, returns 500).

##### `commitDocumentation(Map<String, String> body, HttpSession session)`
-   **Purpose**: Commits (creates or updates) generated documentation as `README.md` to a specified GitHub repository.
-   **Parameters**:
    -   `body`: `Map<String, String>` (request body) - Contains:
        -   `repository`: `String` (required) - Full name of the repository (e.g., "owner/repo").
        -   `documentation`: `String` (required) - The Markdown content of the documentation.
        -   `commit_message`: `String` (optional, default: "Update project documentation") - The commit message.
        -   `branch`: `String` (optional, default: "main") - The target branch.
    -   `session`: `HttpSession` - The current HTTP session, expected to contain the GitHub access token.
-   **Returns**: `ResponseEntity<Map<String, Object>>` - A JSON response indicating success or failure.
    -   **Success (200 OK)**: `{"success": true, "message": "...", "repository": "...", "branch": "...", "html_url": "...", "action": "created/updated"}`
    -   **Error (401 Unauthorized)**: `{"error": "Not authenticated with GitHub"}`
    -   **Error (400 Bad Request)**: `{"error": "Repository name and documentation are required"}`
    -   **Error (500 Internal Server Error)**: `{"error": "Failed to commit documentation: ..."}`
-   **Exceptions**:
    -   `IOException`: If the GitHub API call for committing fails (handled internally, returns 500).

### 📄 OneDrive/Documents/codedocumentor (2)/codedocumentor-java/src/main/java/com/codedocumentor/controller/MainController.java

**File Type**: JAVA

#### Purpose and Overview
The `MainController` is the central Spring MVC controller for the Code Auto Documenter application. It handles the primary user interactions, including serving the main UI, processing file uploads, analyzing GitHub repositories, generating documentation using the Gemini AI, providing a chatbot interface, and managing documentation downloads. It also includes webhook endpoints for SalesIQ and Zobot integrations. The routes are designed to be identical to the original Flask application's endpoints to ensure seamless frontend compatibility.

#### Key Functions/Classes
-   **`MainController` class**:
    -   Annotated with `@Controller`, marking it as a Spring MVC controller.
    -   Uses `org.slf4j.Logger` for logging.
    -   Injects `FileProcessingService`, `GeminiService`, `GitHubService`, and `DownloadStore` to delegate specific business logic.
-   **`index()`**: Renders the main `index_with_github.html` Thymeleaf template, serving as the application's home page.
-   **`generateDocumentation(List<MultipartFile> files)`**: Handles file and ZIP uploads. It processes the uploaded files using `FileProcessingService`, then calls `GeminiService` to generate documentation, returning the result as JSON.
-   **`analyzeGithub(Map<String, String> body)`**: Processes requests to analyze a GitHub repository. It clones the repository using `GitHubService`, extracts code files, and then uses `GeminiService` to generate documentation, returning the result as JSON.
-   **`downloadDocumentation(Map<String, String> body)`**: Echoes the provided documentation back to the client as JSON. This is primarily used by the frontend JavaScript to trigger a client-side download.
-   **`downloadMarkdown(String token, HttpServletResponse response)`**: Serves a previously stored documentation blob (identified by a `token`) as a downloadable Markdown file. It retrieves the content from `DownloadStore`.
-   **`chatbot(Map<String, String> body)`**: Handles chatbot requests. It takes a user's question and the project documentation as context, then uses `GeminiService` to generate an answer, returning it as JSON.
-   **`salesIqWebhook(Map<String, Object> payload)`**: A flexible webhook endpoint for SalesIQ integration. It responds to "render" or "open" actions with an initial widget layout, and to "generate" or "submit" actions by cloning a GitHub repository, generating documentation, and providing a download link.
-   **`zobotAnalyzeRepo(Map<String, Object> body)`**: A webhook endpoint for Zobot integration. It clones a GitHub repository, generates documentation, extracts a summary, key modules, and key files, and provides a download URL, returning all this information as JSON.
-   **`badRequest(String message)` / `serverError(String message)`**: Helper methods to create consistent `ResponseEntity` objects for error responses.
-   **`scoreFile(String name, String[] keywords)`**: Private helper for Zobot, scores a file based on keywords and path depth to identify "key files."
-   **`extractSummary(String documentation)`**: Private helper for Zobot, extracts a concise summary from the generated Markdown documentation.

#### Dependencies
-   `com.codedocumentor.model.FileContent`: Model for representing extracted code files.
-   `com.codedocumentor.service.*`: Injects `FileProcessingService`, `GeminiService`, `GitHubService`, `DownloadStore`.
-   `jakarta.servlet.http.HttpServletResponse`: For direct control over HTTP responses (e.g., file downloads).
-   `org.slf4j.Logger`, `org.slf4j.LoggerFactory`: For logging.
-   `org.springframework.http.MediaType`, `org.springframework.http.ResponseEntity`: For building HTTP responses.
-   `org.springframework.stereotype.Controller`, `org.springframework.web.bind.annotation.*`: Spring MVC annotations.
-   `org.springframework.web.multipart.MultipartFile`: For handling file uploads.

#### Usage Examples
-   **Access Home Page**: `GET /`
-   **Upload Files**: `POST /generate-documentation` with `multipart/form-data` containing `files`.
-   **Analyze GitHub Repo**: `POST /analyze-github` with JSON body `{"repo_url": "..."}`.
-   **Download Documentation (Client-side trigger)**: `POST /download-documentation` with JSON body `{"documentation": "..."}`.
-   **Download Markdown File (Server-side)**: `GET /download/{token}` where `{token}` is obtained from `DownloadStore`.
-   **Chatbot Query**: `POST /chatbot` with JSON body `{"question": "...", "documentation": "..."}`.
-   **SalesIQ Webhook**: `POST /webhooks/salesiq` with various JSON payloads for `action` (e.g., "render", "generate").
-   **Zobot Webhook**: `POST /zobot/analyze-repo` with JSON body `{"repo_url": "..."}`.

#### Code Comments
-   Class-level comment: "Primary Spring MVC controller. Maps the same URLs as the Flask app..." and lists mirrored routes.
-   Method-level comments: Each method explicitly states which Flask route it mirrors.
-   Detailed comments on the logic for SalesIQ and Zobot webhooks, including how data is extracted and processed.
-   Explanations for helper methods like `scoreFile` and `extractSummary`.

#### API Documentation

##### `index()`
-   **Purpose**: Renders the main application page.
-   **Parameters**: None.
-   **Returns**: `String` - The name of the Thymeleaf template (`index_with_github`).
-   **Exceptions**: None.

##### `generateDocumentation(List<MultipartFile> files)`
-   **Purpose**: Accepts uploaded files (or ZIP archives), processes them, and generates documentation using Gemini AI.
-   **Parameters**:
    -   `files`: `List<MultipartFile>` - A list of uploaded files.
-   **Returns**: `ResponseEntity<Map<String, Object>>` - A JSON response.
    -   **Success (200 OK)**: `{"success": true, "documentation": "...", "files_processed": N}`
    -   **Error (400 Bad Request)**: `{"error": "..."}`
    -   **Error (500 Internal Server Error)**: `{"error": "..."}`
-   **Exceptions**:
    -   `IOException`: During file processing (handled internally, returns 400).
    -   `Exception`: During Gemini documentation generation (handled internally, returns 500).

##### `analyzeGithub(Map<String, String> body)`
-   **Purpose**: Clones a GitHub repository, extracts code files, and generates documentation using Gemini AI.
-   **Parameters**:
    -   `body`: `Map<String, String>` (request body) - Contains:
        -   `repo_url`: `String` (required) - The URL of the GitHub repository.
-   **Returns**: `ResponseEntity<Map<String, Object>>` - A JSON response.
    -   **Success (200 OK)**: `{"success": true, "documentation": "...", "files_processed": N, "repo_url": "..."}`
    -   **Error (400 Bad Request)**: `{"error": "..."}`
    -   **Error (500 Internal Server Error)**: `{"error": "..."}`
-   **Exceptions**:
    -   `Exception`: During GitHub cloning or Gemini documentation generation (handled internally, returns 500).

##### `downloadDocumentation(Map<String, String> body)`
-   **Purpose**: Returns the provided documentation string as a JSON response, primarily for client-side download initiation.
-   **Parameters**:
    -   `body`: `Map<String, String>` (request body) - Contains:
        -   `documentation`: `String` (required) - The documentation content.
-   **Returns**: `ResponseEntity<Map<String, Object>>` - A JSON response.
    -   **Success (200 OK)**: `{"success": true, "documentation": "...", "filename": "project-documentation.md"}`
    -   **Error (400 Bad Request)**: `{"error": "No documentation provided"}`
-   **Exceptions**: None.

##### `downloadMarkdown(@PathVariable String token, HttpServletResponse response)`
-   **Purpose**: Serves a stored Markdown documentation file for download using a one-time token.
-   **Parameters**:
    -   `token`: `String` (path variable) - The unique token identifying the documentation.
    -   `response`: `HttpServletResponse` - The HTTP response object to write the file content.
-   **Returns**: `void` - Writes the file directly to the response stream.
-   **Exceptions**:
    -   `IOException`: If the token is invalid/expired (sends 404 error) or during writing to the response stream.

##### `chatbot(Map<String, String> body)`
-   **Purpose**: Answers a user's question about the project based on provided documentation context using Gemini AI.
-   **Parameters**:
    -   `body`: `Map<String, String>` (request body) - Contains:
        -   `question`: `String` (required) - The user's question.
        -   `documentation`: `String` (required) - The project documentation as context.
-   **Returns**: `ResponseEntity<Map<String, Object>>` - A JSON response.
    -   **Success (200 OK)**: `{"success": true, "answer": "..."}`
    -   **Error (400 Bad Request)**: `{"error": "..."}`
    -   **Error (500 Internal Server Error)**: `{"error": "..."}`
-   **Exceptions**:
    -   `Exception`: During Gemini AI processing (handled internally, returns 500).

##### `salesIqWebhook(@RequestBody(required = false) Map<String, Object> payload)`
-   **Purpose**: Handles webhook requests from SalesIQ, providing widget layouts or generating documentation based on the action.
-   **Parameters**:
    -   `payload`: `Map<String, Object>` (request body, optional) - The JSON payload from SalesIQ, containing `action` or `event` and potentially `data` with `repo_url`.
-   **Returns**: `ResponseEntity<Map<String, Object>>` - A JSON response representing a SalesIQ widget layout (initial, error, or success with download link).
-   **Exceptions**:
    -   `Exception`: During GitHub cloning or Gemini documentation generation (handled internally, returns a widget layout with an error message).

##### `zobotAnalyzeRepo(@RequestBody(required = false) Map<String, Object> body)`
-   **Purpose**: Handles webhook requests from Zobot to analyze a GitHub repository, generate documentation, and return a structured summary.
-   **Parameters**:
    -   `body`: `Map<String, Object>` (request body, optional) - The JSON payload from Zobot, expected to contain `repo_url`.
-   **Returns**: `ResponseEntity<Map<String, Object>>` - A JSON response.
    -   **Success (200 OK)**: `{"status": "success", "summary": "...", "modules": [...], "key_files": [...], "download_url": "..."}`
    -   **Error (400 Bad Request)**: `{"status": "error", "message": "Missing 'repo_url'..."}`
    -   **Error (500 Internal Server Error)**: `{"status": "error", "message": "..."}`
-   **Exceptions**:
    -   `Exception`: During GitHub cloning or Gemini documentation generation (handled internally, returns 500).

---

## 📁 OneDrive/Documents/codedocumentor (2)/codedocumentor-java/src/main/java/com/codedocumentor/model/

### 📄 OneDrive/Documents/codedocumentor (2)/codedocumentor-java/src/main/java/com/codedocumentor/model/FileContent.java

**File Type**: JAVA

#### Purpose and Overview
The `FileContent` class is a simple Plain Old Java Object (POJO) that represents a single code file extracted from an upload (e.g., a ZIP archive) or a GitHub repository clone. It encapsulates the essential information needed for documentation generation: the file's name (path), its content, and its extension. This class mirrors a dictionary structure used in the original Python project.

#### Key Functions/Classes
-   **`FileContent` class**:
    -   Contains three private fields: `name`, `content`, and `extension`.
    -   Provides a default no-argument constructor and a parameterized constructor for easy instantiation.
    -   Includes standard public getter methods (`getName()`, `getContent()`, `getExtension()`) to access the field values.
    -   Includes standard public setter methods (`setName()`, `setContent()`, `setExtension()`) to modify the field values.
    -   Implements a **Builder pattern** (`FileContent.Builder`) to facilitate more readable and flexible object creation, especially when dealing with optional fields or a large number of parameters. This replaces the `@Builder` annotation commonly used with Lombok.

#### Dependencies
None, this is a pure data model class.

#### Usage Examples
Creating a `FileContent` object using the builder pattern:
```java
FileContent codeFile = FileContent.builder()
    .name("src/main/java/com/example/App.java")
    .content("package com.example;\n\npublic class App { ... }")
    .extension("java")
    .build();

String fileName = codeFile.getName(); // "src/main/java/com/example/App.java"
String fileContent = codeFile.getContent(); // "package com.example;..."
```

#### Code Comments
-   Class-level comment: "Represents one code file extracted from an upload or GitHub clone. Mirrors the Python dict: `{'name': ..., 'content': ..., 'extension': ...}`"
-   Field-level comments: Explain the purpose and format of each field (`name`, `content`, `extension`).
-   Builder pattern comment: "(replaces Lombok @Builder)"

#### API Documentation

##### `FileContent()`
-   **Purpose**: Default constructor.

##### `FileContent(String name, String content, String extension)`
-   **Purpose**: Constructor to initialize all fields.
-   **Parameters**:
    -   `name`: `String` - Relative file path.
    -   `content`: `String` - Full UTF-8 text content.
    -   `extension`: `String` - Lowercase extension without dot.

##### `getName()`
-   **Purpose**: Returns the relative file path.
-   **Returns**: `String` - The file name.

##### `getContent()`
-   **Purpose**: Returns the full UTF-8 text content of the file.
-   **Returns**: `String` - The file content.

##### `getExtension()`
-   **Purpose**: Returns the lowercase file extension without the dot.
-   **Returns**: `String` - The file extension.

##### `setName(String name)`
-   **Purpose**: Sets the relative file path.
-   **Parameters**:
    -   `name`: `String` - The new file name.

##### `setContent(String content)`
-   **Purpose**: Sets the full UTF-8 text content of the file.
-   **Parameters**:
    -   `content`: `String` - The new file content.

##### `setExtension(String extension)`
-   **Purpose**: Sets the lowercase file extension.
-   **Parameters**:
    -   `extension`: `String` - The new file extension.

##### `builder()`
-   **Purpose**: Static factory method to create a new `Builder` instance for `FileContent`.
-   **Returns**: `FileContent.Builder` - A new builder instance.

##### `FileContent.Builder` class
-   **Purpose**: Provides a fluent API for constructing `FileContent` objects.
-   **Methods**:
    -   `name(String name)`: Sets the `name` field. Returns `this` (Builder).
    -   `content(String content)`: Sets the `content` field. Returns `this` (Builder).
    -   `extension(String extension)`: Sets the `extension` field. Returns `this` (Builder).
    -   `build()`: Constructs and returns a new `FileContent` object with the configured values.

### 📄 OneDrive/Documents/codedocumentor (2)/codedocumentor-java/src/main/java/com/codedocumentor/model/GitHubUser.java

**File Type**: JAVA

#### Purpose and Overview
The `GitHubUser` class is a lightweight Plain Old Java Object (POJO) designed to represent essential information about an authenticated GitHub user. This model is primarily used to store user details in the HTTP session after a successful GitHub OAuth login, mirroring a dictionary structure used in the original Python project. It avoids storing sensitive information like access tokens directly within the user object.

#### Key Functions/Classes
-   **`GitHubUser` class**:
    -   Contains four private fields: `login`, `name`, `email`, and `avatarUrl`.
    -   Provides a default no-argument constructor and a parameterized constructor for easy instantiation.
    -   Includes standard public getter methods (`getLogin()`, `getName()`, `getEmail()`, `getAvatarUrl()`) to access the field values.
    -   Includes standard public setter methods (`setLogin()`, `setName()`, `setEmail()`, `setAvatarUrl()`) to modify the field values.
    -   Implements a **Builder pattern** (`GitHubUser.Builder`) to facilitate more readable and flexible object creation, especially useful when constructing objects from API responses. This replaces the `@Builder` annotation commonly used with Lombok.

#### Dependencies
None, this is a pure data model class.

#### Usage Examples
Creating a `GitHubUser` object using the builder pattern:
```java
GitHubUser user = GitHubUser.builder()
    .login("octocat")
    .name("The Octocat")
    .email("octocat@github.com")
    .avatarUrl("https://avatars.githubusercontent.com/u/583231?v=4")
    .build();

String username = user.getLogin(); // "octocat"
String avatar = user.getAvatarUrl(); // "https://avatars.githubusercontent.com/u/583231?v=4"
```

#### Code Comments
-   Class-level comment: "Lightweight representation of the authenticated GitHub user stored in the HTTP session. Mirrors the Python session dict: `{'login': ..., 'name': ..., 'email': ..., 'avatar_url': ...}`"
-   Builder pattern comment: "(replaces Lombok @Builder)"

#### API Documentation

##### `GitHubUser()`
-   **Purpose**: Default constructor.

##### `GitHubUser(String login, String name, String email, String avatarUrl)`
-   **Purpose**: Constructor to initialize all fields.
-   **Parameters**:
    -   `login`: `String` - The GitHub login username.
    -   `name`: `String` - The user's full name.
    -   `email`: `String` - The user's primary email.
    -   `avatarUrl`: `String` - URL to the user's avatar image.

##### `getLogin()`
-   **Purpose**: Returns the GitHub login username.
-   **Returns**: `String` - The login username.

##### `getName()`
-   **Purpose**: Returns the user's full name.
-   **Returns**: `String` - The user's name.

##### `getEmail()`
-   **Purpose**: Returns the user's primary email.
-   **Returns**: `String` - The user's email.

##### `getAvatarUrl()`
-   **Purpose**: Returns the URL to the user's avatar image.
-   **Returns**: `String` - The avatar URL.

##### `setLogin(String login)`
-   **Purpose**: Sets the GitHub login username.
-   **Parameters**:
    -   `login`: `String` - The new login username.

##### `setName(String name)`
-   **Purpose**: Sets the user's full name.
-   **Parameters**:
    -   `name`: `String` - The new name.

##### `setEmail(String email)`
-   **Purpose**: Sets the user's primary email.
-   **Parameters**:
    -   `email`: `String` - The new email.

##### `setAvatarUrl(String avatarUrl)`
-   **Purpose**: Sets the URL to the user's avatar image.
-   **Parameters**:
    -   `avatarUrl`: `String` - The new avatar URL.

##### `builder()`
-   **Purpose**: Static factory method to create a new `Builder` instance for `GitHubUser`.
-   **Returns**: `GitHubUser.Builder` - A new builder instance.

##### `GitHubUser.Builder` class
-   **Purpose**: Provides a fluent API for constructing `GitHubUser` objects.
-   **Methods**:
    -   `login(String login)`: Sets the `login` field. Returns `this` (Builder).
    -   `name(String name)`: Sets the `name` field. Returns `this` (Builder).
    -   `email(String email)`: Sets the `email` field. Returns `this` (Builder).
    -   `avatarUrl(String avatarUrl)`: Sets the `avatarUrl` field. Returns `this` (Builder).
    -   `build()`: Constructs and returns a new `GitHubUser` object with the configured values.

---

## 📁 OneDrive/Documents/codedocumentor (2)/codedocumentor-java/src/main/java/com/codedocumentor/service/

### 📄 OneDrive/Documents/codedocumentor (2)/codedocumentor-java/src/main/java/com/codedocumentor/service/DownloadStore.java

**File Type**: JAVA

#### Purpose and Overview
The `DownloadStore` service provides a simple, in-memory mechanism to temporarily store generated documentation content and make it available for download via a unique, one-time token. This mirrors the `DOWNLOADS` dictionary and the `/download/<token>` route functionality from the original Python Flask application. It's designed for simplicity, acknowledging that a production environment might require more robust, persistent, and expiring storage (e.g., Redis).

#### Key Functions/Classes
-   **`DownloadStore` class**:
    -   Annotated with `@Service`, marking it as a Spring service component.
    -   Manages a `ConcurrentHashMap<String, String>` named `store` to hold tokens and their corresponding documentation content. `ConcurrentHashMap` is chosen for thread-safety in a multi-threaded environment.
-   **`save(String documentation)`**: Stores the provided documentation content and generates a unique `UUID` as a token for retrieval.
-   **`get(String token)`**: Retrieves the documentation content associated with a given token.
-   **`remove(String token)`**: Removes a token and its associated content from the store. This is useful for implementing one-time download links.

#### Dependencies
-   `org.springframework.stereotype.Service`: Spring annotation to mark this class as a service.
-   `java.util.UUID`: For generating unique tokens.
-   `java.util.concurrent.ConcurrentHashMap`: For thread-safe in-memory storage.

#### Usage Examples
1.  **Saving documentation**:
    ```java
    String generatedDoc = "## My Project\nThis is my documentation.";
    String downloadToken = downloadStore.save(generatedDoc);
    // downloadToken can then be used to construct a URL like "/download/" + downloadToken
    ```
2.  **Retrieving documentation**:
    ```java
    String content = downloadStore.get(downloadToken);
    // content will be "## My Project\nThis is my documentation." or null if token not found
    ```
3.  **Removing documentation (e.g., after download)**:
    ```java
    downloadStore.remove(downloadToken);
    ```

#### Code Comments
-   Class-level comment: "In-memory store for generated documentation blobs... Mirrors Python's DOWNLOADS dict... NOTE: This is intentionally simple (in-memory). In production you would want expiry and possibly Redis-backed storage." This clearly explains the design choice and potential improvements.

#### API Documentation

##### `save(String documentation)`
-   **Purpose**: Stores the given documentation content and returns a unique token for later retrieval.
-   **Parameters**:
    -   `documentation`: `String` - The Markdown documentation content to be stored.
-   **Returns**: `String` - A unique `UUID` token that can be used to retrieve the documentation.
-   **Exceptions**: None.

##### `get(String token)`
-   **Purpose**: Retrieves the documentation content associated with the provided token.
-   **Parameters**:
    -   `token`: `String` - The unique token generated by `save()`.
-   **Returns**: `String` - The stored documentation content, or `null` if the token is not found.
-   **Exceptions**: None.

##### `remove(String token)`
-   **Purpose**: Removes the documentation content associated with the provided token from the store.
-   **Parameters**:
    -   `token`: `String` - The token to remove.
-   **Returns**: `void`
-   **Exceptions**: None.

### 📄 OneDrive/Documents/codedocumentor (2)/codedocumentor-java/src/main/java/com/codedocumentor/service/FileProcessingService.java

**File Type**: JAVA

#### Purpose and Overview
The `FileProcessingService` is responsible for handling the extraction and filtering of code files from various sources, primarily uploaded `MultipartFile` objects (which can be individual files or ZIP archives). It implements logic to identify supported file types and ignore common development-related directories and files, mirroring the `allowed_file()`, `should_ignore_path()`, and `extract_files_from_zip()` helpers from the original Python project.

#### Key Functions/Classes
-   **`FileProcessingService` class**:
    -   Annotated with `@Service`, marking it as a Spring service component.
    -   Uses `org.slf4j.Logger` for logging.
    -   **`SUPPORTED_EXTENSIONS`**: A `Set<String>` containing all file extensions that the application is configured to process (e.g., "js", "py", "java", "md", "json"). This mirrors the Python `SUPPORTED_EXTENSIONS` set.
    -   **`IGNORE_PATTERNS`**: A `Set<String>` containing directory and file names to be ignored during processing (e.g., ".git", "node_modules", ".env"). This mirrors the Python `IGNORE_PATTERNS` set.
-   **`processUploadedFile(MultipartFile file)`**: The main entry point for processing an uploaded file. It checks if the file is a ZIP archive; if so, it calls `extractFilesFromZip`. Otherwise, it reads the file content directly, checks if it's an allowed file, and returns it as a `FileContent` object.
-   **`extractFilesFromZip(InputStream zipInputStream)`**: Extracts all supported code files from a given `ZipInputStream`. It iterates through ZIP entries, skips directories and ignored paths, reads file content, and creates `FileContent` objects. It handles UTF-8 decoding and logs non-UTF-8 files.
-   **`isAllowedFile(String filename)`**: Determines if a file's extension is among the `SUPPORTED_EXTENSIONS`.
-   **`shouldIgnorePath(String path)`**: Checks if any segment of a given file path matches an `IGNORE_PATTERNS` entry.
-   **`getExtension(String filename)`**: Extracts the lowercase file extension from a filename.

#### Dependencies
-   `com.codedocumentor.model.FileContent`: For creating objects to hold extracted file data.
-   `org.slf4j.Logger`, `org.slf4j.LoggerFactory`: For logging.
-   `org.springframework.stereotype.Service`: Spring annotation.
-   `org.springframework.web.multipart.MultipartFile`: For handling uploaded files.
-   `java.io.*`, `java.nio.charset.StandardCharsets`, `java.nio.file.*`, `java.util.*`, `java.util.zip.*`: Standard Java libraries for file I/O, character encoding, path manipulation, collections, and ZIP archive handling.

#### Usage Examples
-   **Processing an uploaded file**:
    ```java
    // In a controller method:
    @PostMapping("/upload")
    public ResponseEntity<List<FileContent>> handleFileUpload(@RequestParam("file") MultipartFile file) {
        try {
            List<FileContent> files = fileProcessingService.processUploadedFile(file);
            return ResponseEntity.ok(files);
        } catch (IOException e) {
            return ResponseEntity.status(500).build();
        }
    }
    ```
-   **Extracting from a ZIP stream (internal use, e.g., by GitHubService)**:
    ```java
    InputStream zipStream = new FileInputStream("my_project.zip");
    List<FileContent> extractedFiles = fileProcessingService.extractFilesFromZip(zipStream);
    ```

#### Code Comments
-   Class-level comment: "Handles file extraction from uploaded MultipartFiles and ZIP archives. Mirrors the Python helpers..."
-   Field-level comments: Explain `SUPPORTED_EXTENSIONS` and `IGNORE_PATTERNS` and their Python counterparts.
-   Method-level comments: Explain the purpose of each public method and which Python function it mirrors.
-   Internal logging for skipped files (e.g., non-UTF8 or ignored paths).

#### API Documentation

##### `processUploadedFile(MultipartFile file)`
-   **Purpose**: Processes a single uploaded file. If it's a ZIP, it extracts contents; otherwise, it reads the file directly.
-   **Parameters**:
    -   `file`: `MultipartFile` - The uploaded file.
-   **Returns**: `List<FileContent>` - A list of extracted `FileContent` objects. Returns an empty list if the file is not allowed, empty, or cannot be processed.
-   **Exceptions**:
    -   `IOException`: If there are errors reading the file's input stream or bytes.

##### `extractFilesFromZip(InputStream zipInputStream)`
-   **Purpose**: Extracts all supported code files from a ZIP input stream, ignoring specified patterns.
-   **Parameters**:
    -   `zipInputStream`: `InputStream` - The input stream of a ZIP file.
-   **Returns**: `List<FileContent>` - A list of extracted `FileContent` objects.
-   **Exceptions**:
    -   `IOException`: If there are errors reading from the ZIP input stream.

##### `isAllowedFile(String filename)`
-   **Purpose**: Checks if a file's extension is in the predefined set of supported extensions.
-   **Parameters**:
    -   `filename`: `String` - The name of the file.
-   **Returns**: `boolean` - `true` if the file extension is supported, `false` otherwise.
-   **Exceptions**: None.

##### `shouldIgnorePath(String path)`
-   **Purpose**: Checks if any part of the given path matches a predefined ignore pattern (e.g., `.git`, `node_modules`).
-   **Parameters**:
    -   `path`: `String` - The file or directory path.
-   **Returns**: `boolean` - `true` if the path should be ignored, `false` otherwise.
-   **Exceptions**: None.

##### `getExtension(String filename)`
-   **Purpose**: Extracts the lowercase file extension from a filename.
-   **Parameters**:
    -   `filename`: `String` - The name of the file.
-   **Returns**: `String` - The lowercase extension without the leading dot, or an empty string if no extension is found.
-   **Exceptions**: None.

### 📄 OneDrive/Documents/codedocumentor (2)/codedocumentor-java/src/main/java/com/codedocumentor/service/GeminiService.java

**File Type**: JAVA

#### Purpose and Overview
The `GeminiService` class acts as a wrapper for interacting with the Google Gemini generative language REST API. Its core functionality involves constructing prompts from code files or user questions and sending these prompts to the Gemini API to generate documentation or chatbot answers. It mirrors the `call_gemini_api()` and `create_documentation_prompt()` functions from the original Python project.

#### Key Functions/Classes
-   **`GeminiService` class**:
    -   Annotated with `@Service`, marking it as a Spring service component.
    -   Uses `org.slf4j.Logger` for logging.
    -   `@Value("${GEMINI_API_KEY:}")`: Injects the Gemini API key from environment variables or `application.properties`. The `: ` provides a default empty string if the key is not found.
    -   **`httpClient`**: An `OkHttpClient` instance configured with timeouts for robust network communication.
    -   **`objectMapper`**: An `ObjectMapper` from Jackson for JSON serialization and deserialization.
-   **`generateDocumentation(List<FileContent> files)`**: The main method for generating project documentation. It takes a list of `FileContent` objects, constructs a detailed prompt using `createDocumentationPrompt`, and sends it to the Gemini API via `callGeminiApi`.
-   **`answerChatbotQuestion(String question, String documentation)`**: Generates an answer to a user's question, using previously generated project documentation as context. It constructs a specific prompt for Q&A and calls `callGeminiApi`.
-   **`createDocumentationPrompt(List<FileContent> files)`**: Builds the comprehensive Markdown prompt for Gemini to generate project documentation. It structures the prompt by grouping files by their parent directory and includes detailed instructions for the desired documentation format.
-   **`callGeminiApi(String prompt)`**: The low-level method responsible for making the HTTP POST request to the Gemini API endpoint. It constructs the JSON request body, sends the request using `OkHttpClient`, handles the response, and parses the generated text. It includes error handling for API failures and unexpected response structures.

#### Dependencies
-   `com.codedocumentor.model.FileContent`: For inputting code file data.
-   `com.fasterxml.jackson.databind.*`: For JSON processing (serialization/deserialization).
-   `okhttp3.*`: For making HTTP requests.
-   `org.slf4j.Logger`, `org.slf4j.LoggerFactory`: For logging.
-   `org.springframework.beans.factory.annotation.Value`: For injecting configuration properties.
-   `org.springframework.stereotype.Service`: Spring annotation.
-   `java.io.IOException`, `java.nio.file.Path`, `java.util.*`, `java.util.concurrent.TimeUnit`: Standard Java libraries for I/O, path manipulation, collections, and time units.

#### Usage Examples
-   **Generating documentation**:
    ```java
    List<FileContent> files = // ... populate with code files
    String documentation = geminiService.generateDocumentation(files);
    // documentation will contain the Markdown output from Gemini
    ```
-   **Answering a chatbot question**:
    ```java
    String question = "What is the main function?";
    String projectDocs = "## Project Overview\n..."; // previously generated docs
    String answer = geminiService.answerChatbotQuestion(question, projectDocs);
    // answer will contain the HTML response from Gemini
    ```

#### Code Comments
-   Class-level comment: "Wraps the Google Gemini generative language REST API. Mirrors Python's call_gemini_api() and create_documentation_prompt() functions."
-   Method-level comments: Explain the purpose of each public method and which Python function it mirrors.
-   Detailed explanation of the prompt construction logic, including grouping files by directory.
-   Error handling for missing `GEMINI_API_KEY` and API response parsing.

#### API Documentation

##### `generateDocumentation(List<FileContent> files)`
-   **Purpose**: Generates comprehensive project documentation from a list of code files using the Gemini API.
-   **Parameters**:
    -   `files`: `List<FileContent>` - A list of `FileContent` objects representing the code files to be documented.
-   **Returns**: `String` - A Markdown string containing the generated documentation.
-   **Exceptions**:
    -   `IllegalArgumentException`: If the input `files` list is null or empty.
    -   `IllegalStateException`: If `GEMINI_API_KEY` is not configured.
    -   `IOException`: If there's a network error or an issue with the Gemini API call or response parsing.

##### `answerChatbotQuestion(String question, String documentation)`
-   **Purpose**: Answers a user's free-form question about a project, using provided documentation as context, via the Gemini API.
-   **Parameters**:
    -   `question`: `String` - The user's question.
    -   `documentation`: `String` - The previously generated Markdown documentation for the project.
-   **Returns**: `String` - An HTML string containing the AI assistant's answer.
-   **Exceptions**:
    -   `IllegalStateException`: If `GEMINI_API_KEY` is not configured.
    -   `IOException`: If there's a network error or an issue with the Gemini API call or response parsing.

##### `createDocumentationPrompt(List<FileContent> files)` (Internal)
-   **Purpose**: Constructs the detailed Markdown prompt string to be sent to the Gemini API for documentation generation. It formats file contents and includes specific instructions for the AI.
-   **Parameters**:
    -   `files`: `List<FileContent>` - The list of code files.
-   **Returns**: `String` - The formatted prompt string.
-   **Exceptions**: None.

##### `callGeminiApi(String prompt)` (Internal)
-   **Purpose**: Sends a given prompt to the Gemini REST API and extracts the text response.
-   **Parameters**:
    -   `prompt`: `String` - The full prompt string to send to Gemini.
-   **Returns**: `String` - The text content generated by the Gemini model.
-   **Exceptions**:
    -   `IllegalStateException`: If `GEMINI_API_KEY` is not configured.
    -   `IOException`: If the HTTP request fails, the API returns an unsuccessful status code, or the response structure is unexpected.

### 📄 OneDrive/Documents/codedocumentor (2)/codedocumentor-java/src/main/java/com/codedocumentor/service/GitHubService.java

**File Type**: JAVA

#### Purpose and Overview
The `GitHubService` class encapsulates all interactions with the GitHub API, including OAuth 2.0 authentication, fetching user information, listing repositories, cloning repositories (via ZIP download), and committing documentation files. It acts as a bridge between the application's business logic and the external GitHub platform, mirroring various GitHub-related functionalities from the original Python project.

#### Key Functions/Classes
-   **`GitHubService` class**:
    -   Annotated with `@Service`, marking it as a Spring service component.
    -   Uses `org.slf4j.Logger` for logging.
    -   `@Value` annotations inject GitHub OAuth configuration (`clientId`, `clientSecret`, `redirectUri`) from environment variables or `application.properties`.
    -   Injects `FileProcessingService` to handle the extraction of code files from downloaded ZIP archives.
    -   **`httpClient`**: An `OkHttpClient` instance for making raw HTTP requests (e.g., for OAuth token exchange, ZIP downloads).
    -   **`objectMapper`**: An `ObjectMapper` from Jackson for JSON processing.
-   **`buildAuthorizationUrl(String state)`**: Constructs the URL to redirect users to GitHub for OAuth authorization.
-   **`exchangeCode