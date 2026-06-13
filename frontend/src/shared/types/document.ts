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
