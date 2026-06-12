import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { ArrowLeft, FileText, KeyRound, Trash2, UserRound } from "lucide-react";
import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { documentsApi } from "../features/documents/api/documentsApi";
import { notebooksApi } from "../features/notebooks/api/notebooksApi";
import { userApi } from "../features/user/api/userApi";
import { Button } from "../shared/components/Button";
import { EmptyState } from "../shared/components/EmptyState";
import { TextField } from "../shared/components/TextField";

export function SettingsPage() {
  const queryClient = useQueryClient();
  const [fullName, setFullName] = useState("");
  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");

  const profileQuery = useQuery({
    queryKey: ["user-profile"],
    queryFn: userApi.getProfile,
  });

  const notebooksQuery = useQuery({
    queryKey: ["notebooks"],
    queryFn: notebooksApi.list,
  });

  const documentsQuery = useQuery({
    queryKey: ["documents"],
    queryFn: documentsApi.listMine,
  });

  useEffect(() => {
    setFullName(profileQuery.data?.fullName ?? "");
  }, [profileQuery.data?.fullName]);

  const updateProfileMutation = useMutation({
    mutationFn: userApi.updateProfile,
    onSuccess(profile) {
      queryClient.setQueryData(["user-profile"], profile);
    },
  });

  const updatePasswordMutation = useMutation({
    mutationFn: () => userApi.updatePassword(currentPassword, newPassword),
    onSuccess() {
      setCurrentPassword("");
      setNewPassword("");
    },
  });

  const deleteNotebookMutation = useMutation({
    mutationFn: notebooksApi.delete,
    onSuccess() {
      queryClient.invalidateQueries({ queryKey: ["notebooks"] });
      queryClient.invalidateQueries({ queryKey: ["documents"] });
    },
  });

  const deleteDocumentMutation = useMutation({
    mutationFn: documentsApi.delete,
    onSuccess() {
      queryClient.invalidateQueries({ queryKey: ["documents"] });
      queryClient.invalidateQueries({ queryKey: ["embedding-count"] });
    },
  });

  const notebooksById = new Map(
    (notebooksQuery.data ?? []).map((notebook) => [notebook.id, notebook.title])
  );

  return (
    <main className="settings-page">
      <header className="settings-header">
        <Link className="back-link" to="/notebooks"><ArrowLeft size={16} /> Back to notebooks</Link>
        <h1>Settings</h1>
      </header>

      <section className="settings-grid">
        <section className="settings-panel">
          <div className="panel-heading">
            <span><UserRound size={17} /> Profile</span>
          </div>
          <div className="settings-form">
            <TextField disabled label="Email" value={profileQuery.data?.email ?? ""} onChange={() => undefined} />
            <TextField label="Full name" value={fullName} onChange={(event) => setFullName(event.target.value)} />
            {updateProfileMutation.error ? <p className="settings-error">{updateProfileMutation.error.message}</p> : null}
            <Button disabled={updateProfileMutation.isPending} onClick={() => updateProfileMutation.mutate(fullName)}>
              Save profile
            </Button>
          </div>
        </section>

        <section className="settings-panel">
          <div className="panel-heading">
            <span><KeyRound size={17} /> Password</span>
          </div>
          <div className="settings-form">
            <TextField label="Current password" type="password" value={currentPassword} onChange={(event) => setCurrentPassword(event.target.value)} />
            <TextField label="New password" type="password" value={newPassword} onChange={(event) => setNewPassword(event.target.value)} />
            {updatePasswordMutation.error ? <p className="settings-error">{updatePasswordMutation.error.message}</p> : null}
            {updatePasswordMutation.isSuccess ? <p className="settings-success">Password updated.</p> : null}
            <Button
              disabled={!currentPassword || newPassword.length < 8 || updatePasswordMutation.isPending}
              onClick={() => updatePasswordMutation.mutate()}
            >
              Update password
            </Button>
          </div>
        </section>
      </section>

      <section className="settings-panel">
        <div className="panel-heading">
          <span><FileText size={17} /> Notebooks</span>
        </div>
        {notebooksQuery.data?.length ? (
          <div className="settings-list">
            {notebooksQuery.data.map((notebook) => (
              <article className="settings-row" key={notebook.id}>
                <div>
                  <strong>{notebook.title}</strong>
                  <span>{notebook.id}</span>
                </div>
                <Button
                  disabled={deleteNotebookMutation.isPending}
                  icon={<Trash2 size={15} />}
                  onClick={() => deleteNotebookMutation.mutate(notebook.id)}
                  variant="secondary"
                >
                  Delete
                </Button>
              </article>
            ))}
          </div>
        ) : (
          <EmptyState title="No notebooks" description="Created notebooks will appear here." />
        )}
      </section>

      <section className="settings-panel">
        <div className="panel-heading">
          <span><FileText size={17} /> Documents</span>
        </div>
        {documentsQuery.data?.length ? (
          <div className="settings-list">
            {documentsQuery.data.map((document) => (
              <article className="settings-row" key={document.id}>
                <div>
                  <strong>{document.fileName}</strong>
                  <span>{notebooksById.get(document.notebookId) ?? "Notebook"} · {document.status}</span>
                </div>
                <Button
                  disabled={deleteDocumentMutation.isPending}
                  icon={<Trash2 size={15} />}
                  onClick={() => deleteDocumentMutation.mutate(document.id)}
                  variant="secondary"
                >
                  Delete
                </Button>
              </article>
            ))}
          </div>
        ) : (
          <EmptyState title="No documents" description="Uploaded sources will appear here." />
        )}
      </section>
    </main>
  );
}
