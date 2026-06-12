import { FileUp, Upload } from "lucide-react";
import { useState } from "react";
import { Button } from "../../../shared/components/Button";

type SourceUploadPanelProps = {
  isUploading: boolean;
  onUpload: (file: File) => void;
};

export function SourceUploadPanel({ isUploading, onUpload }: SourceUploadPanelProps) {
  const [file, setFile] = useState<File | null>(null);

  function submit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (file) {
      onUpload(file);
      setFile(null);
    }
  }

  return (
    <section className="source-upload-panel">
      <div className="panel-heading">
        <span><FileUp size={17} /> Sources</span>
      </div>
      <form className="source-dropzone" onSubmit={submit}>
        <Upload size={22} />
        <strong>{file ? file.name : "Upload a PDF source"}</strong>
        <p>DocMind extracts, chunks, embeds, and connects the source to chat.</p>
        <input type="file" accept="application/pdf" onChange={(event) => setFile(event.target.files?.[0] ?? null)} />
        <Button disabled={!file || isUploading} icon={<Upload size={16} />} type="submit">
          {isUploading ? "Processing" : "Add source"}
        </Button>
      </form>
    </section>
  );
}
