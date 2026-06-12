# Frontend Architecture

The frontend is a React, TypeScript, and Vite application in:

```text
frontend/
```

## Libraries

- React for UI.
- React Router for pages.
- TanStack Query for server state, mutations, and cache updates.
- Lucide React for professional iconography.
- React Markdown and remark-gfm for assistant answer rendering.

## Structure

```text
src/
  app/                 Route shell and providers
  pages/               Route-level screens
  features/
    auth/              Login/register API and auth context
    chat/              Notebook chat history API and chat UI
    documents/         PDF upload API and source upload panel
    notebooks/         Notebook API and notebook cards
    rag/               Low-level RAG search/ask API
    studio/            Study artifact UI surface
  shared/
    api/               HTTP client
    components/        Reusable UI primitives
    lib/               Small utilities
    types/             Shared API types
```

## UI Direction

The app is dark-only. The workspace intentionally has three primary areas:

- Sources/sidebar
- Chat
- Studio

Chat history is loaded from the backend and updated optimistically when the user sends a message. Assistant messages render Markdown.

The chat retrieval size is not exposed in the UI. The frontend sends `topK: 5` internally so the product feels simple for normal users.

## Settings

The frontend includes `/settings` for basic account and workspace management:

- View email.
- Update full name.
- Update password.
- Delete notebooks.
- Delete uploaded documents.

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
