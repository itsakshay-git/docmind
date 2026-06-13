import { ArrowLeft, Library } from "lucide-react";
import { Link, useParams } from "react-router-dom";
import { useState } from "react";
import { useNotebookChat } from "../features/chat/hooks/useNotebookChat";
import { ChatPanel } from "../features/chat/components/ChatPanel";
import { SourceListPanel } from "../features/documents/components/SourceListPanel";
import { SourceUploadPanel } from "../features/documents/components/SourceUploadPanel";
import { useNotebookDocuments, useSourceMutations } from "../features/documents/hooks/useNotebookDocuments";
import { StudioPanel } from "../features/studio/components/StudioPanel";
import { useNotebooks } from "../features/notebooks/hooks/useNotebooks";

type WorkspaceMobileSection = "sources" | "chat" | "studio";

const workspaceMobileSections: Array<{
  id: WorkspaceMobileSection;
  label: string;
}> = [
  { id: "sources", label: "Sources" },
  { id: "chat", label: "Chat" },
  { id: "studio", label: "Studio" },
];

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
      <div className="workspace-mobile-topbar">
        <Link className="back-link" to="/notebooks"><ArrowLeft size={16} /> All notebooks</Link>
      </div>

      <nav className="workspace-mobile-tabs" aria-label="Workspace sections">
        {workspaceMobileSections.map((section) => (
          <button
            aria-pressed={activeMobileSection === section.id}
            className={activeMobileSection === section.id ? "active" : ""}
            key={section.id}
            onClick={() => setActiveMobileSection(section.id)}
            type="button"
          >
            {section.label}
          </button>
        ))}
      </nav>

      <aside className={`workspace-sidebar workspace-mobile-section workspace-mobile-section--sources ${activeMobileSection === "sources" ? "active" : ""}`}>
        <Link className="back-link" to="/notebooks"><ArrowLeft size={16} /> All notebooks</Link>
        <section className="sidebar-block">
          <div className="panel-heading"><span><Library size={17} /> Notebook</span></div>
          <h2>{notebook?.title ?? "Notebook"}</h2>
          <p>Source-grounded workspace for upload, retrieval, chat, and study artifacts.</p>
        </section>
        <SourceUploadPanel
          isUploading={isAddingSource}
          onAddWebUrl={(url) => addWebUrlMutation.mutate(url)}
          onAddYouTubeTranscript={(url, title, transcript) => addYouTubeTranscriptMutation.mutate({ url, title, transcript })}
          onAddYouTubeUrl={(url) => addYouTubeUrlMutation.mutate(url)}
          onUpload={(file) => uploadMutation.mutate(file)}
        />
        <SourceListPanel
          documents={notebookDocumentsQuery.data ?? []}
          isDeleting={deleteDocumentMutation.isPending}
          onDelete={(documentId) => deleteDocumentMutation.mutate(documentId)}
        />
      </aside>

      <section className="workspace-main">
        <section className="workspace-content-grid">
          <div className={`workspace-mobile-section workspace-mobile-section--chat ${activeMobileSection === "chat" ? "active" : ""}`}>
            <ChatPanel
              errorMessage={messagesQuery.error instanceof Error ? messagesQuery.error.message : undefined}
              isBusy={isBusy}
              isClearing={clearChatMutation.isPending}
              isLoading={messagesQuery.isLoading}
              messages={messages}
              topK={chatTopK}
              onClear={() => {
                if (window.confirm("Clear this notebook chat history?")) {
                  clearChatMutation.mutate();
                }
              }}
              onSend={(content, topK) => sendMessageMutation.mutate({ content, topK })}
              onTopKChange={setChatTopK}
            />
          </div>
          <div className={`workspace-mobile-section workspace-mobile-section--studio ${activeMobileSection === "studio" ? "active" : ""}`}>
            <StudioPanel notebookId={notebookId} />
          </div>
        </section>
      </section>
    </main>
  );
}
