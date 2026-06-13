import { httpClient } from "../../../shared/api/httpClient";
import { tokenStorage } from "../../../shared/lib/tokenStorage";
import type { StudioArtifact, StudioArtifactType } from "../../../shared/types/api";

const API_BASE_URL = import.meta.env.VITE_DOCMIND_API_URL ?? "http://localhost:8081";

export const studioApi = {
  list(notebookId: string) {
    return httpClient<StudioArtifact[]>(`/api/v1/studio/notebooks/${notebookId}/artifacts`);
  },

  generate(notebookId: string, type: StudioArtifactType, instruction: string) {
    return httpClient<StudioArtifact>(`/api/v1/studio/notebooks/${notebookId}/artifacts`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ type, instruction }),
    });
  },

  delete(artifactId: string) {
    return httpClient<void>(`/api/v1/studio/artifacts/${artifactId}`, {
      method: "DELETE",
    });
  },

  async audioBlobUrl(artifactId: string) {
    const token = tokenStorage.get();
    const response = await fetch(`${API_BASE_URL}/api/v1/studio/artifacts/${artifactId}/audio`, {
      headers: token ? { Authorization: `Bearer ${token}` } : {},
    });

    if (!response.ok) {
      throw new Error(await response.text());
    }

    return URL.createObjectURL(await response.blob());
  },

  async imageBlobUrl(artifactId: string) {
    const token = tokenStorage.get();
    const response = await fetch(`${API_BASE_URL}/api/v1/studio/artifacts/${artifactId}/image`, {
      headers: token ? { Authorization: `Bearer ${token}` } : {},
    });

    if (!response.ok) {
      throw new Error(await response.text());
    }

    return URL.createObjectURL(await response.blob());
  },

  async download(artifactId: string, format: "audio" | "png" | "jpg") {
    const token = tokenStorage.get();
    const response = await fetch(
      `${API_BASE_URL}/api/v1/studio/artifacts/${artifactId}/download?format=${format}`,
      {
        headers: token ? { Authorization: `Bearer ${token}` } : {},
      }
    );

    if (!response.ok) {
      throw new Error(await response.text());
    }

    const blob = await response.blob();
    const contentDisposition = response.headers.get("content-disposition") ?? "";
    const filenameMatch = contentDisposition.match(/filename="?([^"]+)"?/);
    const fallbackExtension = format === "audio" ? "wav" : format;
    const filename = filenameMatch?.[1] ?? `studio-artifact.${fallbackExtension}`;
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");

    link.href = url;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    link.remove();
    URL.revokeObjectURL(url);
  },
};
