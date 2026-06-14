# Architecture Decisions

## Decision 001

Date:
2026-06-02

Decision:
Use Modular Monolith First

Reason:
Faster development while maintaining clean boundaries.

---

## Decision 002

Date:
2026-06-02

Decision:
Use PostgreSQL first, keep `pgvector` as the next retrieval upgrade

Reason:
PostgreSQL keeps the MVP deployable and easy to inspect. Embeddings are stored as JSON text for the first deployed version, while `pgvector` remains the planned upgrade for database-native similarity search.

---

## Decision 003

Date:
2026-06-02

Decision:
Use Spring AI

Reason:
Strong integration with Spring ecosystem.

---

## Decision 004

Date:
2026-06-02

Decision:
Frontend Stack

React
Vite
React Query
React Router
Plain CSS

Reason:
React Query handles server state and cache invalidation. AuthContext handles the JWT session. Local React state is enough for UI-only state, so Redux/Zustand are intentionally not used right now.
