import { useMutation } from "@tanstack/react-query";
import { Eye, EyeOff, Github, Library, Linkedin, ShieldCheck, Sparkles } from "lucide-react";
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { authApi } from "../features/auth/api/authApi";
import { useAuth } from "../features/auth/context/AuthContext";
import { Button } from "../shared/components/Button";
import { TextField } from "../shared/components/TextField";

const demoCredentials = {
  email: "recruiter@docmind.dev",
  password: "Demo@12345",
};

export function LoginPage() {
  const auth = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState(demoCredentials.email);
  const [password, setPassword] = useState(demoCredentials.password);
  const [showPassword, setShowPassword] = useState(false);
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
        <div className="login-hero-glass">
          <div className="brand-lockup">
            <span>DocMind</span>
          </div>
          <h1>Turn scattered sources into grounded study answers.</h1>
          <p>
            Build private research notebooks, connect PDFs, websites, and transcripts, then ask questions backed by your own source material.
          </p>
          <div className="value-grid">
            <span><Library size={16} /> Multi-source notebooks</span>
            <span><Sparkles size={16} /> Grounded AI answers</span>
            <span><ShieldCheck size={16} /> Private authenticated workspace</span>
          </div>
          <div className="external-profile-links">
            <a className="github-link" href="https://github.com/itsakshay-git/docmind" rel="noreferrer" target="_blank">
              <Github size={17} />
              View source on GitHub
            </a>
            <a className="github-link" href="https://www.linkedin.com/in/akshay-d-868a49209/" rel="noreferrer" target="_blank">
              <Linkedin size={17} />
              Connect on LinkedIn
            </a>
          </div>
        </div>
      </section>

      <section className="login-card">
        <div>
          <p className="eyebrow">Welcome</p>
          <h2>{mode === "login" ? "Sign in to DocMind" : "Create your workspace"}</h2>
        </div>
        <TextField label="Email" onChange={(event) => setEmail(event.target.value)} type="email" value={email} />
        <label className="field">
          <span>Password</span>
          <div className="password-field">
            <input
              onChange={(event) => setPassword(event.target.value)}
              type={showPassword ? "text" : "password"}
              value={password}
            />
            <button
              aria-label={showPassword ? "Hide password" : "Show password"}
              onClick={() => setShowPassword((current) => !current)}
              type="button"
            >
              {showPassword ? <EyeOff size={17} /> : <Eye size={17} />}
            </button>
          </div>
        </label>
        <div className="demo-login-card">
          <div>
            <strong>Recruiter demo</strong>
            <span>{demoCredentials.email} / {demoCredentials.password}</span>
          </div>
          <button
            className="text-action"
            onClick={() => {
              setMode("login");
              setEmail(demoCredentials.email);
              setPassword(demoCredentials.password);
            }}
            type="button"
          >
            Use
          </button>
        </div>
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
