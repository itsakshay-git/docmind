import { httpClient } from "../../../shared/api/httpClient";
import type { UserProfile } from "../../../shared/types/api";

export const userApi = {
  getProfile() {
    return httpClient<UserProfile>("/api/v1/users/me");
  },

  updateProfile(fullName: string) {
    return httpClient<UserProfile>("/api/v1/users/me", {
      method: "PATCH",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ fullName }),
    });
  },

  updatePassword(currentPassword: string, newPassword: string) {
    return httpClient<void>("/api/v1/users/me/password", {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ currentPassword, newPassword }),
    });
  },
};
