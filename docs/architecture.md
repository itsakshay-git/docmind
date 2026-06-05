# DocMind Architecture

## Vision

DocMind is an AI-powered knowledge platform inspired by NotebookLM.

Users can:

- Upload PDFs
- Upload DOCX files
- Upload TXT files
- Import web pages
- Import YouTube transcripts
- Chat with documents
- Generate summaries
- Generate flashcards
- Generate quizzes
- Generate podcast scripts

---

## Architecture Style

Phase 1:
Modular Monolith

Phase 2:
Microservices

---

## Modules

### Auth Module

Responsibilities:

- Registration
- Login
- JWT Authentication
- User Management

### Notebook Module

Responsibilities:

- Create notebook
- Manage notebook
- Organize sources

### Document Module

Responsibilities:

- Upload documents
- Manage sources
- Track processing status

### Processing Module

Responsibilities:

- Text extraction
- Chunking
- Embedding generation

### RAG Module

Responsibilities:

- Semantic retrieval
- Context assembly
- Citation generation

### AI Module

Responsibilities:

- Chat
- Summaries
- Flashcards
- Quizzes
- Podcast scripts

---

## High Level Flow

User Uploads PDF
↓
Store Metadata
↓
Extract Text
↓
Chunk Text
↓
Generate Embeddings
↓
Store in pgvector
↓
Ready For Chat

---

## Future Architecture

API Gateway
↓
Auth Service
Knowledge Service
Processing Service
