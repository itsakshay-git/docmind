import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { chatApi } from "../api/chatApi";
import type { ChatMessage } from "../../../shared/types";
import { chatKeys } from "./chatKeys";

export function useNotebookChat(notebookId: string) {
  const queryClient = useQueryClient();
  const messagesQuery = useQuery({
    queryKey: chatKeys.messages(notebookId),
    queryFn: () => chatApi.getMessages(notebookId),
    enabled: Boolean(notebookId),
  });

  const sendMessage = useMutation({
    mutationFn: ({ content, topK }: { content: string; topK: number }) =>
      chatApi.sendMessage(notebookId, content, topK),
    onMutate: async ({ content }) => {
      await queryClient.cancelQueries({ queryKey: chatKeys.messages(notebookId) });

      const previousMessages = queryClient.getQueryData<ChatMessage[]>(chatKeys.messages(notebookId)) ?? [];

      const optimisticMessage: ChatMessage = {
        id: `optimistic-${Date.now()}`,
        role: "USER",
        content,
        sources: [],
        createdAt: new Date().toISOString(),
        optimistic: true,
      };

      queryClient.setQueryData<ChatMessage[]>(chatKeys.messages(notebookId), [...previousMessages, optimisticMessage]);

      return { previousMessages };
    },
    onError: (error) => {
      const message = error instanceof Error ? error.message : "Message failed. Please try again.";

      const failedAssistantMessage: ChatMessage = {
        id: `failed-${Date.now()}`,
        role: "ASSISTANT",
        content: message,
        sources: [],
        createdAt: new Date().toISOString(),
      };

      queryClient.setQueryData<ChatMessage[]>(chatKeys.messages(notebookId), (current = []) => [
        ...current,
        failedAssistantMessage,
      ]);
    },
    onSuccess: (response) => {
      queryClient.setQueryData<ChatMessage[]>(chatKeys.messages(notebookId), (current = []) => [
        ...current.filter((message) => !message.optimistic),
        response.userMessage,
        response.assistantMessage,
      ]);
    },
  });

  const clearChat = useMutation({
    mutationFn: () => chatApi.clearMessages(notebookId),
    onSuccess() {
      queryClient.setQueryData<ChatMessage[]>(chatKeys.messages(notebookId), []);
      queryClient.invalidateQueries({ queryKey: chatKeys.messages(notebookId) });
    },
  });

  return {
    clearChat,
    messages: messagesQuery.data ?? [],
    messagesQuery,
    sendMessage,
  };
}
