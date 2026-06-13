import { Moon, Sun } from "lucide-react";
import type { ThemeMode } from "../../model/settingsSections";

type ThemeSettingsSectionProps = {
  theme: ThemeMode;
  onThemeChange: (theme: ThemeMode) => void;
};

export function ThemeSettingsSection({ theme, onThemeChange }: ThemeSettingsSectionProps) {
  return (
    <>
      <div className="settings-section-heading">
        <span><Moon size={18} /> Theme</span>
        <p>Choose how DocMind looks on this device.</p>
      </div>
      <div className="theme-options" role="radiogroup" aria-label="Theme mode">
        <button className={theme === "dark" ? "active" : ""} onClick={() => onThemeChange("dark")} type="button">
          <Moon size={18} />
          <strong>Dark</strong>
          <span>Default focused workspace</span>
        </button>
        <button className={theme === "light" ? "active" : ""} onClick={() => onThemeChange("light")} type="button">
          <Sun size={18} />
          <strong>Light</strong>
          <span>Brighter reading mode</span>
        </button>
      </div>
    </>
  );
}
