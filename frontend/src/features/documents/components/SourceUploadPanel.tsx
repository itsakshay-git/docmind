import { FileUp, Link, Upload, Youtube } from "lucide-react";
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

export function SourceUploadPanel({ isUploading, onAddWebUrl, onAddYouTubeTranscript, onAddYouTubeUrl, onUpload }: SourceUploadPanelProps) {
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
      <div className="panel-heading">
        <span><FileUp size={17} /> Sources</span>
      </div>
      <div className="source-mode-tabs">
        <button className={mode === "PDF" ? "active" : ""} onClick={() => setMode("PDF")} type="button">PDF</button>
        <button className={mode === "WEB_URL" ? "active" : ""} onClick={() => setMode("WEB_URL")} type="button">Website</button>
        <button className={mode === "YOUTUBE" ? "active" : ""} onClick={() => setMode("YOUTUBE")} type="button">YouTube</button>
      </div>
      <form className="source-dropzone" onSubmit={submit}>
        {mode === "PDF" ? (
          <>
            <Upload size={22} />
            <strong>{file ? file.name : "Upload a PDF source"}</strong>
            <p>Extract, chunk, embed, and connect the PDF to chat.</p>
            <input type="file" accept="application/pdf" onChange={(event) => setFile(event.target.files?.[0] ?? null)} />
            <Button disabled={!file || isUploading} icon={<Upload size={16} />} type="submit">
              {isUploading ? "Processing" : "Add PDF"}
            </Button>
          </>
        ) : null}

        {mode === "WEB_URL" ? (
          <>
            <Link size={22} />
            <strong>Add website</strong>
            <p>Import readable page text from a URL.</p>
            <input placeholder="https://example.com/article" value={url} onChange={(event) => setUrl(event.target.value)} />
            <Button disabled={!url.trim() || isUploading} icon={<Link size={16} />} type="submit">
              {isUploading ? "Processing" : "Add website"}
            </Button>
          </>
        ) : null}

        {mode === "YOUTUBE" ? (
          <>
            <Youtube size={22} />
            <strong>Add YouTube transcript</strong>
            <p>Paste a transcript for reliable ingestion, or try auto-fetch when captions are public.</p>
            <div className="source-mode-tabs source-mode-tabs--compact">
              <button className={youtubeMode === "MANUAL" ? "active" : ""} onClick={() => setYouTubeMode("MANUAL")} type="button">Paste</button>
              <button className={youtubeMode === "AUTO" ? "active" : ""} onClick={() => setYouTubeMode("AUTO")} type="button">Auto</button>
            </div>
            <input placeholder="https://www.youtube.com/watch?v=..." value={url} onChange={(event) => setUrl(event.target.value)} />
            {youtubeMode === "MANUAL" ? (
              <>
                <input placeholder="Optional title" value={youtubeTitle} onChange={(event) => setYouTubeTitle(event.target.value)} />
                <textarea
                  placeholder="Paste transcript text here"
                  rows={7}
                  value={transcript}
                  onChange={(event) => setTranscript(event.target.value)}
                />
                <Button disabled={!transcript.trim() || isUploading} icon={<Youtube size={16} />} type="submit">
                  {isUploading ? "Processing" : "Add transcript"}
                </Button>
              </>
            ) : (
              <Button disabled={!url.trim() || isUploading} icon={<Youtube size={16} />} type="submit">
                {isUploading ? "Processing" : "Try auto transcript"}
              </Button>
            )}
          </>
        ) : null}
      </form>
    </section>
  );
}
