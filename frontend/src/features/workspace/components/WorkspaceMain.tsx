import { ChatPanel } from "../../chat/components/ChatPanel";
import { StudioPanel } from "../../studio/components/StudioPanel";
import type { ChatMessage } from "../../../shared/types";
import type { WorkspaceMobileSection } from "../model/workspaceSections";

type WorkspaceMainProps = {
  activeMobileSection: WorkspaceMobileSection;
  chatErrorMessage?: string;
  chatTopK: number;
  isBusy: boolean;
  isChatClearing: boolean;
  isChatLoading: boolean;
  messages: ChatMessage[];
  notebookId: string;
  onChatClear: () => void;
  onChatSend: (content: string, topK: number) => void;
  onChatTopKChange: (topK: number) => void;
};

export function WorkspaceMain({
  activeMobileSection,
  chatErrorMessage,
  chatTopK,
  isBusy,
  isChatClearing,
  isChatLoading,
  messages,
  notebookId,
  onChatClear,
  onChatSend,
  onChatTopKChange,
}: WorkspaceMainProps) {
  return (
    <section className="workspace-main">
      <section className="workspace-content-grid">
        <div className={`workspace-mobile-section workspace-mobile-section--chat ${activeMobileSection === "chat" ? "active" : ""}`}>
          <ChatPanel
            errorMessage={chatErrorMessage}
            isBusy={isBusy}
            isClearing={isChatClearing}
            isLoading={isChatLoading}
            messages={messages}
            topK={chatTopK}
            onClear={onChatClear}
            onSend={onChatSend}
            onTopKChange={onChatTopKChange}
          />
        </div>
        <div className={`workspace-mobile-section workspace-mobile-section--studio ${activeMobileSection === "studio" ? "active" : ""}`}>
          <StudioPanel notebookId={notebookId} />
        </div>
      </section>
    </section>
  );
}
