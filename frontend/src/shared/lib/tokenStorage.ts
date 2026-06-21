const TOKEN_KEY = "docmind.accessToken";

export const AUTH_SESSION_EXPIRED_EVENT = "docmind:auth-session-expired";

export const tokenStorage = {
  get() {
    return localStorage.getItem(TOKEN_KEY) ?? "";
  },

  set(token: string) {
    localStorage.setItem(TOKEN_KEY, token);
  },

  clear() {
    localStorage.removeItem(TOKEN_KEY);
  },

  expireSession() {
    localStorage.removeItem(TOKEN_KEY);
    window.dispatchEvent(new Event(AUTH_SESSION_EXPIRED_EVENT));
  },
};