import { httpClient } from "../../../shared/api/httpClient";
import type { DocumentSource } from "../../../shared/types/api";

export const documentsApi = {
  uploadPdf(notebookId: string, file: File) {
    const formData = new FormData();
    formData.append("file", file);

    return httpClient<string>(`/api/v1/documents/${notebookId}/upload`, {
      method: "POST",
      body: formData,
    });
  },

  addWebUrl(notebookId: string, url: string) {
    return httpClient<DocumentSource>(`/api/v1/documents/${notebookId}/web-url`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ url }),
    });
  },

  addYouTubeUrl(notebookId: string, url: string) {
    return httpClient<DocumentSource>(`/api/v1/documents/${notebookId}/youtube`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ url }),
    });
  },

  addYouTubeTranscript(notebookId: string, url: string, title: string, transcript: string) {
    return httpClient<DocumentSource>(`/api/v1/documents/${notebookId}/youtube-transcript`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ url, title, transcript }),
    });
  },

  getEmbeddingCount() {
    return httpClient<number>("/api/v1/embeddings/count");
  },

  listMine() {
    return httpClient<DocumentSource[]>("/api/v1/documents");
  },

  listByNotebook(notebookId: string) {
    return httpClient<DocumentSource[]>(`/api/v1/documents/notebooks/${notebookId}`);
  },

  delete(documentId: string) {
    return httpClient<void>(`/api/v1/documents/${documentId}`, {
      method: "DELETE",
    });
  },
};
