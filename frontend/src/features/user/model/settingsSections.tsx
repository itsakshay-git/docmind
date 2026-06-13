import { KeyRound, Moon, ShieldAlert, UserRound } from "lucide-react";
import type { ReactNode } from "react";

export type SettingsSection = "profile" | "password" | "appearance" | "delete";
export type ThemeMode = "dark" | "light";

export const settingsSections: Array<{
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
