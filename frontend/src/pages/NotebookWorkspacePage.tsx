import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { ArrowLeft, BrainCircuit, Library } from "lucide-react";
import { Link, useParams } from "react-router-dom";
import { chatApi } from "../features/chat/api/chatApi";
import { ChatPanel } from "../features/chat/components/ChatPanel";
import { SourceListPanel } from "../features/documents/components/SourceListPanel";
import { SourceUploadPanel } from "../features/documents/components/SourceUploadPanel";
import { documentsApi } from "../features/documents/api/documentsApi";
import { StudioPanel } from "../features/studio/components/StudioPanel";
import { notebooksApi } from "../features/notebooks/api/notebooksApi";
import type { ChatMessage } from "../shared/types/api";

export function NotebookWorkspacePage() {
  const { notebookId = "" } = useParams();
  const queryClient = useQueryClient();

  const notebooksQuery = useQuery({
    queryKey: ["notebooks"],
    queryFn: notebooksApi.list,
  });

  const messagesQuery = useQuery({
    queryKey: ["chat-messages", notebookId],
    queryFn: () => chatApi.getMessages(notebookId),
    enabled: Boolean(notebookId),
  });

  const notebookDocumentsQuery = useQuery({
    queryKey: ["notebook-documents", notebookId],
    queryFn: () => documentsApi.listByNotebook(notebookId),
    enabled: Boolean(notebookId),
  });

  const uploadMutation = useMutation({
    mutationFn: (file: File) => documentsApi.uploadPdf(notebookId, file),
    onSuccess() {
      queryClient.invalidateQueries({ queryKey: ["embedding-count"] });
      queryClient.invalidateQueries({ queryKey: ["documents"] });
      queryClient.invalidateQueries({ queryKey: ["notebook-documents", notebookId] });
    },
  });

  const deleteDocumentMutation = useMutation({
    mutationFn: documentsApi.delete,
    onSuccess() {
      queryClient.invalidateQueries({ queryKey: ["documents"] });
      queryClient.invalidateQueries({ queryKey: ["notebook-documents", notebookId] });
      queryClient.invalidateQueries({ queryKey: ["embedding-count"] });
    },
  });

  const sendMessageMutation = useMutation({
    mutationFn: ({ content, topK }: { content: string; topK: number }) => chatApi.sendMessage(notebookId, content, topK),
    onMutate: async ({ content }) => {
      await queryClient.cancelQueries({ queryKey: ["chat-messages", notebookId] });

      const previousMessages =
        queryClient.getQueryData<ChatMessage[]>(["chat-messages", notebookId]) ?? [];

      const optimisticMessage: ChatMessage = {
        id: `optimistic-${Date.now()}`,
        role: "USER",
        content,
        sources: [],
        createdAt: new Date().toISOString(),
        optimistic: true,
      };

      queryClient.setQueryData<ChatMessage[]>(
        ["chat-messages", notebookId],
        [...previousMessages, optimisticMessage]
      );

      return { previousMessages };
    },
    onError: (error) => {
      const message =
        error instanceof Error
          ? error.message
          : "Message failed. Please try again.";

      const failedAssistantMessage: ChatMessage = {
        id: `failed-${Date.now()}`,
        role: "ASSISTANT",
        content: message,
        sources: [],
        createdAt: new Date().toISOString(),
      };

      queryClient.setQueryData<ChatMessage[]>(
        ["chat-messages", notebookId],
        (current = []) => [...current, failedAssistantMessage]
      );
    },
    onSuccess: (response) => {
      queryClient.setQueryData<ChatMessage[]>(
        ["chat-messages", notebookId],
        (current = []) => [
          ...current.filter((message) => !message.optimistic),
          response.userMessage,
          response.assistantMessage,
        ]
      );
    },
  });

  const notebook = notebooksQuery.data?.find((item) => item.id === notebookId);
  const messages = messagesQuery.data ?? [];
  const isBusy = sendMessageMutation.isPending || uploadMutation.isPending || deleteDocumentMutation.isPending;

  return (
    <main className="workspace-page">
      <aside className="workspace-sidebar">
        <div className="brand-lockup compact">
          <div className="brand-symbol"><BrainCircuit size={23} /></div>
          <span>DocMind</span>
        </div>
        <Link className="back-link" to="/notebooks"><ArrowLeft size={16} /> All notebooks</Link>
        <section className="sidebar-block">
          <div className="panel-heading"><span><Library size={17} /> Notebook</span></div>
          <h2>{notebook?.title ?? "Notebook"}</h2>
          <p>Source-grounded workspace for upload, retrieval, chat, and study artifacts.</p>
        </section>
        <SourceUploadPanel isUploading={uploadMutation.isPending} onUpload={(file) => uploadMutation.mutate(file)} />
        <SourceListPanel
          documents={notebookDocumentsQuery.data ?? []}
          isDeleting={deleteDocumentMutation.isPending}
          onDelete={(documentId) => deleteDocumentMutation.mutate(documentId)}
        />
      </aside>

      <section className="workspace-main">
        <section className="workspace-content-grid">
          <ChatPanel
            errorMessage={messagesQuery.error instanceof Error ? messagesQuery.error.message : undefined}
            isBusy={isBusy}
            isLoading={messagesQuery.isLoading}
            messages={messages}
            onSend={(content) => sendMessageMutation.mutate({ content, topK: 5 })}
          />
          <StudioPanel />
        </section>
      </section>
    </main>
  );
}
