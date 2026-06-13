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
