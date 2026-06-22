import { FileText, FileUp, Globe2, Link, Upload, Youtube } from "lucide-react";
import { useState } from "react";
import { Button } from "../../../shared/components/Button";

type SourceMode = "PDF" | "WEB_URL" | "YOUTUBE";

type SourceUploadPanelProps = {
  isUploading: boolean;
  onAddWebUrl: (url: string) => void;
  onAddYouTubeUrl: (url: string) => void;
  onAddYouTubeTranscript: (url: string, title: string, transcript: string) => void;
  onUpload: (file: File) => void;
};

const sourceModes = [
  { id: "PDF", label: "PDF", Icon: FileText },
  { id: "WEB_URL", label: "Web", Icon: Globe2 },
  { id: "YOUTUBE", label: "Video", Icon: Youtube },
] as const;

export function SourceUploadPanel({
  isUploading,
  onAddWebUrl,
  onAddYouTubeTranscript,
  onAddYouTubeUrl,
  onUpload,
}: SourceUploadPanelProps) {
  const [file, setFile] = useState<File | null>(null);
  const [mode, setMode] = useState<SourceMode>("PDF");
  const [url, setUrl] = useState("");
  const [youtubeMode, setYouTubeMode] = useState<"MANUAL" | "AUTO">("MANUAL");
  const [youtubeTitle, setYouTubeTitle] = useState("");
  const [transcript, setTranscript] = useState("");

  function submit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (mode === "PDF" && file) {
      onUpload(file);
      setFile(null);
    }

    if (mode === "WEB_URL" && url.trim()) {
      onAddWebUrl(url.trim());
      setUrl("");
    }

    if (mode === "YOUTUBE" && youtubeMode === "AUTO" && url.trim()) {
      onAddYouTubeUrl(url.trim());
      setUrl("");
    }

    if (mode === "YOUTUBE" && youtubeMode === "MANUAL" && transcript.trim()) {
      onAddYouTubeTranscript(url.trim(), youtubeTitle.trim(), transcript.trim());
      setUrl("");
      setYouTubeTitle("");
      setTranscript("");
    }
  }

  return (
    <section className="source-upload-panel">
      <div className="panel-heading source-panel-heading">
        <span>
          <FileUp size={17} /> Add source
        </span>
        <small>{modeLabel(mode)}</small>
      </div>
      <div className="source-mode-tabs" role="tablist" aria-label="Source type">
        {sourceModes.map(({ id, label, Icon }) => (
          <button
            aria-selected={mode === id}
            className={mode === id ? "active" : ""}
            key={id}
            onClick={() => setMode(id)}
            role="tab"
            type="button"
          >
            <Icon size={14} /> {label}
          </button>
        ))}
      </div>
      <form className="source-dropzone" onSubmit={submit}>
        {mode === "PDF" ? (
          <>
            <div className="source-dropzone__intro">
              <span>
                <Upload size={18} />
              </span>
              <div>
                <strong>{file ? file.name : "Upload a PDF"}</strong>
                <p>{file ? "Ready to index this file." : "Best for notes, slides, and reference PDFs."}</p>
              </div>
            </div>
            <label className="source-file-picker">
              <input
                type="file"
                accept="application/pdf"
                onChange={(event) => setFile(event.target.files?.[0] ?? null)}
              />
              <span>{file ? "Change file" : "Choose PDF"}</span>
            </label>
            <Button disabled={!file || isUploading} icon={<Upload size={16} />} type="submit">
              {isUploading ? "Indexing" : "Add PDF"}
            </Button>
          </>
        ) : null}

        {mode === "WEB_URL" ? (
          <>
            <div className="source-dropzone__intro">
              <span>
                <Link size={18} />
              </span>
              <div>
                <strong>Add a website</strong>
                <p>Imports readable article or documentation text.</p>
              </div>
            </div>
            <input
              placeholder="https://example.com/article"
              value={url}
              onChange={(event) => setUrl(event.target.value)}
            />
            <Button disabled={!url.trim() || isUploading} icon={<Link size={16} />} type="submit">
              {isUploading ? "Indexing" : "Add website"}
            </Button>
          </>
        ) : null}

        {mode === "YOUTUBE" ? (
          <>
            <div className="source-dropzone__intro">
              <span>
                <Youtube size={18} />
              </span>
              <div>
                <strong>Add a transcript</strong>
                <p>Paste transcript text for the most reliable demo path.</p>
              </div>
            </div>
            <div className="source-mode-tabs source-mode-tabs--compact" role="tablist" aria-label="Transcript mode">
              <button
                aria-selected={youtubeMode === "MANUAL"}
                className={youtubeMode === "MANUAL" ? "active" : ""}
                onClick={() => setYouTubeMode("MANUAL")}
                role="tab"
                type="button"
              >
                Paste
              </button>
              <button
                aria-selected={youtubeMode === "AUTO"}
                className={youtubeMode === "AUTO" ? "active" : ""}
                onClick={() => setYouTubeMode("AUTO")}
                role="tab"
                type="button"
              >
                Auto
              </button>
            </div>
            <input
              placeholder="https://www.youtube.com/watch?v=..."
              value={url}
              onChange={(event) => setUrl(event.target.value)}
            />
            {youtubeMode === "MANUAL" ? (
              <>
                <input
                  placeholder="Optional title"
                  value={youtubeTitle}
                  onChange={(event) => setYouTubeTitle(event.target.value)}
                />
                <textarea
                  placeholder="Paste transcript text here"
                  rows={7}
                  value={transcript}
                  onChange={(event) => setTranscript(event.target.value)}
                />
                <Button disabled={!transcript.trim() || isUploading} icon={<Youtube size={16} />} type="submit">
                  {isUploading ? "Indexing" : "Add transcript"}
                </Button>
              </>
            ) : (
              <Button disabled={!url.trim() || isUploading} icon={<Youtube size={16} />} type="submit">
                {isUploading ? "Indexing" : "Try auto transcript"}
              </Button>
            )}
          </>
        ) : null}
      </form>
    </section>
  );
}

function modeLabel(mode: SourceMode) {
  if (mode === "WEB_URL") {
    return "URL";
  }

  if (mode === "YOUTUBE") {
    return "Transcript";
  }

  return "File";
}
