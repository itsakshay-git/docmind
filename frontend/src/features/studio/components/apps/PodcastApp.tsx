import { useEffect, useMemo, useState } from "react";
import type { StudioArtifact } from "../../../../shared/types";
import { studioApi } from "../../api/studioApi";
import { readJson } from "../../utils/artifactJson";

type PodcastSegment = {
  speaker: string;
  text: string;
};

export function PodcastApp({ artifact }: { artifact: StudioArtifact }) {
  const segments = useMemo(() => readJson<{ segments?: PodcastSegment[] }>(artifact).segments ?? [], [artifact]);
  const [audioUrl, setAudioUrl] = useState("");
  const [audioError, setAudioError] = useState("");

  useEffect(() => {
    let currentUrl = "";

    if (!artifact.audioAvailable) {
      setAudioUrl("");
      return undefined;
    }

    studioApi
      .audioBlobUrl(artifact.id)
      .then((url) => {
        currentUrl = url;
        setAudioUrl(url);
      })
      .catch((error) => setAudioError(error instanceof Error ? error.message : "Audio failed to load"));

    return () => {
      if (currentUrl) {
        URL.revokeObjectURL(currentUrl);
      }
    };
  }, [artifact.id, artifact.audioAvailable]);

  return (
    <article className="studio-mini-app">
      <div>
        <h3>{artifact.title}</h3>
        <p>{artifact.audioAvailable ? "Playable podcast audio" : "Script saved. Audio was not generated."}</p>
      </div>
      {audioError ? <p className="settings-error">{audioError}</p> : null}
      {audioUrl ? <audio className="podcast-player" controls src={audioUrl} /> : null}
      <div className="podcast-script">
        {segments.map((segment, index) => (
          <p key={`${segment.speaker}-${index}`}>
            <strong>{segment.speaker}:</strong> {segment.text}
          </p>
        ))}
      </div>
    </article>
  );
}
