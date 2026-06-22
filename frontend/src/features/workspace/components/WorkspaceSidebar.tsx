import { ArrowLeft, Library } from "lucide-react";
import { Link } from "react-router-dom";
import { SourceListPanel } from "../../documents/components/SourceListPanel";
import { SourceUploadPanel } from "../../documents/components/SourceUploadPanel";
import type { DocumentSource } from "../../../shared/types";
import type { WorkspaceMobileSection } from "../model/workspaceSections";

type WorkspaceSidebarProps = {
  activeMobileSection: WorkspaceMobileSection;
  documents: DocumentSource[];
  isAddingSource: boolean;
  isDeletingSource: boolean;
  notebookTitle: string;
  onAddWebUrl: (url: string) => void;
  onAddYouTubeTranscript: (url: string, title: string, transcript: string) => void;
  onAddYouTubeUrl: (url: string) => void;
  onDeleteSource: (documentId: string, documentTitle: string) => void;
  onUploadPdf: (file: File) => void;
};

export function WorkspaceSidebar({
  activeMobileSection,
  documents,
  isAddingSource,
  isDeletingSource,
  notebookTitle,
  onAddWebUrl,
  onAddYouTubeTranscript,
  onAddYouTubeUrl,
  onDeleteSource,
  onUploadPdf,
}: WorkspaceSidebarProps) {
  return (
    <aside
      className={`workspace-sidebar workspace-mobile-section workspace-mobile-section--sources ${activeMobileSection === "sources" ? "active" : ""}`}
    >
      <Link className="back-link" to="/notebooks">
        <ArrowLeft size={16} /> All notebooks
      </Link>
      <section className="sidebar-block">
        <div className="panel-heading">
          <span>
            <Library size={17} /> Notebook
          </span>
        </div>
        <h2>{notebookTitle}</h2>
        <p>Source-grounded workspace for upload, retrieval, chat, and study artifacts.</p>
      </section>
      <SourceUploadPanel
        isUploading={isAddingSource}
        onAddWebUrl={onAddWebUrl}
        onAddYouTubeTranscript={onAddYouTubeTranscript}
        onAddYouTubeUrl={onAddYouTubeUrl}
        onUpload={onUploadPdf}
      />
      <SourceListPanel documents={documents} isDeleting={isDeletingSource} onDelete={onDeleteSource} />
    </aside>
  );
}
