import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { notebooksApi } from "../api/notebooksApi";
import { documentKeys } from "../../documents/hooks/documentKeys";
import { notebookKeys } from "./notebookKeys";

export function useNotebooks() {
  return useQuery({
    queryKey: notebookKeys.all,
    queryFn: notebooksApi.list,
  });
}

export function useCreateNotebook() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: notebooksApi.create,
    onSuccess() {
      queryClient.invalidateQueries({ queryKey: notebookKeys.all });
    },
  });
}

export function useDeleteNotebook() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: notebooksApi.delete,
    onSuccess() {
      queryClient.invalidateQueries({ queryKey: notebookKeys.all });
      queryClient.invalidateQueries({ queryKey: documentKeys.all });
    },
  });
}

export function useUpdateNotebookTitle() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ notebookId, nextTitle }: { notebookId: string; nextTitle: string }) =>
      notebooksApi.updateTitle(notebookId, nextTitle),
    onSuccess() {
      queryClient.invalidateQueries({ queryKey: notebookKeys.all });
    },
  });
}
