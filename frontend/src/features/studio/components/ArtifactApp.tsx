import { ArrowLeft, AudioLines, Download, Trash2 } from "lucide-react";
import type { StudioArtifact } from "../../../shared/types";
import { FlashcardsApp } from "./apps/FlashcardsApp";
import { InfographicApp } from "./apps/InfographicApp";
import { MarkdownArtifactApp } from "./apps/MarkdownArtifactApp";
import { PodcastApp } from "./apps/PodcastApp";
import { QuizApp } from "./apps/QuizApp";

type ArtifactAppProps = {
  artifact: StudioArtifact;
  downloadError: string;
  isDeleting: boolean;
  onBack: () => void;
  onDelete: () => void;
  onDownload: (format: "audio" | "png" | "jpg") => void;
};

export function ArtifactApp({
  artifact,
  downloadError,
  isDeleting,
  onBack,
  onDelete,
  onDownload,
}: ArtifactAppProps) {
  const showAudioDownload = artifact.type === "PODCAST_SCRIPT" && artifact.audioAvailable;
  const showImageDownloads = artifact.type === "INFOGRAPHIC_OUTLINE" && artifact.imageAvailable;

  return (
    <>
      <div className="studio-app-header">
        <button className="text-action" onClick={onBack} type="button"><ArrowLeft size={15} /> Studio</button>
        <div className="studio-preview-actions">
          {showAudioDownload ? (
            <button className="icon-button icon-button--subtle" onClick={() => onDownload("audio")} type="button" aria-label="Download Audio">
              <AudioLines size={15} />
            </button>
          ) : null}
          {showImageDownloads ? (
            <>
              <button className="button button--secondary button--compact" onClick={() => onDownload("png")} type="button">
                <Download size={14} /> PNG
              </button>
              <button className="button button--secondary button--compact" onClick={() => onDownload("jpg")} type="button">
                <Download size={14} /> JPG
              </button>
            </>
          ) : null}
          <button className="icon-button icon-button--subtle" disabled={isDeleting} onClick={onDelete} type="button" aria-label="Delete artifact">
            <Trash2 size={15} />
          </button>
        </div>
      </div>

      {downloadError ? <p className="settings-error">{downloadError}</p> : null}

      {artifact.type === "FLASHCARDS" ? <FlashcardsApp artifact={artifact} /> : null}
      {artifact.type === "QUIZ" ? <QuizApp artifact={artifact} /> : null}
      {artifact.type === "PODCAST_SCRIPT" ? <PodcastApp artifact={artifact} /> : null}
      {artifact.type === "BRIEFING" ? <MarkdownArtifactApp artifact={artifact} /> : null}
      {artifact.type === "INFOGRAPHIC_OUTLINE" ? <InfographicApp artifact={artifact} /> : null}
    </>
  );
}
