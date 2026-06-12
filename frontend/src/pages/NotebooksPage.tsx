import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { BrainCircuit, Plus, RefreshCw, Search } from "lucide-react";
import { useState } from "react";
import { Link } from "react-router-dom";
import { useAuth } from "../features/auth/context/AuthContext";
import { NotebookCard } from "../features/notebooks/components/NotebookCard";
import { notebooksApi } from "../features/notebooks/api/notebooksApi";
import { Button } from "../shared/components/Button";
import { EmptyState } from "../shared/components/EmptyState";

export function NotebooksPage() {
  const auth = useAuth();
  const queryClient = useQueryClient();
  const [title, setTitle] = useState("");

  const notebooksQuery = useQuery({
    queryKey: ["notebooks"],
    queryFn: notebooksApi.list,
  });

  const createMutation = useMutation({
    mutationFn: notebooksApi.create,
    onSuccess() {
      setTitle("");
      queryClient.invalidateQueries({ queryKey: ["notebooks"] });
    },
  });

  const deleteMutation = useMutation({
    mutationFn: notebooksApi.delete,
    onSuccess() {
      queryClient.invalidateQueries({ queryKey: ["notebooks"] });
      queryClient.invalidateQueries({ queryKey: ["documents"] });
    },
  });

  return (
    <main className="notebooks-page">
      <header className="notebooks-topbar">
        <div className="brand-lockup compact">
          <div className="brand-symbol"><BrainCircuit size={24} /></div>
          <span>DocMind</span>
        </div>
        <div className="topbar-actions">
          <Button icon={<RefreshCw size={16} />} onClick={() => notebooksQuery.refetch()} variant="secondary">
            Refresh
          </Button>
          <Link className="button button--secondary" to="/settings">Settings</Link>
          <Button onClick={auth.signOut} variant="ghost">Sign out</Button>
        </div>
      </header>

      <section className="notebooks-hero">
        <p className="eyebrow">Notebook library</p>
        <h1>Choose a notebook or start a new research space.</h1>
        <p>Each notebook keeps its own sources, chat history surface, and study-generation workspace.</p>
      </section>

      <section className="create-notebook-panel">
        <Search size={18} />
        <input placeholder="Notebook title" value={title} onChange={(event) => setTitle(event.target.value)} />
        <Button
          disabled={!title.trim() || createMutation.isPending}
          icon={<Plus size={16} />}
          onClick={() => createMutation.mutate(title.trim())}
        >
          Create notebook
        </Button>
      </section>

      {notebooksQuery.data?.length ? (
        <section className="notebook-grid">
          {notebooksQuery.data.map((notebook) => (
            <NotebookCard
              isDeleting={deleteMutation.isPending}
              key={notebook.id}
              notebook={notebook}
              onDelete={(notebookId) => deleteMutation.mutate(notebookId)}
            />
          ))}
        </section>
      ) : (
        <EmptyState
          title="No notebooks yet"
          description="Create your first notebook, upload a PDF, and start asking grounded questions."
        />
      )}
    </main>
  );
}
