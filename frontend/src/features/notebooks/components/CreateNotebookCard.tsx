import { Plus } from "lucide-react";

type CreateNotebookCardProps = {
  errorMessage?: string;
  isCreating: boolean;
  message: string;
  title: string;
  onCreate: () => void;
  onTitleChange: (title: string) => void;
};

export function CreateNotebookCard({
  errorMessage,
  isCreating,
  message,
  title,
  onCreate,
  onTitleChange,
}: CreateNotebookCardProps) {
  return (
    <article className="create-notebook-card">
      <button className="create-notebook-button" disabled={isCreating} onClick={onCreate} type="button">
        <span><Plus size={32} /></span>
        <strong>{isCreating ? "Creating..." : "Create new notebook"}</strong>
      </button>
      <input
        aria-label="Notebook title"
        placeholder="Optional title"
        value={title}
        onChange={(event) => onTitleChange(event.target.value)}
      />
      {message ? <p className="create-feedback">{message}</p> : null}
      {errorMessage ? <p className="create-feedback create-feedback--error">{errorMessage}</p> : null}
    </article>
  );
}
