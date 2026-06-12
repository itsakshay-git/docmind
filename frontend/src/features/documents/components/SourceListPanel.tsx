import { FileText, Trash2 } from "lucide-react";
import type { DocumentSource } from "../../../shared/types/api";
import { EmptyState } from "../../../shared/components/EmptyState";

type SourceListPanelProps = {
  documents: DocumentSource[];
  isDeleting: boolean;
  onDelete: (documentId: string) => void;
};

export function SourceListPanel({ documents, isDeleting, onDelete }: SourceListPanelProps) {
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
                <span>{document.status}</span>
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
        <EmptyState title="No sources yet" description="Upload a PDF to make this notebook searchable." />
      )}
    </section>
  );
}
