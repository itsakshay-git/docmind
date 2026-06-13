import { ArrowLeft } from "lucide-react";
import { Link } from "react-router-dom";

export function WorkspaceMobileTopbar() {
  return (
    <div className="workspace-mobile-topbar">
      <Link className="back-link" to="/notebooks">
        <ArrowLeft size={16} /> All notebooks
      </Link>
    </div>
  );
}
