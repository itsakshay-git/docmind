import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { userApi } from "../api/userApi";
import { userKeys } from "./userKeys";

export function useUserProfile() {
  return useQuery({
    queryKey: userKeys.profile,
    queryFn: userApi.getProfile,
  });
}

export function useUpdateUserProfile() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: userApi.updateProfile,
    onSuccess(profile) {
      queryClient.setQueryData(userKeys.profile, profile);
    },
  });
}

export function useUpdateUserPassword(currentPassword: string, newPassword: string, onSuccess: () => void) {
  return useMutation({
    mutationFn: () => userApi.updatePassword(currentPassword, newPassword),
    onSuccess,
  });
}

export function useDeleteUserAccount(onSuccess: () => void) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: userApi.deleteAccount,
    onSuccess() {
      queryClient.clear();
      onSuccess();
    },
  });
}
