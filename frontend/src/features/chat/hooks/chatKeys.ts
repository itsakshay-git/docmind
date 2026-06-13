export const chatKeys = {
  messages: (notebookId: string) => ["chat-messages", notebookId] as const,
};
