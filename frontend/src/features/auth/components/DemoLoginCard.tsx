import { demoCredentials } from "../model/demoCredentials";

type DemoLoginCardProps = {
  onUseDemo: () => void;
};

export function DemoLoginCard({ onUseDemo }: DemoLoginCardProps) {
  return (
    <div className="demo-login-card">
      <div>
        <strong>Recruiter demo</strong>
        <span>
          {demoCredentials.email} / {demoCredentials.password}
        </span>
      </div>
      <button className="text-action" onClick={onUseDemo} type="button">
        Use
      </button>
    </div>
  );
}
