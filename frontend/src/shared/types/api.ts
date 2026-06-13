export type Notebook = {
  id: string;
  title: string;
  createdAt: string;
  sourceCount: number;
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
  sourceType: "PDF" | "WEB_URL" | "YOUTUBE" | "YOUTUBE_TRANSCRIPT";
  sourceUrl: string | null;
  status: "UPLOADED" | "PROCESSING" | "PROCESSED" | "FAILED";
  failureReason: string | null;
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

export type StudioArtifactType =
  | "FLASHCARDS"
  | "QUIZ"
  | "BRIEFING"
  | "PODCAST_SCRIPT"
  | "INFOGRAPHIC_OUTLINE";

export type StudioArtifact = {
  id: string;
  notebookId: string;
  type: StudioArtifactType;
  title: string;
  markdownContent: string;
  jsonContent: string;
  sourceChunkIds: string[];
  audioAvailable: boolean;
  imageAvailable: boolean;
  createdAt: string;
};
