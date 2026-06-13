import { BrainCircuit, Check, LogOut, Menu, MoreVertical, Pencil, Plus, Settings, Trash2, X } from "lucide-react";
import { useMemo, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../features/auth/context/AuthContext";
import { NotebookCard } from "../features/notebooks/components/NotebookCard";
import { NotebookLibraryToolbar } from "../features/notebooks/components/NotebookLibraryToolbar";
import type { NotebookSort, NotebookView } from "../features/notebooks/components/NotebookLibraryToolbar";
import { useCreateNotebook, useDeleteNotebook, useNotebooks, useUpdateNotebookTitle } from "../features/notebooks/hooks/useNotebooks";
import { Button } from "../shared/components/Button";
import { EmptyState } from "../shared/components/EmptyState";

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
    const nextTitle =
      title.trim() || "Untitled notebook";

    createMutation.mutate(nextTitle, {
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

  function handleListRenameKeyDown(event: React.KeyboardEvent<HTMLInputElement>, notebookId: string, currentTitle: string) {
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

  function formatNotebookDate(value: string) {
    return new Intl.DateTimeFormat("en", {
      month: "short",
      day: "numeric",
      year: "numeric",
    }).format(new Date(value));
  }

  const notebooks = notebooksQuery.data ?? [];

  const visibleNotebooks = useMemo(() => {
    const query = searchTerm.trim().toLowerCase();

    return [...notebooks]
      .filter((notebook) =>
        query
          ? notebook.title.toLowerCase().includes(query)
          : true
      )
      .sort((first, second) => {
        if (sort === "title") {
          return first.title.localeCompare(second.title);
        }

        return new Date(second.createdAt).getTime() - new Date(first.createdAt).getTime();
      });
  }, [notebooks, searchTerm, sort]);

  return (
    <main className="notebooks-page">
      <header className="notebooks-shellbar">
        <div className="brand-lockup compact">
          <span>DocMind</span>
        </div>
        <div className="topbar-actions">
          <Link className="button button--secondary" to="/settings"><Settings size={16} /> Settings</Link>
          <Button icon={<LogOut size={16} />} onClick={auth.signOut} variant="ghost">Sign out</Button>
        </div>
        <div className="mobile-nav-menu">
          <button
            aria-expanded={isMobileMenuOpen}
            aria-label="Open navigation menu"
            className="icon-button"
            onClick={() => setIsMobileMenuOpen((current) => !current)}
            type="button"
          >
            <Menu size={18} />
          </button>
          {isMobileMenuOpen ? (
            <div className="mobile-nav-menu__panel">
              <Link onClick={() => setIsMobileMenuOpen(false)} to="/settings">
                <Settings size={15} /> Settings
              </Link>
              <button onClick={auth.signOut} type="button">
                <LogOut size={15} /> Sign out
              </button>
            </div>
          ) : null}
        </div>
      </header>

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

        {notebooks.length ? (
          view === "list" ? (
            <section className="notebook-list-table">
              <div className="notebook-list-head">
                <span>Title</span>
                <span>Sources</span>
                <span>Created</span>
                <span>Role</span>
                <span aria-hidden="true" />
              </div>
              {visibleNotebooks.map((notebook) => (
                <div className="notebook-list-row" key={notebook.id}>
                  {editingNotebookId === notebook.id ? (
                    <div className="notebook-list-title">
                      <span className="notebook-list-icon"><BrainCircuit size={15} /></span>
                      <input
                        aria-label="Notebook title"
                        className="notebook-title-input"
                        disabled={updateTitleMutation.isPending}
                        onChange={(event) => setEditingTitle(event.target.value)}
                        onKeyDown={(event) => handleListRenameKeyDown(event, notebook.id, notebook.title)}
                        value={editingTitle}
                      />
                    </div>
                  ) : (
                    <Link className="notebook-list-title" to={`/notebooks/${notebook.id}`}>
                      <span className="notebook-list-icon"><BrainCircuit size={15} /></span>
                      <strong>{notebook.title}</strong>
                    </Link>
                  )}
                  <span>{notebook.sourceCount} {notebook.sourceCount === 1 ? "Source" : "Sources"}</span>
                  <span>{formatNotebookDate(notebook.createdAt)}</span>
                  <span>Owner</span>
                  <div className="notebook-list-actions">
                    {editingNotebookId === notebook.id ? (
                      <>
                        <button
                          aria-label={`Save ${notebook.title}`}
                          className="icon-button icon-button--subtle"
                          disabled={updateTitleMutation.isPending || !editingTitle.trim()}
                          onClick={() => saveListRename(notebook.id, notebook.title)}
                          type="button"
                        >
                          <Check size={14} />
                        </button>
                        <button
                          aria-label="Cancel title edit"
                          className="icon-button icon-button--subtle"
                          disabled={updateTitleMutation.isPending}
                          onClick={cancelListRename}
                          type="button"
                        >
                          <X size={14} />
                        </button>
                      </>
                    ) : (
                      <button
                        aria-label={`Edit ${notebook.title}`}
                        className="icon-button icon-button--subtle"
                        disabled={updateTitleMutation.isPending}
                        onClick={() => startListRename(notebook.id, notebook.title)}
                        type="button"
                      >
                        <Pencil size={14} />
                      </button>
                    )}
                    <button
                      aria-label={`Delete ${notebook.title}`}
                      className="icon-button icon-button--subtle"
                      disabled={deleteMutation.isPending}
                      onClick={() => deleteNotebook(notebook.id, notebook.title)}
                      type="button"
                    >
                      <Trash2 size={14} />
                    </button>
                    <MoreVertical size={18} />
                  </div>
                </div>
              ))}
              {!visibleNotebooks.length ? (
                <EmptyState
                  title="No notebooks found"
                  description="Adjust your search to find a notebook."
                />
              ) : null}
            </section>
          ) : (
            <section className="notebook-grid">
              <article className="create-notebook-card">
                <button className="create-notebook-button" disabled={createMutation.isPending} onClick={createNotebook} type="button">
                  <span><Plus size={32} /></span>
                  <strong>{createMutation.isPending ? "Creating..." : "Create new notebook"}</strong>
                </button>
                <input
                  aria-label="Notebook title"
                  placeholder="Optional title"
                  value={title}
                  onChange={(event) => setTitle(event.target.value)}
                />
                {createMessage ? <p className="create-feedback">{createMessage}</p> : null}
                {createMutation.error ? <p className="create-feedback create-feedback--error">{createMutation.error.message}</p> : null}
              </article>
              {visibleNotebooks.map((notebook) => (
                <NotebookCard
                  isDeleting={deleteMutation.isPending}
                  isUpdating={updateTitleMutation.isPending}
                  key={notebook.id}
                  notebook={notebook}
                  onDelete={(notebookId) => deleteMutation.mutate(notebookId)}
                  onRename={(notebookId, nextTitle) => updateTitleMutation.mutate({ notebookId, nextTitle })}
                />
              ))}
              {!visibleNotebooks.length ? (
                <EmptyState
                  title="No notebooks found"
                  description="Adjust your search to find a notebook."
                />
              ) : null}
            </section>
          )
        ) : (
          <section className="notebook-grid">
            <article className="create-notebook-card">
              <button className="create-notebook-button" disabled={createMutation.isPending} onClick={createNotebook} type="button">
                <span><Plus size={32} /></span>
                <strong>{createMutation.isPending ? "Creating..." : "Create new notebook"}</strong>
              </button>
              <input
                aria-label="Notebook title"
                placeholder="Optional title"
                value={title}
                onChange={(event) => setTitle(event.target.value)}
              />
              {createMessage ? <p className="create-feedback">{createMessage}</p> : null}
              {createMutation.error ? <p className="create-feedback create-feedback--error">{createMutation.error.message}</p> : null}
            </article>
            <EmptyState
              title="No notebooks yet"
              description="Create your first notebook and add sources to start asking grounded questions."
            />
          </section>
        )}
      </div>
    </main>
  );
}
