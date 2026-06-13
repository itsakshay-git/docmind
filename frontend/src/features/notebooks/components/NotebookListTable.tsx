import { BrainCircuit, Check, MoreVertical, Pencil, Trash2, X } from "lucide-react";
import { Link } from "react-router-dom";
import { EmptyState } from "../../../shared/components/EmptyState";
import type { Notebook } from "../../../shared/types";

type NotebookListTableProps = {
  editingNotebookId: string;
  editingTitle: string;
  isDeleting: boolean;
  isUpdating: boolean;
  notebooks: Notebook[];
  onCancelRename: () => void;
  onDelete: (notebookId: string, notebookTitle: string) => void;
  onEditingTitleChange: (title: string) => void;
  onRenameKeyDown: (event: React.KeyboardEvent<HTMLInputElement>, notebookId: string, currentTitle: string) => void;
  onSaveRename: (notebookId: string, currentTitle: string) => void;
  onStartRename: (notebookId: string, currentTitle: string) => void;
};

export function NotebookListTable({
  editingNotebookId,
  editingTitle,
  isDeleting,
  isUpdating,
  notebooks,
  onCancelRename,
  onDelete,
  onEditingTitleChange,
  onRenameKeyDown,
  onSaveRename,
  onStartRename,
}: NotebookListTableProps) {
  return (
    <section className="notebook-list-table">
      <div className="notebook-list-head">
        <span>Title</span>
        <span>Sources</span>
        <span>Created</span>
        <span>Role</span>
        <span aria-hidden="true" />
      </div>
      {notebooks.map((notebook) => (
        <div className="notebook-list-row" key={notebook.id}>
          {editingNotebookId === notebook.id ? (
            <div className="notebook-list-title">
              <span className="notebook-list-icon">
                <BrainCircuit size={15} />
              </span>
              <input
                aria-label="Notebook title"
                className="notebook-title-input"
                disabled={isUpdating}
                onChange={(event) => onEditingTitleChange(event.target.value)}
                onKeyDown={(event) => onRenameKeyDown(event, notebook.id, notebook.title)}
                value={editingTitle}
              />
            </div>
          ) : (
            <Link className="notebook-list-title" to={`/notebooks/${notebook.id}`}>
              <span className="notebook-list-icon">
                <BrainCircuit size={15} />
              </span>
              <strong>{notebook.title}</strong>
            </Link>
          )}
          <span>
            {notebook.sourceCount} {notebook.sourceCount === 1 ? "Source" : "Sources"}
          </span>
          <span>{formatNotebookDate(notebook.createdAt)}</span>
          <span>Owner</span>
          <div className="notebook-list-actions">
            {editingNotebookId === notebook.id ? (
              <>
                <button
                  aria-label={`Save ${notebook.title}`}
                  className="icon-button icon-button--subtle"
                  disabled={isUpdating || !editingTitle.trim()}
                  onClick={() => onSaveRename(notebook.id, notebook.title)}
                  type="button"
                >
                  <Check size={14} />
                </button>
                <button
                  aria-label="Cancel title edit"
                  className="icon-button icon-button--subtle"
                  disabled={isUpdating}
                  onClick={onCancelRename}
                  type="button"
                >
                  <X size={14} />
                </button>
              </>
            ) : (
              <button
                aria-label={`Edit ${notebook.title}`}
                className="icon-button icon-button--subtle"
                disabled={isUpdating}
                onClick={() => onStartRename(notebook.id, notebook.title)}
                type="button"
              >
                <Pencil size={14} />
              </button>
            )}
            <button
              aria-label={`Delete ${notebook.title}`}
              className="icon-button icon-button--subtle"
              disabled={isDeleting}
              onClick={() => onDelete(notebook.id, notebook.title)}
              type="button"
            >
              <Trash2 size={14} />
            </button>
            <MoreVertical size={18} />
          </div>
        </div>
      ))}
      {!notebooks.length ? (
        <EmptyState title="No notebooks found" description="Adjust your search to find a notebook." />
      ) : null}
    </section>
  );
}

function formatNotebookDate(value: string) {
  return new Intl.DateTimeFormat("en", {
    month: "short",
    day: "numeric",
    year: "numeric",
  }).format(new Date(value));
}
