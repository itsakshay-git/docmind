import { Bot, Send, Sparkles, User } from "lucide-react";
import { useEffect, useRef, useState } from "react";
import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";
import type { ChatMessage } from "../../../shared/types/api";

type ChatPanelProps = {
  errorMessage?: string;
  isBusy: boolean;
  isLoading: boolean;
  messages: ChatMessage[];
  onSend: (content: string) => void;
};

export function ChatPanel({ errorMessage, isBusy, isLoading, messages, onSend }: ChatPanelProps) {
  const [content, setContent] = useState("");
  const threadRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    threadRef.current?.scrollTo({
      top: threadRef.current.scrollHeight,
      behavior: "smooth",
    });
  }, [messages, isBusy]);

  function cleanContent() {
    return content.trim();
  }

  function sendMessage() {
    const nextContent = cleanContent();

    if (!nextContent) {
      return;
    }

    onSend(nextContent);
    setContent("");
  }

  return (
    <section className="chat-panel">
      <div className="panel-heading">
        <span><Sparkles size={17} /> Chat</span>
      </div>

      <div className="chat-thread" ref={threadRef}>
        {isLoading ? (
          <article className="chat-message chat-message--assistant">
            <div className="chat-avatar"><Bot size={17} /></div>
            <div className="chat-bubble chat-bubble--loading">Loading conversation...</div>
          </article>
        ) : null}

        {!isLoading && messages.length === 0 ? (
          <article className="chat-message chat-message--assistant">
            <div className="chat-avatar"><Bot size={17} /></div>
            <div className="chat-bubble">
              Ask a question about this notebook. Answers are saved here so the conversation feels continuous.
            </div>
          </article>
        ) : null}

        {messages.map((message) => (
          <article className={`chat-message chat-message--${message.role === "USER" ? "user" : "assistant"}`} key={message.id}>
            <div className="chat-avatar">
              {message.role === "USER" ? <User size={17} /> : <Bot size={17} />}
            </div>
            <div className="chat-bubble">
              {message.role === "ASSISTANT" ? (
                <ReactMarkdown remarkPlugins={[remarkGfm]}>{message.content}</ReactMarkdown>
              ) : (
                message.content
              )}
              {message.sources.length > 0 ? (
                <div className="chat-sources">{message.sources.length} sources</div>
              ) : null}
            </div>
          </article>
        ))}

        {errorMessage ? (
          <article className="chat-message chat-message--assistant">
            <div className="chat-avatar"><Bot size={17} /></div>
            <div className="chat-bubble chat-bubble--error">{errorMessage}</div>
          </article>
        ) : null}

        {isBusy ? (
          <article className="chat-message chat-message--assistant">
            <div className="chat-avatar"><Bot size={17} /></div>
            <div className="chat-bubble chat-bubble--loading">Thinking...</div>
          </article>
        ) : null}
      </div>

      <div className="chat-composer">
        <textarea
          aria-label="Message"
          placeholder="Ask anything about this notebook"
          rows={1}
          value={content}
          onChange={(event) => setContent(event.target.value)}
          onKeyDown={(event) => {
            if (event.key === "Enter" && !event.shiftKey) {
              event.preventDefault();
              sendMessage();
            }
          }}
        />
        <button aria-label="Send message" className="icon-button icon-button--primary" disabled={isBusy || !cleanContent()} onClick={sendMessage}>
          <Send size={18} />
        </button>
      </div>
    </section>
  );
}
