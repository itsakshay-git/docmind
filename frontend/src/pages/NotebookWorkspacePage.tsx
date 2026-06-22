import { useState } from "react";
import { useParams } from "react-router-dom";
import { useNotebookChat } from "../features/chat/hooks/useNotebookChat";
import { ConfirmDialog } from "../shared/components/ConfirmDialog";
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
  const [sourceToDelete, setSourceToDelete] = useState<{ id: string; title: string } | null>(null);
  const [isClearChatDialogOpen, setIsClearChatDialogOpen] = useState(false);

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
  const isAddingSource =
    uploadMutation.isPending ||
    addWebUrlMutation.isPending ||
    addYouTubeUrlMutation.isPending ||
    addYouTubeTranscriptMutation.isPending;
  const isBusy =
    sendMessageMutation.isPending || clearChatMutation.isPending || isAddingSource || deleteDocumentMutation.isPending;

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
        onAddYouTubeTranscript={(url, title, transcript) =>
          addYouTubeTranscriptMutation.mutate({ url, title, transcript })
        }
        onAddYouTubeUrl={(url) => addYouTubeUrlMutation.mutate(url)}
        onDeleteSource={(documentId, documentTitle) => setSourceToDelete({ id: documentId, title: documentTitle })}
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
        onChatClear={() => setIsClearChatDialogOpen(true)}
        onChatSend={(content, topK) => sendMessageMutation.mutate({ content, topK })}
        onChatTopKChange={setChatTopK}
      />
      <ConfirmDialog
        confirmLabel="Delete source"
        description={`This will remove "${sourceToDelete?.title ?? "this source"}" and its indexed chunks from the notebook.`}
        isOpen={Boolean(sourceToDelete)}
        isPending={deleteDocumentMutation.isPending}
        title="Delete source?"
        onCancel={() => setSourceToDelete(null)}
        onConfirm={() => {
          if (!sourceToDelete) {
            return;
          }

          deleteDocumentMutation.mutate(sourceToDelete.id, {
            onSuccess() {
              setSourceToDelete(null);
            },
          });
        }}
      />
      <ConfirmDialog
        confirmLabel="Clear chat"
        description="This will delete the saved chat history for this notebook. Your sources will stay indexed."
        isOpen={isClearChatDialogOpen}
        isPending={clearChatMutation.isPending}
        title="Clear chat history?"
        onCancel={() => setIsClearChatDialogOpen(false)}
        onConfirm={() =>
          clearChatMutation.mutate(undefined, {
            onSuccess() {
              setIsClearChatDialogOpen(false);
            },
          })
        }
      />{" "}
    </main>
  );
}
