export type Notebook = {
  id: string;
  title: string;
};

export type UserProfile = {
  id: string;
  email: string;
  fullName: string | null;
};

export type AuthResponse = {
  accessToken: string;
};

export type DocumentSource = {
  id: string;
  notebookId: string;
  fileName: string;
  status: "UPLOADED" | "PROCESSING" | "PROCESSED" | "FAILED";
  createdAt: string;
};

export type SemanticSearchResult = {
  chunkId: string;
  documentId: string;
  content: string;
  score: number;
};

export type RagSource = {
  chunkId: string;
  documentId: string;
  score: number;
};

export type RagAskResponse = {
  answer: string;
  sources: RagSource[];
};

export type ChatMessageRole = "USER" | "ASSISTANT";

export type ChatMessage = {
  id: string;
  role: ChatMessageRole;
  content: string;
  sources: RagSource[];
  createdAt: string;
  optimistic?: boolean;
};

export type ChatExchangeResponse = {
  userMessage: ChatMessage;
  assistantMessage: ChatMessage;
};
