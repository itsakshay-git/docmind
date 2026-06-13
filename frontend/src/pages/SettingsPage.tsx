import { ArrowLeft } from "lucide-react";
import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../features/auth/context/AuthContext";
import { SettingsDetailPanel } from "../features/user/components/SettingsDetailPanel";
import { SettingsMenu } from "../features/user/components/SettingsMenu";
import { useDeleteUserAccount, useUpdateUserPassword, useUpdateUserProfile, useUserProfile } from "../features/user/hooks/useUserProfile";
import type { SettingsSection, ThemeMode } from "../features/user/model/settingsSections";

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
        <SettingsMenu activeSection={activeSection} onSectionChange={setActiveSection} />
        <SettingsDetailPanel
          activeSection={activeSection}
          canDeleteAccount={canDeleteAccount}
          currentPassword={currentPassword}
          deleteAccountMutation={deleteAccountMutation}
          deleteConfirmation={deleteConfirmation}
          email={email}
          fullName={fullName}
          newPassword={newPassword}
          theme={theme}
          updatePasswordMutation={updatePasswordMutation}
          updateProfileMutation={updateProfileMutation}
          onCurrentPasswordChange={setCurrentPassword}
          onDeleteConfirmationChange={setDeleteConfirmation}
          onFullNameChange={setFullName}
          onNewPasswordChange={setNewPassword}
          onThemeChange={setTheme}
        />
      </section>
    </main>
  );
}
