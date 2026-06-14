# DocMind Frontend

React + TypeScript + Vite frontend for the DocMind RAG workflow.

## Stack

- React
- TypeScript
- Vite
- React Router
- TanStack Query
- Lucide React
- React Markdown
- remark-gfm

## Structure

```text
src/
  app/                 App shell, routes, providers
  pages/               Route-level pages
  features/
    auth/              Auth API, session context, login components, and demo model
    chat/              Persistent notebook chat API, hooks, and UI
    notebooks/         Notebook API, hooks, library components, and notebook cards
    documents/         PDF, website, and YouTube source APIs/hooks/components
    rag/               Low-level ask/search API
    studio/            Artifact API, hooks, metadata, mini apps, preview, and downloads
    user/              Account APIs, hooks, model metadata, and settings components
    workspace/         Notebook workspace layout components and mobile section model
  shared/
    api/               HTTP client
    components/        Reusable UI primitives
    lib/               Small utilities
    types/             Feature-split API types with a barrel export
  styles/              Plain CSS split by product area
```

See also:

- `docs/frontend-structure-snapshot.md`
- `docs/frontend-refactor-rules.md`

## Run

```powershell
cd "D:\my projects\docmind\frontend"
corepack pnpm install
corepack pnpm dev
```

Open:

```text
http://127.0.0.1:5173
```

The backend must be running on:

```text
http://localhost:8081
```

To point at another backend URL, create `.env.local`:

```text
VITE_DOCMIND_API_URL=http://localhost:8081
```

## Quality Checks

```powershell
corepack pnpm lint
corepack pnpm format:check
corepack pnpm test
corepack pnpm build
```

Use `corepack pnpm format` to apply the shared Prettier style.

## Product Direction

The UI supports dark and light themes and keeps the notebook workspace focused on three areas:

- Sources
- Chat
- Studio

Chat messages are loaded from the backend, optimistically updated on send, and assistant responses render Markdown.

Studio artifacts are generated from notebook sources, saved on the backend, and opened as mini apps. Flashcards, quizzes, and briefings stay in-app. Podcast artifacts expose audio playback/download when available, and infographic artifacts preview an authenticated PNG with PNG/JPG download actions.

Notebook cards expose management actions on hover/focus. Uploaded notebook sources can be deleted directly from the workspace sidebar.

The workspace can add PDF files, website URLs, best-effort YouTube auto-transcripts, and pasted YouTube transcripts. All sources reuse the same backend chunking and embedding pipeline as PDFs.
