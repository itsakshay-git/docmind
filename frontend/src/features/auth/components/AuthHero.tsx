import { Github, Library, Linkedin, ShieldCheck, Sparkles } from "lucide-react";

export function AuthHero() {
  return (
    <section className="login-hero">
      <div className="login-hero-glass">
        <div className="brand-lockup">
          <span>DocMind</span>
        </div>
        <h1>Turn scattered sources into grounded study answers.</h1>
        <p>
          Build private research notebooks, connect PDFs, websites, and transcripts, then ask questions backed by your
          own source material.
        </p>
        <div className="value-grid">
          <span>
            <Library size={16} /> Multi-source notebooks
          </span>
          <span>
            <Sparkles size={16} /> Grounded AI answers
          </span>
          <span>
            <ShieldCheck size={16} /> Private authenticated workspace
          </span>
        </div>
        <div className="external-profile-links">
          <a className="github-link" href="https://github.com/itsakshay-git/docmind" rel="noreferrer" target="_blank">
            <Github size={17} />
            View source on GitHub
          </a>
          <a
            className="github-link"
            href="https://www.linkedin.com/in/akshay-d-868a49209/"
            rel="noreferrer"
            target="_blank"
          >
            <Linkedin size={17} />
            Connect on LinkedIn
          </a>
        </div>
      </div>
    </section>
  );
}
