import { httpClient } from "../../../shared/api/httpClient";
import type { RagAskResponse, SemanticSearchResult } from "../../../shared/types";

export const ragApi = {
  ask(notebookId: string, question: string, topK: number) {
    return httpClient<RagAskResponse>(`/api/v1/rag/notebooks/${notebookId}/ask`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ question, topK }),
    });
  },

  search(notebookId: string, question: string, topK: number) {
    return httpClient<SemanticSearchResult[]>(`/api/v1/rag/notebooks/${notebookId}/search`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ question, topK }),
    });
  },
};
