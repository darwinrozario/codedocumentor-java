# Code Auto Documenter — Java / Spring Boot

This is the Java rewrite of the original Python/Flask `codedocumentor` project.
It exposes **exactly the same REST endpoints** as the Flask app so the frontend
JavaScript (script.js, chatbot.js, github-integration.js) continues to work
without any modification.

## Tech stack

| Layer | Flask (original) | Spring Boot (this project) |
|-------|-----------------|---------------------------|
| Web framework | Flask 2.3 | Spring Boot 3 (Spring MVC) |
| Templating | Jinja2 | Thymeleaf |
| HTTP client | `requests` | OkHttp 4 |
| GitHub API | PyGithub | kohsuke/github-api |
| Env vars | python-dotenv | dotenv-java |
| Build tool | pip / requirements.txt | Maven |
| Java version | — | 17 |

## Prerequisites

- JDK 17 or later
- Maven 3.8+

## Configuration

Copy `.env.example` to `.env` (or set environment variables):

```
GEMINI_API_KEY=your_gemini_api_key
GITHUB_CLIENT_ID=your_github_oauth_client_id
GITHUB_CLIENT_SECRET=your_github_oauth_client_secret
GITHUB_REDIRECT_URI=http://localhost:8080/auth/github/callback
SECRET_KEY=your_random_secret_key
```

## Running locally

```bash
# From the codedocumentor-java directory
mvn spring-boot:run
```

Then open http://localhost:8080 in your browser.

## Building a JAR

```bash
mvn package -DskipTests
java -jar target/codedocumentor-1.0.0.jar
```

## Docker

```bash
docker build -t codedocumentor-java .
docker run -p 8080:8080 --env-file .env codedocumentor-java
```

## API endpoints (identical to Flask version)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/` | Main UI |
| POST | `/generate-documentation` | Upload files/ZIP → Gemini docs |
| POST | `/analyze-github` | Clone GitHub repo → Gemini docs |
| POST | `/download-documentation` | Return doc for client download |
| GET | `/download/{token}` | Server-side Markdown download |
| POST | `/chatbot` | Q&A over generated docs |
| GET | `/auth/github` | Start GitHub OAuth |
| GET | `/auth/github/callback` | GitHub OAuth callback |
| GET | `/auth/github/logout` | Clear GitHub session |
| GET | `/api/github/repositories` | List authed user's repos |
| POST | `/api/github/commit-documentation` | Commit docs to GitHub repo |
| POST,GET | `/webhooks/salesiq` | SalesIQ operator widget |
| POST | `/zobot/analyze-repo` | Zobot analysis endpoint |
