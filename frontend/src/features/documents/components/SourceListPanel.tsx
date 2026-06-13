import { FileText, Trash2 } from "lucide-react";
import type { DocumentSource } from "../../../shared/types";
import { EmptyState } from "../../../shared/components/EmptyState";

type SourceListPanelProps = {
  documents: DocumentSource[];
  isDeleting: boolean;
  onDelete: (documentId: string) => void;
};

export function SourceListPanel({ documents, isDeleting, onDelete }: SourceListPanelProps) {
  function labelFor(sourceType: DocumentSource["sourceType"]) {
    if (sourceType === "WEB_URL") {
      return "WEB";
    }

    if (sourceType === "YOUTUBE_TRANSCRIPT") {
      return "YOUTUBE";
    }

    return sourceType;
  }

  return (
    <section className="source-list-panel">
      <div className="panel-heading">
        <span><FileText size={17} /> Files</span>
      </div>
      {documents.length ? (
        <div className="source-list">
          {documents.map((document) => (
            <article className="source-row" key={document.id}>
              <FileText size={16} />
              <div>
                <strong>{document.fileName}</strong>
                <span>{labelFor(document.sourceType)} | {document.status}</span>
                {document.failureReason ? <small>{document.failureReason}</small> : null}
              </div>
              <button
                aria-label={`Delete ${document.fileName}`}
                className="icon-button icon-button--subtle"
                disabled={isDeleting}
                onClick={() => onDelete(document.id)}
              >
                <Trash2 size={15} />
              </button>
            </article>
          ))}
        </div>
      ) : (
        <EmptyState title="No sources yet" description="Add a PDF, website, or YouTube transcript to make this notebook searchable." />
      )}
    </section>
  );
}
