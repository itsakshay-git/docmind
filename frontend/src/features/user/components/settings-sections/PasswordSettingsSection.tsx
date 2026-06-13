import { KeyRound } from "lucide-react";
import type { UseMutationResult } from "@tanstack/react-query";
import { Button } from "../../../../shared/components/Button";
import { TextField } from "../../../../shared/components/TextField";

type PasswordSettingsSectionProps = {
  currentPassword: string;
  mutation: UseMutationResult<void, Error, void>;
  newPassword: string;
  onCurrentPasswordChange: (password: string) => void;
  onNewPasswordChange: (password: string) => void;
};

export function PasswordSettingsSection({
  currentPassword,
  mutation,
  newPassword,
  onCurrentPasswordChange,
  onNewPasswordChange,
}: PasswordSettingsSectionProps) {
  return (
    <>
      <div className="settings-section-heading">
        <span>
          <KeyRound size={18} /> Password
        </span>
        <p>Update your password with your current credential.</p>
      </div>
      <div className="settings-form">
        <TextField
          label="Current password"
          type="password"
          value={currentPassword}
          onChange={(event) => onCurrentPasswordChange(event.target.value)}
        />
        <TextField
          label="New password"
          type="password"
          value={newPassword}
          onChange={(event) => onNewPasswordChange(event.target.value)}
        />
        {mutation.error ? <p className="settings-error">{mutation.error.message}</p> : null}
        {mutation.isSuccess ? <p className="settings-success">Password updated.</p> : null}
        <Button
          disabled={!currentPassword || newPassword.length < 8 || mutation.isPending}
          onClick={() => mutation.mutate()}
        >
          Update password
        </Button>
      </div>
    </>
  );
}
