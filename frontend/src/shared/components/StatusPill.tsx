type StatusPillProps = {
  tone?: "neutral" | "success" | "warning" | "danger";
  children: string;
};

export function StatusPill({ children, tone = "neutral" }: StatusPillProps) {
  return <span className={`status-pill status-pill--${tone}`}>{children}</span>;
}
