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
Use PostgreSQL plus custom `pgvector` retrieval

Reason:
PostgreSQL keeps the MVP deployable and easy to inspect. DocMind now stores Gemini's 3072-dimensional embeddings in `pgvector` and uses a narrow custom JDBC repository for exact DB-side cosine search, while retaining the legacy JSON text value for compatibility.

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
