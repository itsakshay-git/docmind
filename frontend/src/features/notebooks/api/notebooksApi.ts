import { httpClient } from "../../../shared/api/httpClient";
import type { Notebook } from "../../../shared/types/api";

export const notebooksApi = {
  list() {
    return httpClient<Notebook[]>("/api/v1/notebooks");
  },

  create(title: string) {
    return httpClient<Notebook>("/api/v1/notebooks", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ title }),
    });
  },

  delete(notebookId: string) {
    return httpClient<void>(`/api/v1/notebooks/${notebookId}`, {
      method: "DELETE",
    });
  },
};
