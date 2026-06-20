import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { chatApi } from "../api/chatApi";
import type { ChatExchangeResponse, ChatMessage } from "../../../shared/types";
import { chatKeys } from "./chatKeys";

export function useNotebookChat(notebookId: string) {
  const queryClient = useQueryClient();
  const messagesQuery = useQuery({
    queryKey: chatKeys.messages(notebookId),
    queryFn: () => chatApi.getMessages(notebookId),
    enabled: Boolean(notebookId),
  });

  const sendMessage = useMutation({
    mutationFn: async ({ content, topK }: { content: string; topK: number }) => {
      let streamStarted = false;
      let userMessage: ChatMessage | undefined;
      let assistantMessage: ChatMessage | undefined;
      let streamCompleted = false;
      let assistantPlaceholderId = "";

      try {
        await chatApi.streamMessage(notebookId, content, topK, {
          onUserMessage(message) {
            streamStarted = true;
            userMessage = message;
            assistantPlaceholderId = `streaming-${message.id}`;

            const assistantPlaceholder: ChatMessage = {
              id: assistantPlaceholderId,
              role: "ASSISTANT",
              content: "",
              sources: [],
              createdAt: new Date().toISOString(),
              streaming: true,
            };

            queryClient.setQueryData<ChatMessage[]>(chatKeys.messages(notebookId), (current = []) => [
              ...current.filter((item) => !item.optimistic && item.id !== message.id),
              message,
              assistantPlaceholder,
            ]);
          },
          onSources(sources) {
            updateStreamingAssistant(queryClient, notebookId, assistantPlaceholderId, (message) => ({
              ...message,
              sources,
            }));
          },
          onToken(token) {
            updateStreamingAssistant(queryClient, notebookId, assistantPlaceholderId, (message) => ({
              ...message,
              content: `${message.content}${token}`,
            }));
          },
          onAssistantMessage(message) {
            assistantMessage = message;
            streamCompleted = true;

            queryClient.setQueryData<ChatMessage[]>(chatKeys.messages(notebookId), (current = []) => [
              ...current.filter(
                (item) => !item.optimistic && item.id !== assistantPlaceholderId && item.id !== message.id
              ),
              message,
            ]);
          },
        });

        if (!userMessage || !assistantMessage) {
          throw new Error("Streaming response ended before the final message was saved.");
        }

        return {
          userMessage,
          assistantMessage,
        } satisfies ChatExchangeResponse;
      } catch (error) {
        if (userMessage && assistantMessage && streamCompleted) {
          return {
            userMessage,
            assistantMessage,
          } satisfies ChatExchangeResponse;
        }

        if (!streamStarted) {
          return chatApi.sendMessage(notebookId, content, topK);
        }

        throw error;
      }
    },
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
        ...current.filter((item) => !item.streaming),
        failedAssistantMessage,
      ]);
    },
    onSuccess: (response) => {
      queryClient.setQueryData<ChatMessage[]>(chatKeys.messages(notebookId), (current = []) => [
        ...current.filter(
          (message) =>
            !message.optimistic &&
            !message.streaming &&
            message.id !== response.userMessage.id &&
            message.id !== response.assistantMessage.id
        ),
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

function updateStreamingAssistant(
  queryClient: ReturnType<typeof useQueryClient>,
  notebookId: string,
  assistantPlaceholderId: string,
  update: (message: ChatMessage) => ChatMessage
) {
  if (!assistantPlaceholderId) {
    return;
  }

  queryClient.setQueryData<ChatMessage[]>(chatKeys.messages(notebookId), (current = []) =>
    current.map((message) => (message.id === assistantPlaceholderId ? update(message) : message))
  );
}
