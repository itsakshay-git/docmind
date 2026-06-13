import { httpClient } from "../../../shared/api/httpClient";
import type { ChatExchangeResponse, ChatMessage } from "../../../shared/types";

export const chatApi = {
  getMessages(notebookId: string) {
    return httpClient<ChatMessage[]>(`/api/v1/chat/notebooks/${notebookId}/messages`);
  },

  sendMessage(notebookId: string, content: string, topK: number) {
    return httpClient<ChatExchangeResponse>(`/api/v1/chat/notebooks/${notebookId}/messages`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ content, topK }),
    });
  },

  clearMessages(notebookId: string) {
    return httpClient<void>(`/api/v1/chat/notebooks/${notebookId}/messages`, {
      method: "DELETE",
    });
  },
};
