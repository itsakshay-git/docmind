import { useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../features/auth/context/AuthContext";
import { NotebookGrid } from "../features/notebooks/components/NotebookGrid";
import { NotebookLibraryHeader } from "../features/notebooks/components/NotebookLibraryHeader";
import { NotebookLibraryToolbar } from "../features/notebooks/components/NotebookLibraryToolbar";
import type { NotebookSort, NotebookView } from "../features/notebooks/components/NotebookLibraryToolbar";
import { NotebookListTable } from "../features/notebooks/components/NotebookListTable";
import {
  useCreateNotebook,
  useDeleteNotebook,
  useNotebooks,
  useUpdateNotebookTitle,
} from "../features/notebooks/hooks/useNotebooks";

export function NotebooksPage() {
  const auth = useAuth();
  const navigate = useNavigate();
  const [title, setTitle] = useState("");
  const [createMessage, setCreateMessage] = useState("");
  const [searchTerm, setSearchTerm] = useState("");
  const [view, setView] = useState<NotebookView>("grid");
  const [sort, setSort] = useState<NotebookSort>("recent");
  const [editingNotebookId, setEditingNotebookId] = useState("");
  const [editingTitle, setEditingTitle] = useState("");
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);

  const notebooksQuery = useNotebooks();
  const createMutation = useCreateNotebook();
  const deleteMutation = useDeleteNotebook();
  const updateTitleMutation = useUpdateNotebookTitle();

  function handleNotebookCreated(savedNotebook: { title: string }) {
    setTitle("");
    setCreateMessage(`Created "${savedNotebook.title}"`);
    window.setTimeout(() => setCreateMessage(""), 2500);
  }

  function createNotebook() {
    createMutation.mutate(title.trim() || "Untitled notebook", {
      onSuccess: handleNotebookCreated,
    });
  }

  function createAndOpenNotebook() {
    createMutation.mutate(title.trim() || "Untitled notebook", {
      onSuccess(savedNotebook) {
        handleNotebookCreated(savedNotebook);
        navigate(`/notebooks/${savedNotebook.id}`);
      },
    });
  }

  function startListRename(notebookId: string, currentTitle: string) {
    setEditingNotebookId(notebookId);
    setEditingTitle(currentTitle);
  }

  function saveListRename(notebookId: string, currentTitle: string) {
    const nextTitle = editingTitle.trim();

    if (nextTitle && nextTitle !== currentTitle) {
      updateTitleMutation.mutate(
        { notebookId, nextTitle },
        {
          onSuccess() {
            setEditingNotebookId("");
            setEditingTitle("");
          },
        }
      );
    }

    if (nextTitle) {
      setEditingNotebookId("");
      setEditingTitle("");
    }
  }

  function cancelListRename() {
    setEditingNotebookId("");
    setEditingTitle("");
  }

  function handleListRenameKeyDown(
    event: React.KeyboardEvent<HTMLInputElement>,
    notebookId: string,
    currentTitle: string
  ) {
    if (event.key === "Enter") {
      event.preventDefault();
      saveListRename(notebookId, currentTitle);
    }

    if (event.key === "Escape") {
      event.preventDefault();
      cancelListRename();
    }
  }

  function deleteNotebook(notebookId: string, notebookTitle: string) {
    if (window.confirm(`Delete "${notebookTitle}" and all its sources/chat history?`)) {
      deleteMutation.mutate(notebookId);
    }
  }

  const notebooks = useMemo(() => notebooksQuery.data ?? [], [notebooksQuery.data]);
  const visibleNotebooks = useMemo(() => {
    const query = searchTerm.trim().toLowerCase();

    return [...notebooks]
      .filter((notebook) => (query ? notebook.title.toLowerCase().includes(query) : true))
      .sort((first, second) => {
        if (sort === "title") {
          return first.title.localeCompare(second.title);
        }

        return new Date(second.createdAt).getTime() - new Date(first.createdAt).getTime();
      });
  }, [notebooks, searchTerm, sort]);

  return (
    <main className="notebooks-page">
      <NotebookLibraryHeader
        isMobileMenuOpen={isMobileMenuOpen}
        onMobileMenuClose={() => setIsMobileMenuOpen(false)}
        onMobileMenuToggle={() => setIsMobileMenuOpen((current) => !current)}
        onSignOut={auth.signOut}
      />

      <div className="notebook-library-content">
        <NotebookLibraryToolbar
          isCreating={createMutation.isPending}
          searchTerm={searchTerm}
          sort={sort}
          view={view}
          onCreate={createAndOpenNotebook}
          onSearchTermChange={setSearchTerm}
          onSortToggle={() => setSort(sort === "recent" ? "title" : "recent")}
          onViewChange={setView}
        />

        <section className="notebook-library-heading">
          <h1>Recent notebooks</h1>
        </section>

        {notebooks.length && view === "list" ? (
          <NotebookListTable
            editingNotebookId={editingNotebookId}
            editingTitle={editingTitle}
            isDeleting={deleteMutation.isPending}
            isUpdating={updateTitleMutation.isPending}
            notebooks={visibleNotebooks}
            onCancelRename={cancelListRename}
            onDelete={deleteNotebook}
            onEditingTitleChange={setEditingTitle}
            onRenameKeyDown={handleListRenameKeyDown}
            onSaveRename={saveListRename}
            onStartRename={startListRename}
          />
        ) : (
          <NotebookGrid
            createErrorMessage={createMutation.error?.message}
            createMessage={createMessage}
            emptyDescription={
              notebooks.length
                ? "Adjust your search to find a notebook."
                : "Create your first notebook and add sources to start asking grounded questions."
            }
            emptyTitle={notebooks.length ? "No notebooks found" : "No notebooks yet"}
            isCreating={createMutation.isPending}
            isDeleting={deleteMutation.isPending}
            isUpdating={updateTitleMutation.isPending}
            notebooks={notebooks.length ? visibleNotebooks : []}
            title={title}
            onCreate={createNotebook}
            onDelete={(notebookId) => deleteMutation.mutate(notebookId)}
            onRename={(notebookId, nextTitle) => updateTitleMutation.mutate({ notebookId, nextTitle })}
            onTitleChange={setTitle}
          />
        )}
      </div>
    </main>
  );
}
