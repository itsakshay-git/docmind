import { Eye, EyeOff } from "lucide-react";

type PasswordFieldProps = {
  isVisible: boolean;
  value: string;
  onToggleVisibility: () => void;
  onValueChange: (value: string) => void;
};

export function PasswordField({ isVisible, value, onToggleVisibility, onValueChange }: PasswordFieldProps) {
  return (
    <label className="field">
      <span>Password</span>
      <div className="password-field">
        <input
          onChange={(event) => onValueChange(event.target.value)}
          type={isVisible ? "text" : "password"}
          value={value}
        />
        <button aria-label={isVisible ? "Hide password" : "Show password"} onClick={onToggleVisibility} type="button">
          {isVisible ? <EyeOff size={17} /> : <Eye size={17} />}
        </button>
      </div>
    </label>
  );
}
