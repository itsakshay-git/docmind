import { DeleteAccountSettingsSection } from "./settings-sections/DeleteAccountSettingsSection";
import { PasswordSettingsSection } from "./settings-sections/PasswordSettingsSection";
import { ProfileSettingsSection } from "./settings-sections/ProfileSettingsSection";
import { ThemeSettingsSection } from "./settings-sections/ThemeSettingsSection";
import type { SettingsSection, ThemeMode } from "../model/settingsSections";
import type { useDeleteUserAccount, useUpdateUserPassword, useUpdateUserProfile } from "../hooks/useUserProfile";

type SettingsDetailPanelProps = {
  activeSection: SettingsSection;
  canDeleteAccount: boolean;
  currentPassword: string;
  deleteAccountMutation: ReturnType<typeof useDeleteUserAccount>;
  deleteConfirmation: string;
  email: string;
  fullName: string;
  newPassword: string;
  theme: ThemeMode;
  updatePasswordMutation: ReturnType<typeof useUpdateUserPassword>;
  updateProfileMutation: ReturnType<typeof useUpdateUserProfile>;
  onCurrentPasswordChange: (password: string) => void;
  onDeleteConfirmationChange: (confirmation: string) => void;
  onFullNameChange: (fullName: string) => void;
  onNewPasswordChange: (password: string) => void;
  onThemeChange: (theme: ThemeMode) => void;
};

export function SettingsDetailPanel({
  activeSection,
  canDeleteAccount,
  currentPassword,
  deleteAccountMutation,
  deleteConfirmation,
  email,
  fullName,
  newPassword,
  theme,
  updatePasswordMutation,
  updateProfileMutation,
  onCurrentPasswordChange,
  onDeleteConfirmationChange,
  onFullNameChange,
  onNewPasswordChange,
  onThemeChange,
}: SettingsDetailPanelProps) {
  return (
    <section className="settings-panel settings-detail-panel">
      {activeSection === "profile" ? (
        <ProfileSettingsSection
          email={email}
          fullName={fullName}
          mutation={updateProfileMutation}
          onFullNameChange={onFullNameChange}
        />
      ) : null}

      {activeSection === "password" ? (
        <PasswordSettingsSection
          currentPassword={currentPassword}
          mutation={updatePasswordMutation}
          newPassword={newPassword}
          onCurrentPasswordChange={onCurrentPasswordChange}
          onNewPasswordChange={onNewPasswordChange}
        />
      ) : null}

      {activeSection === "appearance" ? (
        <ThemeSettingsSection theme={theme} onThemeChange={onThemeChange} />
      ) : null}

      {activeSection === "delete" ? (
        <DeleteAccountSettingsSection
          canDeleteAccount={canDeleteAccount}
          confirmation={deleteConfirmation}
          mutation={deleteAccountMutation}
          onConfirmationChange={onDeleteConfirmationChange}
        />
      ) : null}
    </section>
  );
}
