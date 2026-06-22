import { EmptyState } from "../../../shared/components/EmptyState";
import type { Notebook } from "../../../shared/types";
import { CreateNotebookCard } from "./CreateNotebookCard";
import { NotebookCard } from "./NotebookCard";

type NotebookGridProps = {
  createErrorMessage?: string;
  createMessage: string;
  isCreating: boolean;
  isDeleting: boolean;
  isUpdating: boolean;
  notebooks: Notebook[];
  title: string;
  emptyDescription: string;
  emptyTitle: string;
  onCreate: () => void;
  onDelete: (notebookId: string, notebookTitle: string) => void;
  onRename: (notebookId: string, nextTitle: string) => void;
  onTitleChange: (title: string) => void;
};

export function NotebookGrid({
  createErrorMessage,
  createMessage,
  isCreating,
  isDeleting,
  isUpdating,
  notebooks,
  title,
  emptyDescription,
  emptyTitle,
  onCreate,
  onDelete,
  onRename,
  onTitleChange,
}: NotebookGridProps) {
  return (
    <section className="notebook-grid">
      <CreateNotebookCard
        errorMessage={createErrorMessage}
        isCreating={isCreating}
        message={createMessage}
        title={title}
        onCreate={onCreate}
        onTitleChange={onTitleChange}
      />
      {notebooks.map((notebook) => (
        <NotebookCard
          isDeleting={isDeleting}
          isUpdating={isUpdating}
          key={notebook.id}
          notebook={notebook}
          onDelete={onDelete}
          onRename={onRename}
        />
      ))}
      {!notebooks.length ? <EmptyState title={emptyTitle} description={emptyDescription} /> : null}
    </section>
  );
}
