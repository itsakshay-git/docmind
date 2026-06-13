import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { useEffect } from "react";
import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import { AuthProvider, useAuth } from "../features/auth/context/AuthContext";
import { LoginPage } from "../pages/LoginPage";
import { NotebookWorkspacePage } from "../pages/NotebookWorkspacePage";
import { NotebooksPage } from "../pages/NotebooksPage";
import { SettingsPage } from "../pages/SettingsPage";

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      refetchOnWindowFocus: false,
      retry: 1,
    },
  },
});

function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const auth = useAuth();

  if (!auth.isAuthenticated) {
    return <Navigate replace to="/login" />;
  }

  return children;
}

export function App() {
  useEffect(() => {
    const savedTheme = localStorage.getItem("docmind-theme") === "light" ? "light" : "dark";

    document.documentElement.dataset.theme = savedTheme;
  }, []);

  return (
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <BrowserRouter>
          <Routes>
            <Route element={<LoginPage />} path="/login" />
            <Route element={<Navigate replace to="/notebooks" />} path="/" />
            <Route
              element={
                <ProtectedRoute>
                  <NotebooksPage />
                </ProtectedRoute>
              }
              path="/notebooks"
            />
            <Route
              element={
                <ProtectedRoute>
                  <NotebookWorkspacePage />
                </ProtectedRoute>
              }
              path="/notebooks/:notebookId"
            />
            <Route
              element={
                <ProtectedRoute>
                  <SettingsPage />
                </ProtectedRoute>
              }
              path="/settings"
            />
          </Routes>
        </BrowserRouter>
      </AuthProvider>
    </QueryClientProvider>
  );
}
