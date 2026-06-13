import { LogOut, Menu, Settings } from "lucide-react";
import { Link } from "react-router-dom";
import { Button } from "../../../shared/components/Button";

type NotebookLibraryHeaderProps = {
  isMobileMenuOpen: boolean;
  onMobileMenuToggle: () => void;
  onMobileMenuClose: () => void;
  onSignOut: () => void;
};

export function NotebookLibraryHeader({
  isMobileMenuOpen,
  onMobileMenuToggle,
  onMobileMenuClose,
  onSignOut,
}: NotebookLibraryHeaderProps) {
  return (
    <header className="notebooks-shellbar">
      <div className="brand-lockup compact">
        <span>DocMind</span>
      </div>
      <div className="topbar-actions">
        <Link className="button button--secondary" to="/settings"><Settings size={16} /> Settings</Link>
        <Button icon={<LogOut size={16} />} onClick={onSignOut} variant="ghost">Sign out</Button>
      </div>
      <div className="mobile-nav-menu">
        <button
          aria-expanded={isMobileMenuOpen}
          aria-label="Open navigation menu"
          className="icon-button"
          onClick={onMobileMenuToggle}
          type="button"
        >
          <Menu size={18} />
        </button>
        {isMobileMenuOpen ? (
          <div className="mobile-nav-menu__panel">
            <Link onClick={onMobileMenuClose} to="/settings">
              <Settings size={15} /> Settings
            </Link>
            <button onClick={onSignOut} type="button">
              <LogOut size={15} /> Sign out
            </button>
          </div>
        ) : null}
      </div>
    </header>
  );
}
