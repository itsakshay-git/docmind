# DocMind Agent Instructions

## Project Context

DocMind is a Spring Boot document intelligence platform inspired by NotebookLM. The MVP flow is PDF upload, text extraction, chunking, embedding generation, storage, semantic retrieval, and Gemini-grounded answers.

Use [docs/chatgpt-project-handoff.md](docs/chatgpt-project-handoff.md) as the current handoff from the ChatGPT web Project.

## Stack

- Java 21
- Spring Boot 3.5.14
- Maven
- PostgreSQL
- Flyway
- Spring Security with JWT
- Spring AI 1.1.7
- Google Gemini for chat and embeddings
- Apache PDFBox for PDF parsing

## Working Rules

- Before making changes, inspect the relevant existing files and follow local patterns.
- Keep changes narrow and explain changed files plus diffs in chat.
- Never commit API keys or secrets.
- Use `GEMINI_API_KEY` from the environment for Gemini configuration.
- Use constructor injection with `@RequiredArgsConstructor`.
- Do not use field injection.
- Add database changes through new Flyway migrations only.
- Do not edit old migrations unless the user explicitly asks.
- Prefer feature-based packages such as `ai`, `document`, `rag`, `security`, `common`, and user/auth-related modules.

## Current Direction

The next milestone is automatic embedding generation and storage for uploaded PDF chunks, followed by semantic retrieval and the first RAG question-answering flow.

