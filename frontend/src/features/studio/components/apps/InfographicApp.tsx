import { X } from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";
import type { StudioArtifact } from "../../../../shared/types";
import { studioApi } from "../../api/studioApi";
import { readJson } from "../../utils/artifactJson";

type InfographicSection = {
  heading: string;
  points: string[];
};

export function InfographicApp({ artifact }: { artifact: StudioArtifact }) {
  const data = useMemo(() => readJson<{ title?: string; sections?: InfographicSection[] }>(artifact), [artifact]);
  const [imageUrl, setImageUrl] = useState("");
  const [imageError, setImageError] = useState("");
  const [isPreviewOpen, setIsPreviewOpen] = useState(false);

  useEffect(() => {
    let currentUrl = "";

    setImageError("");

    if (!artifact.imageAvailable) {
      setImageUrl("");
      return undefined;
    }

    studioApi
      .imageBlobUrl(artifact.id)
      .then((url) => {
        currentUrl = url;
        setImageUrl(url);
      })
      .catch(() => setImageError("The infographic image could not be loaded. The saved outline is still available below."));

    return () => {
      if (currentUrl) {
        URL.revokeObjectURL(currentUrl);
      }
    };
  }, [artifact.id, artifact.imageAvailable]);

  useEffect(() => {
    if (!isPreviewOpen) {
      return undefined;
    }

    function closeOnEscape(event: KeyboardEvent) {
      if (event.key === "Escape") {
        setIsPreviewOpen(false);
      }
    }

    window.addEventListener("keydown", closeOnEscape);

    return () => window.removeEventListener("keydown", closeOnEscape);
  }, [isPreviewOpen]);

  return (
    <article className="studio-mini-app infographic-app">
      <div>
        <h3>{data.title ?? artifact.title}</h3>
        <p>{artifact.imageAvailable ? "Generated visual summary" : "Outline saved. Image was not generated."}</p>
      </div>
      {imageError ? <p className="settings-error">{imageError}</p> : null}
      {imageUrl ? (
        <button className="infographic-frame" onClick={() => setIsPreviewOpen(true)} type="button">
          <img className="infographic-image" src={imageUrl} alt={data.title ?? artifact.title} />
          <span>Click to preview</span>
        </button>
      ) : null}
      {isPreviewOpen && imageUrl ? (
        <div aria-modal="true" className="infographic-modal" onClick={() => setIsPreviewOpen(false)} role="dialog">
          <div className="infographic-modal__content" onClick={(event) => event.stopPropagation()}>
            <div className="infographic-modal__header">
              <strong>{data.title ?? artifact.title}</strong>
              <button
                className="icon-button icon-button--subtle"
                onClick={() => setIsPreviewOpen(false)}
                type="button"
                aria-label="Close infographic preview"
              >
                <X size={16} />
              </button>
            </div>
            <img className="infographic-modal__image" src={imageUrl} alt={data.title ?? artifact.title} />
          </div>
        </div>
      ) : null}
      {!imageUrl ? (
        <div className="studio-preview-body">
          <ReactMarkdown remarkPlugins={[remarkGfm]}>{artifact.markdownContent}</ReactMarkdown>
        </div>
      ) : null}
    </article>
  );
}
