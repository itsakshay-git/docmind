import type { RagSource } from "./rag";

export type ChatMessageRole = "USER" | "ASSISTANT";

export type ChatMessage = {
  id: string;
  role: ChatMessageRole;
  content: string;
  sources: RagSource[];
  createdAt: string;
  optimistic?: boolean;
  streaming?: boolean;
};

export type ChatExchangeResponse = {
  userMessage: ChatMessage;
  assistantMessage: ChatMessage;
};
