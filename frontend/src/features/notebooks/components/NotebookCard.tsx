import { BookOpen, Check, Pencil, Trash2, X } from "lucide-react";
import { useEffect, useRef, useState } from "react";
import { Link } from "react-router-dom";
import type { Notebook } from "../../../shared/types/api";

type NotebookCardProps = {
  isUpdating: boolean;
  isDeleting: boolean;
  notebook: Notebook;
  onDelete: (notebookId: string) => void;
  onRename: (notebookId: string, title: string) => void;
};

export function NotebookCard({ isDeleting, isUpdating, notebook, onDelete, onRename }: NotebookCardProps) {
  const [isEditing, setIsEditing] = useState(false);
  const [draftTitle, setDraftTitle] = useState(notebook.title);
  const titleInputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    setDraftTitle(notebook.title);
  }, [notebook.title]);

  useEffect(() => {
    if (isEditing) {
      titleInputRef.current?.focus();
      titleInputRef.current?.select();
    }
  }, [isEditing]);

  function deleteNotebook(event: React.MouseEvent<HTMLButtonElement>) {
    event.preventDefault();
    event.stopPropagation();

    if (window.confirm(`Delete "${notebook.title}" and all its sources/chat history?`)) {
      onDelete(notebook.id);
    }
  }

  function startEditing(event: React.MouseEvent<HTMLButtonElement>) {
    event.preventDefault();
    event.stopPropagation();
    setIsEditing(true);
  }

  function cancelEditing(event?: React.MouseEvent<HTMLButtonElement>) {
    event?.preventDefault();
    event?.stopPropagation();
    setDraftTitle(notebook.title);
    setIsEditing(false);
  }

  function saveTitle(event?: React.MouseEvent<HTMLButtonElement>) {
    event?.preventDefault();
    event?.stopPropagation();

    const nextTitle = draftTitle.trim();

    if (nextTitle && nextTitle !== notebook.title) {
      onRename(notebook.id, nextTitle);
    }

    if (nextTitle) {
      setIsEditing(false);
    }
  }

  function handleTitleKeyDown(event: React.KeyboardEvent<HTMLInputElement>) {
    if (event.key === "Enter") {
      event.preventDefault();
      saveTitle();
    }

    if (event.key === "Escape") {
      event.preventDefault();
      setDraftTitle(notebook.title);
      setIsEditing(false);
    }
  }

  return (
    <Link className="notebook-card-link" to={`/notebooks/${notebook.id}`}>
      <div className="notebook-card-cover">
        <BookOpen size={22} />
      </div>
      <div className="notebook-card-body">
        <div>
          {isEditing ? (
            <input
              aria-label="Notebook title"
              className="notebook-title-input"
              disabled={isUpdating}
              onChange={(event) => setDraftTitle(event.target.value)}
              onClick={(event) => {
                event.preventDefault();
                event.stopPropagation();
              }}
              onKeyDown={handleTitleKeyDown}
              ref={titleInputRef}
              value={draftTitle}
            />
          ) : (
            <h3>{notebook.title}</h3>
          )}
          <p>{notebook.sourceCount} {notebook.sourceCount === 1 ? "source" : "sources"}</p>
        </div>
      </div>
      <div className="notebook-card-actions">
        {isEditing ? (
          <>
            <button
              aria-label={`Save ${notebook.title}`}
              className="icon-button icon-button--subtle"
              disabled={isUpdating || !draftTitle.trim()}
              onClick={saveTitle}
            >
              <Check size={15} />
            </button>
            <button
              aria-label="Cancel title edit"
              className="icon-button icon-button--subtle"
              disabled={isUpdating}
              onClick={cancelEditing}
            >
              <X size={15} />
            </button>
          </>
        ) : (
          <button
            aria-label={`Edit ${notebook.title}`}
            className="icon-button icon-button--subtle"
            disabled={isUpdating}
            onClick={startEditing}
          >
            <Pencil size={15} />
          </button>
        )}
        <button
          aria-label={`Delete ${notebook.title}`}
          className="icon-button icon-button--subtle"
          disabled={isDeleting}
          onClick={deleteNotebook}
        >
          <Trash2 size={15} />
        </button>
      </div>
    </Link>
  );
}
