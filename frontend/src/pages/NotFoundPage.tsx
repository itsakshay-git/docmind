import { ArrowLeft, Home } from "lucide-react";
import { Link } from "react-router-dom";
import { useAuth } from "../features/auth/context/AuthContext";

export function NotFoundPage() {
  const auth = useAuth();
  const targetPath = auth.isAuthenticated ? "/notebooks" : "/login";
  const targetLabel = auth.isAuthenticated ? "Back to notebooks" : "Back to login";
  const TargetIcon = auth.isAuthenticated ? Home : ArrowLeft;

  return (
    <main className="not-found-page">
      <section className="not-found-panel">
        <p>404</p>
        <h1>Page not found</h1>
        <span>The page you opened does not exist or has moved.</span>
        <Link className="button button--primary" to={targetPath}>
          <TargetIcon size={18} />
          {targetLabel}
        </Link>
      </section>
    </main>
  );
}
