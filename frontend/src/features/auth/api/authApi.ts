import { httpClient } from "../../../shared/api/httpClient";
import type { AuthResponse } from "../../../shared/types";

export const authApi = {
  register(email: string, password: string) {
    return httpClient<{ id: string; email: string }>("/api/v1/auth/register", {
      method: "POST",
      skipAuth: true,
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, password }),
    });
  },

  login(email: string, password: string) {
    return httpClient<AuthResponse>("/api/v1/auth/login", {
      method: "POST",
      skipAuth: true,
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, password }),
    });
  },
};
