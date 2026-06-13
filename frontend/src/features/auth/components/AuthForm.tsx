import { Button } from "../../../shared/components/Button";
import { TextField } from "../../../shared/components/TextField";
import { DemoLoginCard } from "./DemoLoginCard";
import { PasswordField } from "./PasswordField";
import type { AuthMode } from "../model/demoCredentials";

type AuthFormProps = {
  email: string;
  errorMessage?: string;
  isPending: boolean;
  mode: AuthMode;
  password: string;
  showPassword: boolean;
  onEmailChange: (email: string) => void;
  onModeToggle: () => void;
  onPasswordChange: (password: string) => void;
  onSubmit: () => void;
  onTogglePasswordVisibility: () => void;
  onUseDemo: () => void;
};

export function AuthForm({
  email,
  errorMessage,
  isPending,
  mode,
  password,
  showPassword,
  onEmailChange,
  onModeToggle,
  onPasswordChange,
  onSubmit,
  onTogglePasswordVisibility,
  onUseDemo,
}: AuthFormProps) {
  return (
    <section className="login-card">
      <div>
        <p className="eyebrow">Welcome</p>
        <h2>{mode === "login" ? "Sign in to DocMind" : "Create your workspace"}</h2>
      </div>
      <TextField label="Email" onChange={(event) => onEmailChange(event.target.value)} type="email" value={email} />
      <PasswordField
        isVisible={showPassword}
        value={password}
        onToggleVisibility={onTogglePasswordVisibility}
        onValueChange={onPasswordChange}
      />
      <DemoLoginCard onUseDemo={onUseDemo} />
      {errorMessage ? <div className="form-error">{errorMessage}</div> : null}
      <Button disabled={isPending} onClick={onSubmit}>
        {isPending ? "Working" : mode === "login" ? "Login" : "Register"}
      </Button>
      <button className="text-action" onClick={onModeToggle} type="button">
        {mode === "login" ? "Need an account? Register" : "Already registered? Login"}
      </button>
    </section>
  );
}
