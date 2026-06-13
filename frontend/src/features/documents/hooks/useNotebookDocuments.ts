import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { documentsApi } from "../api/documentsApi";
import { documentKeys } from "./documentKeys";

export function useNotebookDocuments(notebookId: string) {
  return useQuery({
    queryKey: documentKeys.byNotebook(notebookId),
    queryFn: () => documentsApi.listByNotebook(notebookId),
    enabled: Boolean(notebookId),
  });
}

export function useSourceMutations(notebookId: string) {
  const queryClient = useQueryClient();

  function invalidateSources() {
    queryClient.invalidateQueries({ queryKey: documentKeys.all });
    queryClient.invalidateQueries({ queryKey: documentKeys.byNotebook(notebookId) });
    queryClient.invalidateQueries({ queryKey: documentKeys.embeddingCount });
  }

  const uploadPdf = useMutation({
    mutationFn: (file: File) => documentsApi.uploadPdf(notebookId, file),
    onSuccess: invalidateSources,
  });

  const addWebUrl = useMutation({
    mutationFn: (url: string) => documentsApi.addWebUrl(notebookId, url),
    onSuccess: invalidateSources,
  });

  const addYouTubeUrl = useMutation({
    mutationFn: (url: string) => documentsApi.addYouTubeUrl(notebookId, url),
    onSuccess: invalidateSources,
  });

  const addYouTubeTranscript = useMutation({
    mutationFn: ({ url, title, transcript }: { url: string; title: string; transcript: string }) =>
      documentsApi.addYouTubeTranscript(notebookId, url, title, transcript),
    onSuccess: invalidateSources,
  });

  const deleteDocument = useMutation({
    mutationFn: documentsApi.delete,
    onSuccess: invalidateSources,
  });

  return {
    addWebUrl,
    addYouTubeTranscript,
    addYouTubeUrl,
    deleteDocument,
    uploadPdf,
  };
}
