export type StudioArtifactType = "FLASHCARDS" | "QUIZ" | "BRIEFING" | "PODCAST_SCRIPT" | "INFOGRAPHIC_OUTLINE";

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
