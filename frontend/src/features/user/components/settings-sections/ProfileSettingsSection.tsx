import { UserRound } from "lucide-react";
import type { UseMutationResult } from "@tanstack/react-query";
import { Button } from "../../../../shared/components/Button";
import { TextField } from "../../../../shared/components/TextField";
import type { UserProfile } from "../../../../shared/types";

type ProfileSettingsSectionProps = {
  email: string;
  fullName: string;
  mutation: UseMutationResult<UserProfile, Error, string>;
  onFullNameChange: (fullName: string) => void;
};

export function ProfileSettingsSection({ email, fullName, mutation, onFullNameChange }: ProfileSettingsSectionProps) {
  return (
    <>
      <div className="settings-section-heading">
        <span>
          <UserRound size={18} /> User
        </span>
        <p>Manage the identity shown in DocMind.</p>
      </div>
      <div className="settings-form">
        <TextField disabled label="Email" value={email} onChange={() => undefined} />
        <TextField label="Full name" value={fullName} onChange={(event) => onFullNameChange(event.target.value)} />
        {mutation.error ? <p className="settings-error">{mutation.error.message}</p> : null}
        {mutation.isSuccess ? <p className="settings-success">Profile saved.</p> : null}
        <Button disabled={mutation.isPending} onClick={() => mutation.mutate(fullName)}>
          Save profile
        </Button>
      </div>
    </>
  );
}
