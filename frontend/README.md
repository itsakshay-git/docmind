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
    auth/              Auth API and session context
    chat/              Persistent notebook chat history
    notebooks/         Notebook API and notebook cards
    documents/         Source upload API and components
    rag/               Low-level ask/search API
    studio/            Study-generation panel
  shared/
    api/               HTTP client
    components/        Reusable UI primitives
    lib/               Small utilities
    types/             API types
```

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

## Product Direction

The UI is dark-only and keeps the notebook workspace focused on three areas:

- Sources
- Chat
- Studio

Chat messages are loaded from the backend, optimistically updated on send, and assistant responses render Markdown.
