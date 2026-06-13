import { useState } from "react";
import { useParams } from "react-router-dom";
import { useNotebookChat } from "../features/chat/hooks/useNotebookChat";
import { useNotebookDocuments, useSourceMutations } from "../features/documents/hooks/useNotebookDocuments";
import { useNotebooks } from "../features/notebooks/hooks/useNotebooks";
import { WorkspaceMain } from "../features/workspace/components/WorkspaceMain";
import { WorkspaceMobileTabs } from "../features/workspace/components/WorkspaceMobileTabs";
import { WorkspaceMobileTopbar } from "../features/workspace/components/WorkspaceMobileTopbar";
import { WorkspaceSidebar } from "../features/workspace/components/WorkspaceSidebar";
import type { WorkspaceMobileSection } from "../features/workspace/model/workspaceSections";

export function NotebookWorkspacePage() {
  const { notebookId = "" } = useParams();
  const [chatTopK, setChatTopK] = useState(5);
  const [activeMobileSection, setActiveMobileSection] = useState<WorkspaceMobileSection>("chat");

  const notebooksQuery = useNotebooks();
  const notebookDocumentsQuery = useNotebookDocuments(notebookId);
  const {
    addWebUrl: addWebUrlMutation,
    addYouTubeTranscript: addYouTubeTranscriptMutation,
    addYouTubeUrl: addYouTubeUrlMutation,
    deleteDocument: deleteDocumentMutation,
    uploadPdf: uploadMutation,
  } = useSourceMutations(notebookId);
  const {
    clearChat: clearChatMutation,
    messages,
    messagesQuery,
    sendMessage: sendMessageMutation,
  } = useNotebookChat(notebookId);

  const notebook = notebooksQuery.data?.find((item) => item.id === notebookId);
  const isAddingSource = uploadMutation.isPending || addWebUrlMutation.isPending || addYouTubeUrlMutation.isPending || addYouTubeTranscriptMutation.isPending;
  const isBusy = sendMessageMutation.isPending || clearChatMutation.isPending || isAddingSource || deleteDocumentMutation.isPending;

  return (
    <main className="workspace-page">
      <WorkspaceMobileTopbar />
      <WorkspaceMobileTabs activeSection={activeMobileSection} onSectionChange={setActiveMobileSection} />

      <WorkspaceSidebar
        activeMobileSection={activeMobileSection}
        documents={notebookDocumentsQuery.data ?? []}
        isAddingSource={isAddingSource}
        isDeletingSource={deleteDocumentMutation.isPending}
        notebookTitle={notebook?.title ?? "Notebook"}
        onAddWebUrl={(url) => addWebUrlMutation.mutate(url)}
        onAddYouTubeTranscript={(url, title, transcript) => addYouTubeTranscriptMutation.mutate({ url, title, transcript })}
        onAddYouTubeUrl={(url) => addYouTubeUrlMutation.mutate(url)}
        onDeleteSource={(documentId) => deleteDocumentMutation.mutate(documentId)}
        onUploadPdf={(file) => uploadMutation.mutate(file)}
      />

      <WorkspaceMain
        activeMobileSection={activeMobileSection}
        chatErrorMessage={messagesQuery.error instanceof Error ? messagesQuery.error.message : undefined}
        chatTopK={chatTopK}
        isBusy={isBusy}
        isChatClearing={clearChatMutation.isPending}
        isChatLoading={messagesQuery.isLoading}
        messages={messages}
        notebookId={notebookId}
        onChatClear={() => {
          if (window.confirm("Clear this notebook chat history?")) {
            clearChatMutation.mutate();
          }
        }}
        onChatSend={(content, topK) => sendMessageMutation.mutate({ content, topK })}
        onChatTopKChange={setChatTopK}
      />
    </main>
  );
}
