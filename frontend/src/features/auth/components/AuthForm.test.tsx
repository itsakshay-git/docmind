import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import { AuthForm } from "./AuthForm";

function renderAuthForm(overrides: Partial<Parameters<typeof AuthForm>[0]> = {}) {
  const props = {
    email: "recruiter@docmind.dev",
    isPending: false,
    mode: "login" as const,
    password: "Demo@12345",
    showPassword: false,
    onEmailChange: vi.fn(),
    onModeToggle: vi.fn(),
    onPasswordChange: vi.fn(),
    onSubmit: vi.fn(),
    onTogglePasswordVisibility: vi.fn(),
    onUseDemo: vi.fn(),
    ...overrides,
  };

  render(<AuthForm {...props} />);

  return props;
}

describe("AuthForm", () => {
  it("renders login copy and submits through the provided handler", async () => {
    const user = userEvent.setup();
    const props = renderAuthForm();

    expect(screen.getByRole("heading", { name: "Sign in to DocMind" })).toBeInTheDocument();
    await user.click(screen.getByRole("button", { name: "Login" }));

    expect(props.onSubmit).toHaveBeenCalledTimes(1);
  });

  it("renders register mode and calls demo credential handler", async () => {
    const user = userEvent.setup();
    const props = renderAuthForm({ mode: "register" });

    expect(screen.getByRole("heading", { name: "Create your workspace" })).toBeInTheDocument();
    await user.click(screen.getByRole("button", { name: "Use" }));

    expect(props.onUseDemo).toHaveBeenCalledTimes(1);
  });
});
