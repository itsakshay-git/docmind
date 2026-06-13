export type WorkspaceMobileSection = "sources" | "chat" | "studio";

export const workspaceMobileSections: Array<{
  id: WorkspaceMobileSection;
  label: string;
}> = [
  { id: "sources", label: "Sources" },
  { id: "chat", label: "Chat" },
  { id: "studio", label: "Studio" },
];
