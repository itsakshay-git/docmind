import type { StudioArtifact } from "../../../shared/types/api";

export function readJson<T>(artifact: StudioArtifact): T {
  try {
    return JSON.parse(artifact.jsonContent) as T;
  } catch {
    return {} as T;
  }
}

export function unique(values: string[]) {
  return [...new Set(values)];
}
