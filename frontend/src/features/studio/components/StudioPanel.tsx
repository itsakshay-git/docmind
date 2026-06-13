import { ArrowLeft, ArrowRight } from "lucide-react";
import { useState } from "react";
import { studioApi } from "../api/studioApi";
import { useStudioArtifacts } from "../hooks/useStudioArtifacts";
import { artifactTypes } from "../model/artifactTypes";
import type { StudioArtifact, StudioArtifactType } from "../../../shared/types";
import { ArtifactApp } from "./ArtifactApp";

type StudioPanelProps = {
  notebookId: string;
};

export function StudioPanel({ notebookId }: StudioPanelProps) {
  const [activeGenerateType, setActiveGenerateType] = useState<StudioArtifactType | null>(null);
  const [instruction, setInstruction] = useState("");
  const [openedArtifactId, setOpenedArtifactId] = useState("");
  const [downloadError, setDownloadError] = useState("");
  const {
    artifacts,
    artifactsQuery,
    deleteArtifact: deleteMutation,
    generateArtifact: generateMutation,
  } = useStudioArtifacts(notebookId);

  const openedArtifact = artifacts.find((artifact) => artifact.id === openedArtifactId);
  const activeArtifactType = artifactTypes.find((artifactType) => artifactType.type === activeGenerateType);

  async function download(artifact: StudioArtifact, format: "audio" | "png" | "jpg") {
    setDownloadError("");

    try {
      await studioApi.download(artifact.id, format);
    } catch (error) {
      setDownloadError(error instanceof Error ? error.message : "Download failed");
    }
  }

  function deleteArtifact(artifact: StudioArtifact) {
    if (window.confirm(`Delete "${artifact.title}"?`)) {
      deleteMutation.mutate(artifact.id, {
        onSuccess() {
          setOpenedArtifactId("");
        },
      });
    }
  }

  if (openedArtifact) {
    return (
      <section className="studio-panel">
        <ArtifactApp
          artifact={openedArtifact}
          downloadError={downloadError}
          isDeleting={deleteMutation.isPending}
          onBack={() => setOpenedArtifactId("")}
          onDelete={() => deleteArtifact(openedArtifact)}
          onDownload={(format) => download(openedArtifact, format)}
        />
      </section>
    );
  }

  if (activeArtifactType) {
    const Icon = activeArtifactType.icon;

    return (
      <section className="studio-panel">
        <div className="studio-app-header">
          <button
            className="text-action"
            onClick={() => {
              setInstruction("");
              setActiveGenerateType(null);
            }}
            type="button"
          >
            <ArrowLeft size={15} /> Studio
          </button>
        </div>

        <div className="studio-generate-detail">
          <div className="studio-generate-detail__title">
            <span>
              <Icon size={18} />
            </span>
            <div>
              <h3>{activeArtifactType.title}</h3>
              <p>{activeArtifactType.description}</p>
            </div>
          </div>

          <textarea
            aria-label={`${activeArtifactType.title} instruction`}
            placeholder="Optional instruction, e.g. focus on primitive data types"
            rows={4}
            value={instruction}
            onChange={(event) => setInstruction(event.target.value)}
          />

          {generateMutation.error ? <p className="settings-error">{generateMutation.error.message}</p> : null}

          <button
            className="button button--primary"
            disabled={generateMutation.isPending}
            onClick={() => {
              generateMutation.mutate(
                { type: activeArtifactType.type, instruction },
                {
                  onSuccess(artifact) {
                    setInstruction("");
                    setActiveGenerateType(null);
                    setOpenedArtifactId(artifact.id);
                  },
                }
              );
            }}
            type="button"
          >
            {generateMutation.isPending ? "Generating..." : `Generate ${activeArtifactType.title}`}
          </button>
        </div>
      </section>
    );
  }

  return (
    <section className="studio-panel">
      <div className="panel-heading">
        <span>Studio</span>
      </div>

      <div className="studio-generator">
        {artifactTypes.map((artifactType) => {
          const Icon = artifactType.icon;
          return (
            <button
              className="studio-entry-card"
              key={artifactType.type}
              onClick={() => setActiveGenerateType(artifactType.type)}
              type="button"
            >
              <Icon size={18} />
              <span>
                <strong>{artifactType.title}</strong>
                <small>{artifactType.description}</small>
              </span>
              <span className="studio-entry-card__action" aria-hidden="true">
                <ArrowRight size={15} />
              </span>
            </button>
          );
        })}
      </div>

      <div className="studio-artifact-section">
        <h3>Generated artifacts</h3>
        <div className="studio-artifact-list">
          {artifactsQuery.isLoading ? <p>Loading artifacts...</p> : null}
          {!artifactsQuery.isLoading && artifacts.length === 0 ? <p>No artifacts yet.</p> : null}
          {artifacts.map((artifact) => (
            <button key={artifact.id} onClick={() => setOpenedArtifactId(artifact.id)} type="button">
              <strong>{artifact.title}</strong>
              <span>{artifact.type.replaceAll("_", " ").toLowerCase()}</span>
            </button>
          ))}
        </div>
      </div>
    </section>
  );
}
