import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { studioApi } from "../api/studioApi";
import type { StudioArtifactType } from "../../../shared/types/api";
import { studioKeys } from "./studioKeys";

export function useStudioArtifacts(notebookId: string) {
  const queryClient = useQueryClient();
  const artifactsQuery = useQuery({
    queryKey: studioKeys.artifacts(notebookId),
    queryFn: () => studioApi.list(notebookId),
    enabled: Boolean(notebookId),
  });

  const generateArtifact = useMutation({
    mutationFn: ({ type, instruction }: { type: StudioArtifactType; instruction: string }) =>
      studioApi.generate(notebookId, type, instruction),
    onSuccess() {
      queryClient.invalidateQueries({ queryKey: studioKeys.artifacts(notebookId) });
    },
  });

  const deleteArtifact = useMutation({
    mutationFn: studioApi.delete,
    onSuccess() {
      queryClient.invalidateQueries({ queryKey: studioKeys.artifacts(notebookId) });
    },
  });

  return {
    artifacts: artifactsQuery.data ?? [],
    artifactsQuery,
    deleteArtifact,
    generateArtifact,
  };
}
