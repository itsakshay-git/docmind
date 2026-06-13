import { useMutation } from "@tanstack/react-query";
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { authApi } from "../features/auth/api/authApi";
import { AuthForm } from "../features/auth/components/AuthForm";
import { AuthHero } from "../features/auth/components/AuthHero";
import { useAuth } from "../features/auth/context/AuthContext";
import { demoCredentials } from "../features/auth/model/demoCredentials";
import type { AuthMode } from "../features/auth/model/demoCredentials";

export function LoginPage() {
  const auth = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState(demoCredentials.email);
  const [password, setPassword] = useState(demoCredentials.password);
  const [showPassword, setShowPassword] = useState(false);
  const [mode, setMode] = useState<AuthMode>("login");

  const mutation = useMutation({
    mutationFn: async () => {
      if (mode === "register") {
        await authApi.register(email, password);
      }

      return authApi.login(email, password);
    },
    onSuccess(response) {
      auth.setSession(response.accessToken);
      navigate("/notebooks");
    },
  });

  function useDemoCredentials() {
    setMode("login");
    setEmail(demoCredentials.email);
    setPassword(demoCredentials.password);
  }

  return (
    <main className="login-page">
      <AuthHero />
      <AuthForm
        email={email}
        errorMessage={mutation.error?.message}
        isPending={mutation.isPending}
        mode={mode}
        password={password}
        showPassword={showPassword}
        onEmailChange={setEmail}
        onModeToggle={() => setMode(mode === "login" ? "register" : "login")}
        onPasswordChange={setPassword}
        onSubmit={() => mutation.mutate()}
        onTogglePasswordVisibility={() => setShowPassword((current) => !current)}
        onUseDemo={useDemoCredentials}
      />
    </main>
  );
}
