import { CheckCircle2, Clock3, FileText, Globe2, Trash2, TriangleAlert, UploadCloud, Youtube } from "lucide-react";
import type { ComponentType } from "react";
import type { DocumentSource } from "../../../shared/types";
import { EmptyState } from "../../../shared/components/EmptyState";

type SourceListPanelProps = {
  documents: DocumentSource[];
  isDeleting: boolean;
  onDelete: (documentId: string) => void;
};

type SourceMeta = {
  Icon: ComponentType<{ size?: number }>;
  label: string;
};

export function SourceListPanel({ documents, isDeleting, onDelete }: SourceListPanelProps) {
  return (
    <section className="source-list-panel">
      <div className="panel-heading source-panel-heading">
        <span>
          <FileText size={17} /> Sources
        </span>
        <small>{documents.length}</small>
      </div>
      {documents.length ? (
        <div className="source-list">
          {documents.map((document) => {
            const source = sourceMetaFor(document.sourceType);
            const status = statusMetaFor(document.status);
            const SourceIcon = source.Icon;
            const StatusIcon = status.Icon;

            return (
              <article className="source-row" key={document.id}>
                <div className="source-row__icon" aria-hidden="true">
                  <SourceIcon size={16} />
                </div>
                <div className="source-row__body">
                  <strong>{document.fileName}</strong>
                  <span className="source-row__meta">
                    <span>{source.label}</span>
                    <span className={`source-status source-status--${document.status.toLowerCase()}`}>
                      <StatusIcon size={12} /> {status.label}
                    </span>
                  </span>
                  {document.failureReason ? <small>{document.failureReason}</small> : null}
                </div>
                <button
                  aria-label={`Delete ${document.fileName}`}
                  className="icon-button icon-button--subtle"
                  disabled={isDeleting}
                  onClick={() => onDelete(document.id)}
                  type="button"
                >
                  <Trash2 size={15} />
                </button>
              </article>
            );
          })}
        </div>
      ) : (
        <EmptyState
          title="No sources yet"
          description="Add a PDF, website, or transcript to make this notebook searchable."
        />
      )}
    </section>
  );
}

function sourceMetaFor(sourceType: DocumentSource["sourceType"]): SourceMeta {
  if (sourceType === "WEB_URL") {
    return { Icon: Globe2, label: "Website" };
  }

  if (sourceType === "YOUTUBE" || sourceType === "YOUTUBE_TRANSCRIPT") {
    return { Icon: Youtube, label: "Transcript" };
  }

  return { Icon: FileText, label: "PDF" };
}

function statusMetaFor(status: DocumentSource["status"]) {
  if (status === "PROCESSED") {
    return { Icon: CheckCircle2, label: "Indexed" };
  }

  if (status === "FAILED") {
    return { Icon: TriangleAlert, label: "Failed" };
  }

  if (status === "PROCESSING") {
    return { Icon: Clock3, label: "Processing" };
  }

  return { Icon: UploadCloud, label: "Queued" };
}
