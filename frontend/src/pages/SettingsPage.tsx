import { ArrowLeft, KeyRound, Moon, ShieldAlert, Sun, UserRound } from "lucide-react";
import { useEffect, useState } from "react";
import type { ReactNode } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../features/auth/context/AuthContext";
import { useDeleteUserAccount, useUpdateUserPassword, useUpdateUserProfile, useUserProfile } from "../features/user/hooks/useUserProfile";
import { Button } from "../shared/components/Button";
import { TextField } from "../shared/components/TextField";

type SettingsSection = "profile" | "password" | "appearance" | "delete";
type ThemeMode = "dark" | "light";

const settingsSections: Array<{
  id: SettingsSection;
  label: string;
  description: string;
  icon: ReactNode;
}> = [
  {
    id: "profile",
    label: "User",
    description: "Name and account identity",
    icon: <UserRound size={17} />,
  },
  {
    id: "password",
    label: "Password",
    description: "Credential security",
    icon: <KeyRound size={17} />,
  },
  {
    id: "appearance",
    label: "Theme",
    description: "Dark or light mode",
    icon: <Moon size={17} />,
  },
  {
    id: "delete",
    label: "Delete account",
    description: "Permanent account removal",
    icon: <ShieldAlert size={17} />,
  },
];

export function SettingsPage() {
  const auth = useAuth();
  const navigate = useNavigate();
  const [activeSection, setActiveSection] = useState<SettingsSection>("profile");
  const [fullName, setFullName] = useState("");
  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [deleteConfirmation, setDeleteConfirmation] = useState("");
  const [theme, setTheme] = useState<ThemeMode>(() =>
    localStorage.getItem("docmind-theme") === "light" ? "light" : "dark"
  );

  const profileQuery = useUserProfile();

  useEffect(() => {
    setFullName(profileQuery.data?.fullName ?? "");
  }, [profileQuery.data?.fullName]);

  useEffect(() => {
    document.documentElement.dataset.theme = theme;
    localStorage.setItem("docmind-theme", theme);
  }, [theme]);

  const updateProfileMutation = useUpdateUserProfile();
  const updatePasswordMutation = useUpdateUserPassword(currentPassword, newPassword, () => {
      setCurrentPassword("");
      setNewPassword("");
    });
  const deleteAccountMutation = useDeleteUserAccount(() => {
      auth.signOut();
      navigate("/login", { replace: true });
    });

  const email = profileQuery.data?.email ?? "";
  const canDeleteAccount = deleteConfirmation.trim() === email;

  return (
    <main className="settings-page">
      <header className="settings-header">
        <Link className="back-link" to="/notebooks"><ArrowLeft size={16} /> Back to notebooks</Link>
        <h1>Settings</h1>
      </header>

      <section className="settings-layout">
        <aside className="settings-menu" aria-label="Settings sections">
          {settingsSections.map((section) => (
            <button
              className={activeSection === section.id ? "active" : ""}
              key={section.id}
              onClick={() => setActiveSection(section.id)}
              type="button"
            >
              <span>{section.icon}</span>
              <strong>{section.label}</strong>
              <small>{section.description}</small>
            </button>
          ))}
        </aside>

        <section className="settings-panel settings-detail-panel">
          {activeSection === "profile" ? (
            <>
              <div className="settings-section-heading">
                <span><UserRound size={18} /> User</span>
                <p>Manage the identity shown in DocMind.</p>
              </div>
              <div className="settings-form">
                <TextField disabled label="Email" value={email} onChange={() => undefined} />
                <TextField label="Full name" value={fullName} onChange={(event) => setFullName(event.target.value)} />
                {updateProfileMutation.error ? <p className="settings-error">{updateProfileMutation.error.message}</p> : null}
                {updateProfileMutation.isSuccess ? <p className="settings-success">Profile saved.</p> : null}
                <Button disabled={updateProfileMutation.isPending} onClick={() => updateProfileMutation.mutate(fullName)}>
                  Save profile
                </Button>
              </div>
            </>
          ) : null}

          {activeSection === "password" ? (
            <>
              <div className="settings-section-heading">
                <span><KeyRound size={18} /> Password</span>
                <p>Update your password with your current credential.</p>
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
            </>
          ) : null}

          {activeSection === "appearance" ? (
            <>
              <div className="settings-section-heading">
                <span><Moon size={18} /> Theme</span>
                <p>Choose how DocMind looks on this device.</p>
              </div>
              <div className="theme-options" role="radiogroup" aria-label="Theme mode">
                <button className={theme === "dark" ? "active" : ""} onClick={() => setTheme("dark")} type="button">
                  <Moon size={18} />
                  <strong>Dark</strong>
                  <span>Default focused workspace</span>
                </button>
                <button className={theme === "light" ? "active" : ""} onClick={() => setTheme("light")} type="button">
                  <Sun size={18} />
                  <strong>Light</strong>
                  <span>Brighter reading mode</span>
                </button>
              </div>
            </>
          ) : null}

          {activeSection === "delete" ? (
            <>
              <div className="settings-section-heading">
                <span><ShieldAlert size={18} /> Delete account</span>
                <p>Permanently remove your account, notebooks, sources, embeddings, and chat history.</p>
              </div>
              <div className="danger-zone">
                <p>Type your email address to confirm deletion.</p>
                <TextField label="Confirm email" value={deleteConfirmation} onChange={(event) => setDeleteConfirmation(event.target.value)} />
                {deleteAccountMutation.error ? <p className="settings-error">{deleteAccountMutation.error.message}</p> : null}
                <Button
                  disabled={!canDeleteAccount || deleteAccountMutation.isPending}
                  icon={<ShieldAlert size={15} />}
                  onClick={() => deleteAccountMutation.mutate()}
                  variant="secondary"
                >
                  Delete account
                </Button>
              </div>
            </>
          ) : null}
        </section>
      </section>
    </main>
  );
}
