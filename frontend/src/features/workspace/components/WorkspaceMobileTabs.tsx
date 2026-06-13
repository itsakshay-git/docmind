import { workspaceMobileSections } from "../model/workspaceSections";
import type { WorkspaceMobileSection } from "../model/workspaceSections";

type WorkspaceMobileTabsProps = {
  activeSection: WorkspaceMobileSection;
  onSectionChange: (section: WorkspaceMobileSection) => void;
};

export function WorkspaceMobileTabs({ activeSection, onSectionChange }: WorkspaceMobileTabsProps) {
  return (
    <nav className="workspace-mobile-tabs" aria-label="Workspace sections">
      {workspaceMobileSections.map((section) => (
        <button
          aria-pressed={activeSection === section.id}
          className={activeSection === section.id ? "active" : ""}
          key={section.id}
          onClick={() => onSectionChange(section.id)}
          type="button"
        >
          {section.label}
        </button>
      ))}
    </nav>
  );
}
