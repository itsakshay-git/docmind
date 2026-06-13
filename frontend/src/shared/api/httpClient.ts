import { tokenStorage } from "../lib/tokenStorage";

const API_BASE_URL = import.meta.env.VITE_DOCMIND_API_URL ?? "http://localhost:8081";

export class ApiError extends Error {
  readonly status: number;

  constructor(message: string, status: number) {
    super(message);
    this.name = "ApiError";
    this.status = status;
  }
}

type HttpOptions = {
  body?: BodyInit;
  headers?: Record<string, string>;
  method?: "GET" | "POST" | "PUT" | "PATCH" | "DELETE";
  skipAuth?: boolean;
};

export async function httpClient<T>(path: string, options: HttpOptions = {}): Promise<T> {
  const headers: Record<string, string> = { ...(options.headers ?? {}) };
  const token = tokenStorage.get();

  if (token && !options.skipAuth) {
    headers.Authorization = `Bearer ${token}`;
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    method: options.method ?? "GET",
    headers,
    body: options.body,
  });

  const contentType = response.headers.get("content-type") ?? "";
  const payload = contentType.includes("application/json") ? await response.json() : await response.text();

  if (!response.ok) {
    const message =
      typeof payload === "object" && payload?.message
        ? payload.message
        : String(payload || `Request failed with status ${response.status}`);

    throw new ApiError(message, response.status);
  }

  return payload as T;
}
