import { API_BASE_URL, ApiError, httpClient } from "../../../shared/api/httpClient";
import { tokenStorage } from "../../../shared/lib/tokenStorage";
import type { ChatExchangeResponse, ChatMessage, RagSource } from "../../../shared/types";

type ChatStreamHandlers = {
  onAssistantMessage?: (message: ChatMessage) => void;
  onDone?: () => void;
  onError?: (message: string) => void;
  onSources?: (sources: RagSource[]) => void;
  onToken?: (token: string) => void;
  onUserMessage?: (message: ChatMessage) => void;
};

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

  async streamMessage(notebookId: string, content: string, topK: number, handlers: ChatStreamHandlers = {}) {
    const headers: Record<string, string> = {
      Accept: "text/event-stream",
      "Content-Type": "application/json",
    };
    const token = tokenStorage.get();

    if (token) {
      headers.Authorization = `Bearer ${token}`;
    }

    const response = await fetch(`${API_BASE_URL}/api/v1/chat/notebooks/${notebookId}/messages/stream`, {
      method: "POST",
      headers,
      body: JSON.stringify({ content, topK }),
    });

    if (!response.ok) {
      const message = await response.text();
      throw new ApiError(message || `Request failed with status ${response.status}`, response.status);
    }

    if (!response.body) {
      throw new ApiError("Streaming is not supported by this browser.", response.status);
    }

    const reader = response.body.getReader();
    const decoder = new TextDecoder();
    let buffer = "";

    while (true) {
      const { done, value } = await reader.read();

      if (done) {
        break;
      }

      buffer += decoder.decode(value, { stream: true });
      buffer = dispatchBufferedEvents(buffer, handlers);
    }

    buffer += decoder.decode();

    if (buffer.trim()) {
      dispatchEventBlock(buffer, handlers);
    }
  },

  clearMessages(notebookId: string) {
    return httpClient<void>(`/api/v1/chat/notebooks/${notebookId}/messages`, {
      method: "DELETE",
    });
  },
};

function dispatchBufferedEvents(buffer: string, handlers: ChatStreamHandlers) {
  let nextBuffer = buffer.replace(/\r\n/g, "\n");
  let boundary = nextBuffer.indexOf("\n\n");

  while (boundary >= 0) {
    const block = nextBuffer.slice(0, boundary);
    nextBuffer = nextBuffer.slice(boundary + 2);
    dispatchEventBlock(block, handlers);
    boundary = nextBuffer.indexOf("\n\n");
  }

  return nextBuffer;
}

function dispatchEventBlock(block: string, handlers: ChatStreamHandlers) {
  const lines = block.split("\n");
  let eventName = "message";
  const dataLines: string[] = [];

  for (const line of lines) {
    if (line.startsWith("event:")) {
      eventName = line.slice("event:".length).trim();
    }

    if (line.startsWith("data:")) {
      dataLines.push(line.slice("data:".length).trimStart());
    }
  }

  const data = dataLines.join("\n");

  if (!eventName || eventName === "message") {
    return;
  }

  switch (eventName) {
    case "userMessage":
      handlers.onUserMessage?.(JSON.parse(data) as ChatMessage);
      break;
    case "sources":
      handlers.onSources?.(JSON.parse(data) as RagSource[]);
      break;
    case "token":
      handlers.onToken?.((JSON.parse(data) as { token: string }).token);
      break;
    case "assistantMessage":
      handlers.onAssistantMessage?.(JSON.parse(data) as ChatMessage);
      break;
    case "error":
      handlers.onError?.(data || "Message failed. Please try again.");
      throw new Error(data || "Message failed. Please try again.");
    case "done":
      handlers.onDone?.();
      break;
    default:
      break;
  }
}
