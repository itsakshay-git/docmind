# Frontend Structure Snapshot

Date: 2026-06-14

This snapshot records the current working React frontend after the structure refactor. Use it as the baseline for future changes.

## Stack

- React 19 + TypeScript + Vite.
- React Router for routes.
- TanStack Query for server state, mutations, optimistic chat updates, and cache invalidation.
- AuthContext plus tokenStorage for JWT session state.
- React Markdown + remark-gfm for assistant and Studio Markdown.
- lucide-react for icons.
- Plain CSS split by product area under `src/styles/` and imported through `src/styles/index.css`.

No Redux, Zustand, Tailwind, or generated client is currently used.

## Routes

- `/login`: login/register screen, recruiter demo credentials, GitHub and LinkedIn links. This screen stays dark even when the app theme is light.
- `/`: redirects to `/notebooks`.
- `/notebooks`: authenticated notebook library with search, sort, grid/list views, create, inline rename, delete, settings link, and sign out.
- `/notebooks/:notebookId`: authenticated workspace with Sources, Chat, and Studio. Desktop shows three areas; mobile uses bottom app-style tabs.
- `/settings`: authenticated account settings with user, password, theme, and delete-account sections.

## Feature Ownership

- `features/auth`: auth API, session context, login form/hero components, and demo credential model.
- `features/notebooks`: notebook API, query hooks, library header/toolbar/grid/list components, and notebook cards.
- `features/documents`: source APIs, query hooks, and source upload/list panels.
- `features/chat`: persisted chat API, query hooks, and chat panel.
- `features/rag`: lower-level ask/search API kept for direct RAG calls.
- `features/studio`: Studio artifact API, query hooks, artifact metadata, mini apps, and artifact shell.
- `features/user`: user profile/password/delete API, query hooks, settings metadata, and settings section components.
- `features/workspace`: notebook workspace layout, mobile tabs, sidebar, and main content shell.
- `shared/api`: HTTP client and API error handling.
- `shared/types`: feature-split backend-facing DTO types re-exported through `shared/types`.
- `shared/components`: reusable primitives.
- `styles`: global CSS split by product area.

## Route Containers

- `pages/LoginPage.tsx`: owns auth form state, login/register mode, mutation, and navigation.
- `pages/NotebooksPage.tsx`: owns notebook search/sort/view state and notebook mutations.
- `pages/NotebookWorkspacePage.tsx`: owns workspace mobile section state and source/chat mutation wiring.
- `pages/SettingsPage.tsx`: owns settings active section, form state, theme effect, and account mutations.

## Query Keys

Current keys that must be preserved during refactor:

- `["notebooks"]`
- `["documents"]`
- `["notebook-documents", notebookId]`
- `["embedding-count"]`
- `["chat-messages", notebookId]`
- `["studio-artifacts", notebookId]`
- `["user-profile"]`

## Working Data Flows

- Auth: login/register call auth API, store JWT, then protected routes read the token through AuthContext/tokenStorage.
- Notebooks: library loads notebooks, creates notebooks, updates titles, deletes notebooks, then invalidates notebook-related queries.
- Sources: workspace adds PDF, website URL, YouTube auto transcript, or pasted YouTube transcript; successful ingestion invalidates documents and embedding counts.
- Chat: messages load per notebook, sends are optimistic, assistant/user messages from the backend replace optimistic entries, and clear chat empties the cached thread.
- Studio: artifacts load per notebook, generation saves an artifact then opens it, delete clears the opened artifact and refreshes the list, audio/image blobs are fetched with auth headers.
- Settings: profile loads once, updates write through the query cache, password form clears on success, delete account clears query cache and signs out.

## Known Working Flows To Protect

- Login/register demo works and the landing page stays dark-only.
- Notebook library search, sort, grid/list, create, inline rename, delete, settings, and sign out work.
- Workspace desktop layout and mobile Sources/Chat/Studio tabs work.
- Source upload/delete works for PDF, website, YouTube auto transcript, and pasted YouTube transcript.
- Chat send, optimistic display, persisted history, clear chat, Context selector, Markdown rendering, and code-block copy work.
- Studio flashcards, quiz, briefing, podcast audio, infographic preview/download, artifact open/back/delete, and mobile overflow behavior work.
- Settings profile, password, theme, delete account, desktop layout, and mobile bottom tabs work.
