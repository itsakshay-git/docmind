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
- React Markdown and remark-gfm for assistant and Studio Markdown rendering.
- Plain CSS for styling.
- ESLint and Prettier for frontend quality checks.

## Structure

```text
src/
  app/                 Route shell and providers
  pages/               Route-level screens
  features/
    auth/              Login/register API, auth context, login components, and demo model
    chat/              Notebook chat history API, hooks, and chat UI
    documents/         PDF, website, YouTube source APIs, hooks, and source panels
    notebooks/         Notebook API, hooks, and notebook UI
    rag/               Low-level RAG search/ask API
    studio/            Study artifact API, hooks, metadata, mini apps, and UI surface
    user/              Profile/password/delete APIs and hooks
    workspace/         Notebook workspace layout components and mobile section model
  shared/
    api/               HTTP client
    components/        Reusable UI primitives
    lib/               Small utilities
    types/             Feature-split API types with a barrel export
  styles/              Plain CSS split by product area
```

## UI Direction

The app supports dark and light themes. The workspace intentionally has three primary areas:

- Sources/sidebar
- Chat
- Studio

The login/register route is a container page. Recruiter-facing hero content, demo credentials, password field, and auth form rendering live in `features/auth`.

Chat history is loaded from the backend and updated optimistically when the user sends a message. Assistant messages render Markdown. The chat header includes a compact `Context` selector for retrieved source count and a clear-chat action for deleting persisted notebook messages.

The notebook library has grid/list modes, title search, sort controls, inline title editing, and notebook deletion. The workspace sidebar can add PDF, Website, YouTube auto-transcript, and pasted YouTube transcript sources. It lists current notebook sources and supports deleting sources without leaving the notebook.

The notebook library route is a container page. Notebook list/grid/header rendering lives in `features/notebooks/components`, while query and mutation wiring lives in `features/notebooks/hooks`.

Studio can generate, list, open, and delete saved study artifacts. Flashcards and quizzes run as stateful mini apps, briefing renders as Markdown, podcast artifacts support playback/download when audio generation succeeds, and infographic artifacts load an authenticated PNG blob with PNG/JPG download actions.

The notebook workspace route is a container page. Workspace layout, mobile tabs, sidebar, and main content shell live in `features/workspace`; source, chat, and Studio behavior remains owned by their feature folders.

## State And Hooks

TanStack Query is the server-state layer. Feature hooks own query keys, mutations, and cache invalidation. AuthContext owns the JWT session. Local component state is used for view-only state such as selected tabs, open artifact, search text, form drafts, and flashcard/quiz progress.

Current query keys are documented in `docs/archive/frontend-structure-snapshot.md`.

Styles are imported through `src/styles/index.css`. The split is organizational only; selectors remain global and should be renamed only with matching component updates and verification.

Backend-facing DTOs live in `src/shared/types` and are split by feature. New code should import from `shared/types`, not individual type files, unless a feature-local type is intentionally private.

## Quality Checks

Run before committing frontend changes. See also `docs/current/quality-checks.md`.

```powershell
cd "D:\my projects\docmind\frontend"
corepack pnpm lint
corepack pnpm format:check
corepack pnpm test
corepack pnpm build
```

## Settings

The frontend includes `/settings` for account management:

- View email.
- Update full name.
- Update password.
- Switch dark/light theme.
- Delete account.

The settings route is a container page. Account settings UI lives in `features/user/components`, settings metadata lives in `features/user/model`, and server-state mutations live in `features/user/hooks`.

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
