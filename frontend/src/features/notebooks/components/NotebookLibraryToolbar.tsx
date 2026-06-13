import { Check, Grid2X2, List, Plus, Search } from "lucide-react";

export type NotebookView = "grid" | "list";
export type NotebookSort = "recent" | "title";

type NotebookLibraryToolbarProps = {
  isCreating: boolean;
  searchTerm: string;
  sort: NotebookSort;
  view: NotebookView;
  onCreate: () => void;
  onSearchTermChange: (searchTerm: string) => void;
  onSortToggle: () => void;
  onViewChange: (view: NotebookView) => void;
};

export function NotebookLibraryToolbar({
  isCreating,
  searchTerm,
  sort,
  view,
  onCreate,
  onSearchTermChange,
  onSortToggle,
  onViewChange,
}: NotebookLibraryToolbarProps) {
  return (
    <section className="library-toolbar" aria-label="Notebook library controls">
      <button className="library-tab active" type="button">All</button>
      <div className="library-actions">
        <label className="library-search">
          <Search size={17} />
          <input
            aria-label="Search notebooks"
            placeholder="Search"
            value={searchTerm}
            onChange={(event) => onSearchTermChange(event.target.value)}
          />
        </label>
        <div className="view-toggle" aria-label="View options">
          <button className={view === "grid" ? "active" : ""} onClick={() => onViewChange("grid")} type="button"><Grid2X2 size={18} /></button>
          <button className={view === "list" ? "active" : ""} onClick={() => onViewChange("list")} type="button"><List size={18} /></button>
        </div>
        <button className="sort-button" onClick={onSortToggle} type="button">
          {sort === "recent" ? <Check size={16} /> : null}
          {sort === "recent" ? "Most recent" : "Title"}
        </button>
        <button className="button button--primary library-create-button" disabled={isCreating} onClick={onCreate} type="button">
          <Plus size={17} />
          {isCreating ? "Creating..." : "Create new"}
        </button>
      </div>
    </section>
  );
}
