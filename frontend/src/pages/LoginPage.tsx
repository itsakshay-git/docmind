import { useMutation } from "@tanstack/react-query";
import { BrainCircuit, Library, ShieldCheck, Sparkles } from "lucide-react";
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { authApi } from "../features/auth/api/authApi";
import { useAuth } from "../features/auth/context/AuthContext";
import { Button } from "../shared/components/Button";
import { TextField } from "../shared/components/TextField";

export function LoginPage() {
  const auth = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState("test@example.com");
  const [password, setPassword] = useState("Password123!");
  const [mode, setMode] = useState<"login" | "register">("login");

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

  return (
    <main className="login-page">
      <section className="login-hero">
        <div className="brand-lockup">
          <div className="brand-symbol"><BrainCircuit size={28} /></div>
          <span>DocMind</span>
        </div>
        <h1>Your NotebookLM-style research workspace for private PDFs.</h1>
        <p>
          Upload sources, retrieve relevant passages, ask grounded questions, and prepare study artifacts from one clean workspace.
        </p>
        <div className="value-grid">
          <span><Library size={16} /> Source notebooks</span>
          <span><Sparkles size={16} /> Gemini answers</span>
          <span><ShieldCheck size={16} /> JWT protected API</span>
        </div>
      </section>

      <section className="login-card">
        <div>
          <p className="eyebrow">Welcome</p>
          <h2>{mode === "login" ? "Sign in to DocMind" : "Create your workspace"}</h2>
        </div>
        <TextField label="Email" onChange={(event) => setEmail(event.target.value)} type="email" value={email} />
        <TextField label="Password" onChange={(event) => setPassword(event.target.value)} type="password" value={password} />
        {mutation.isError && <div className="form-error">{mutation.error.message}</div>}
        <Button disabled={mutation.isPending} onClick={() => mutation.mutate()}>
          {mutation.isPending ? "Working" : mode === "login" ? "Login" : "Register"}
        </Button>
        <button className="text-action" onClick={() => setMode(mode === "login" ? "register" : "login")} type="button">
          {mode === "login" ? "Need an account? Register" : "Already registered? Login"}
        </button>
      </section>
    </main>
  );
}
