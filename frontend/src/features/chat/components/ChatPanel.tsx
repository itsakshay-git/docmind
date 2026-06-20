import { Bot, Check, Copy, Send, Trash2, User } from "lucide-react";
import { ComponentPropsWithoutRef, useEffect, useRef, useState } from "react";
import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";
import type { ChatMessage } from "../../../shared/types";

type ChatPanelProps = {
  errorMessage?: string;
  isBusy: boolean;
  isClearing: boolean;
  isLoading: boolean;
  messages: ChatMessage[];
  topK: number;
  onClear: () => void;
  onSend: (content: string, topK: number) => void;
  onTopKChange: (topK: number) => void;
};

export function ChatPanel({
  errorMessage,
  isBusy,
  isClearing,
  isLoading,
  messages,
  topK,
  onClear,
  onSend,
  onTopKChange,
}: ChatPanelProps) {
  const [content, setContent] = useState("");
  const threadRef = useRef<HTMLDivElement>(null);
  const hasStreamingMessage = messages.some((message) => message.streaming);

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

    onSend(nextContent, topK);
    setContent("");
  }

  return (
    <section className="chat-panel">
      <div className="panel-heading">
        <span>Chat</span>
        <div className="chat-toolbar">
          <label className="tooltip" data-tooltip="Controls how many source chunks are used for the answer.">
            Context
            <select
              aria-label="Retrieved context count"
              value={topK}
              onChange={(event) => onTopKChange(Number(event.target.value))}
            >
              <option value={3}>Short</option>
              <option value={5}>Balanced</option>
              <option value={8}>Deep</option>
            </select>
          </label>
          <button
            className="icon-button icon-button--subtle tooltip"
            data-tooltip="Delete this notebook's saved chat history."
            disabled={isBusy || isClearing || messages.length === 0}
            onClick={onClear}
            type="button"
            aria-label="Clear chat"
          >
            <Trash2 size={15} />
          </button>
        </div>
      </div>

      <div className="chat-thread" ref={threadRef}>
        {isLoading ? (
          <article className="chat-message chat-message--assistant">
            <div className="chat-avatar">
              <Bot size={17} />
            </div>
            <div className="chat-bubble chat-bubble--loading">Loading conversation...</div>
          </article>
        ) : null}

        {!isLoading && messages.length === 0 ? (
          <article className="chat-message chat-message--assistant">
            <div className="chat-avatar">
              <Bot size={17} />
            </div>
            <div className="chat-bubble">
              Ask a question about this notebook. Answers are saved here so the conversation feels continuous.
            </div>
          </article>
        ) : null}

        {messages.map((message) => (
          <article
            className={`chat-message chat-message--${message.role === "USER" ? "user" : "assistant"}`}
            key={message.id}
          >
            <div className="chat-avatar">{message.role === "USER" ? <User size={17} /> : <Bot size={17} />}</div>
            <div className="chat-bubble">
              {message.role === "ASSISTANT" ? (
                message.content ? (
                  <ReactMarkdown components={{ code: CodeBlock }} remarkPlugins={[remarkGfm]}>
                    {message.content}
                  </ReactMarkdown>
                ) : (
                  <span className="chat-bubble--loading">Thinking...</span>
                )
              ) : (
                message.content
              )}
              {message.sources.length > 0 ? <div className="chat-sources">{message.sources.length} sources</div> : null}
            </div>
          </article>
        ))}

        {errorMessage ? (
          <article className="chat-message chat-message--assistant">
            <div className="chat-avatar">
              <Bot size={17} />
            </div>
            <div className="chat-bubble chat-bubble--error">{errorMessage}</div>
          </article>
        ) : null}

        {isBusy && !hasStreamingMessage ? (
          <article className="chat-message chat-message--assistant">
            <div className="chat-avatar">
              <Bot size={17} />
            </div>
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
        <button
          aria-label="Send message"
          className="icon-button icon-button--primary"
          disabled={isBusy || !cleanContent()}
          onClick={sendMessage}
        >
          <Send size={18} />
        </button>
      </div>
    </section>
  );
}

function CodeBlock({ children, className, ...props }: ComponentPropsWithoutRef<"code">) {
  const [isCopied, setIsCopied] = useState(false);
  const match = /language-(\w+)/.exec(className ?? "");
  const language = match?.[1] ?? "code";
  const code = String(children).replace(/\n$/, "");
  const isInline = !className;

  if (isInline) {
    return (
      <code className={className} {...props}>
        {children}
      </code>
    );
  }

  async function copyCode() {
    await navigator.clipboard.writeText(code);
    setIsCopied(true);
    window.setTimeout(() => setIsCopied(false), 1200);
  }

  return (
    <div className="code-block">
      <div className="code-block__header">
        <span>{language}</span>
        <button onClick={copyCode} type="button">
          {isCopied ? <Check size={14} /> : <Copy size={14} />}
          {isCopied ? "Copied" : "Copy"}
        </button>
      </div>
      <pre>
        <code className={className} {...props}>
          {code}
        </code>
      </pre>
    </div>
  );
}
