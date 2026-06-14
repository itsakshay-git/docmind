import { fireEvent, render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import { NotebookLibraryToolbar } from "./NotebookLibraryToolbar";

function renderToolbar() {
  const props = {
    isCreating: false,
    searchTerm: "",
    sort: "recent" as const,
    view: "grid" as const,
    onCreate: vi.fn(),
    onSearchTermChange: vi.fn(),
    onSortToggle: vi.fn(),
    onViewChange: vi.fn(),
  };

  render(<NotebookLibraryToolbar {...props} />);

  return props;
}

describe("NotebookLibraryToolbar", () => {
  it("calls toolbar handlers for search, sort, view, and create", async () => {
    const user = userEvent.setup();
    const props = renderToolbar();

    fireEvent.change(screen.getByRole("textbox", { name: "Search notebooks" }), { target: { value: "java" } });
    await user.click(screen.getByRole("button", { name: "Most recent" }));
    await user.click(screen.getByRole("button", { name: "Create new" }));

    expect(props.onSearchTermChange).toHaveBeenLastCalledWith("java");
    expect(props.onSortToggle).toHaveBeenCalledTimes(1);
    expect(props.onCreate).toHaveBeenCalledTimes(1);

    const viewButtons = screen.getAllByRole("button").filter((button) => button.closest(".view-toggle"));
    await user.click(viewButtons[1]);

    expect(props.onViewChange).toHaveBeenCalledWith("list");
  });
});
