# Frontend Refactor Rules

Use these rules while restructuring the React frontend.

## Safety Rules

- Do not change backend APIs, request bodies, response shapes, or auth behavior.
- Do not rename CSS classes during structure refactors unless every usage is verified.
- Preserve existing query keys unless replacing them with centralized helpers that return the exact same arrays.
- Keep TanStack Query for server state, AuthContext for session state, and local `useState` for component-only UI state.
- Avoid redesigning UI during structure work.
- Keep route paths unchanged.
- Keep blob downloads and authenticated media fetches in the Studio feature.

## Refactor Order

1. Document the current structure and working behavior.
2. Add query key helpers.
3. Extract hooks around existing API calls and cache invalidation.
4. Move page logic to hooks before splitting presentational components.
5. Split large feature components only after behavior is covered by build/manual checks.
6. Update docs after each meaningful structural milestone.

## Verification

Run after frontend structure changes:

```powershell
cd "D:\my projects\docmind\frontend"
corepack pnpm build
```

Manual checks should cover login, notebooks, workspace sources, chat, Studio, settings, mobile tabs, and theme behavior.
