import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import { WorkspaceMobileTabs } from "./WorkspaceMobileTabs";

describe("WorkspaceMobileTabs", () => {
  it("marks the active section and emits section changes", async () => {
    const user = userEvent.setup();
    const onSectionChange = vi.fn();

    render(<WorkspaceMobileTabs activeSection="chat" onSectionChange={onSectionChange} />);

    expect(screen.getByRole("button", { name: "Chat" })).toHaveAttribute("aria-pressed", "true");

    await user.click(screen.getByRole("button", { name: "Studio" }));

    expect(onSectionChange).toHaveBeenCalledWith("studio");
  });
});
