import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";
import type { StudioArtifact } from "../../../../shared/types";

export function MarkdownArtifactApp({ artifact }: { artifact: StudioArtifact }) {
  return (
    <article className="studio-mini-app">
      <div>
        <h3>{artifact.title}</h3>
        <p>{artifact.sourceChunkIds.length} source chunks</p>
      </div>
      <div className="studio-preview-body">
        <ReactMarkdown remarkPlugins={[remarkGfm]}>{artifact.markdownContent}</ReactMarkdown>
      </div>
    </article>
  );
}
