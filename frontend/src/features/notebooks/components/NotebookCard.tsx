import { BookOpen, MessageSquareText, MoreHorizontal, Trash2 } from "lucide-react";
import { Link } from "react-router-dom";
import type { Notebook } from "../../../shared/types/api";

type NotebookCardProps = {
  isDeleting: boolean;
  notebook: Notebook;
  onDelete: (notebookId: string) => void;
};

export function NotebookCard({ isDeleting, notebook, onDelete }: NotebookCardProps) {
  function deleteNotebook(event: React.MouseEvent<HTMLButtonElement>) {
    event.preventDefault();
    event.stopPropagation();

    if (window.confirm(`Delete "${notebook.title}" and all its sources/chat history?`)) {
      onDelete(notebook.id);
    }
  }

  return (
    <Link className="notebook-card-link" to={`/notebooks/${notebook.id}`}>
      <div className="notebook-card-cover">
        <BookOpen size={22} />
      </div>
      <div className="notebook-card-body">
        <div>
          <h3>{notebook.title}</h3>
          <p>Notebook workspace</p>
        </div>
        <div className="notebook-card-meta">
          <span><MessageSquareText size={14} /> Chat ready</span>
          <span><BookOpen size={14} /> Sources</span>
        </div>
      </div>
      <div className="notebook-card-actions">
        <button aria-label="More notebook actions" className="icon-button icon-button--subtle" onClick={(event) => event.preventDefault()}>
          <MoreHorizontal size={16} />
        </button>
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
