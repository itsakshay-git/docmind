import { createContext, useContext, useMemo, useState, type ReactNode } from "react";
import { tokenStorage } from "../../../shared/lib/tokenStorage";

type AuthContextValue = {
  isAuthenticated: boolean;
  setSession: (token: string) => void;
  signOut: () => void;
};

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState(() => tokenStorage.get());

  const value = useMemo<AuthContextValue>(
    () => ({
      isAuthenticated: Boolean(token),
      setSession(nextToken) {
        tokenStorage.set(nextToken);
        setToken(nextToken);
      },
      signOut() {
        tokenStorage.clear();
        setToken("");
      },
    }),
    [token]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error("useAuth must be used inside AuthProvider");
  }

  return context;
}
