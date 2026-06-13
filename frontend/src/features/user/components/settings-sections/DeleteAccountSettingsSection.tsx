import { ShieldAlert } from "lucide-react";
import type { UseMutationResult } from "@tanstack/react-query";
import { Button } from "../../../../shared/components/Button";
import { TextField } from "../../../../shared/components/TextField";

type DeleteAccountSettingsSectionProps = {
  canDeleteAccount: boolean;
  confirmation: string;
  mutation: UseMutationResult<void, Error, void>;
  onConfirmationChange: (confirmation: string) => void;
};

export function DeleteAccountSettingsSection({
  canDeleteAccount,
  confirmation,
  mutation,
  onConfirmationChange,
}: DeleteAccountSettingsSectionProps) {
  return (
    <>
      <div className="settings-section-heading">
        <span>
          <ShieldAlert size={18} /> Delete account
        </span>
        <p>Permanently remove your account, notebooks, sources, embeddings, and chat history.</p>
      </div>
      <div className="danger-zone">
        <p>Type your email address to confirm deletion.</p>
        <TextField
          label="Confirm email"
          value={confirmation}
          onChange={(event) => onConfirmationChange(event.target.value)}
        />
        {mutation.error ? <p className="settings-error">{mutation.error.message}</p> : null}
        <Button
          disabled={!canDeleteAccount || mutation.isPending}
          icon={<ShieldAlert size={15} />}
          onClick={() => mutation.mutate()}
          variant="secondary"
        >
          Delete account
        </Button>
      </div>
    </>
  );
}
