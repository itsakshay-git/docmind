import { settingsSections } from "../model/settingsSections";
import type { SettingsSection } from "../model/settingsSections";

type SettingsMenuProps = {
  activeSection: SettingsSection;
  onSectionChange: (section: SettingsSection) => void;
};

export function SettingsMenu({ activeSection, onSectionChange }: SettingsMenuProps) {
  return (
    <aside className="settings-menu" aria-label="Settings sections">
      {settingsSections.map((section) => (
        <button
          className={activeSection === section.id ? "active" : ""}
          key={section.id}
          onClick={() => onSectionChange(section.id)}
          type="button"
        >
          <span>{section.icon}</span>
          <strong>{section.label}</strong>
          <small>{section.description}</small>
        </button>
      ))}
    </aside>
  );
}
