export const documentKeys = {
  all: ["documents"] as const,
  byNotebook: (notebookId: string) => ["notebook-documents", notebookId] as const,
  embeddingCount: ["embedding-count"] as const,
};
