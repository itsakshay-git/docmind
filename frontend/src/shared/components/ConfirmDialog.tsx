import { TriangleAlert } from "lucide-react";
import { useEffect } from "react";

type ConfirmDialogProps = {
  cancelLabel?: string;
  confirmLabel?: string;
  description: string;
  isOpen: boolean;
  isPending?: boolean;
  title: string;
  onCancel: () => void;
  onConfirm: () => void;
};

export function ConfirmDialog({
  cancelLabel = "Cancel",
  confirmLabel = "Delete",
  description,
  isOpen,
  isPending = false,
  title,
  onCancel,
  onConfirm,
}: ConfirmDialogProps) {
  useEffect(() => {
    if (!isOpen) {
      return;
    }

    function handleKeyDown(event: KeyboardEvent) {
      if (event.key === "Escape") {
        onCancel();
      }
    }

    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [isOpen, onCancel]);

  if (!isOpen) {
    return null;
  }

  return (
    <div aria-modal="true" className="confirm-dialog-backdrop" onClick={onCancel} role="dialog">
      <section className="confirm-dialog" onClick={(event) => event.stopPropagation()}>
        <span className="confirm-dialog__icon" aria-hidden="true">
          <TriangleAlert size={20} />
        </span>
        <div className="confirm-dialog__body">
          <h2>{title}</h2>
          <p>{description}</p>
        </div>
        <div className="confirm-dialog__actions">
          <button className="button button--ghost" disabled={isPending} onClick={onCancel} type="button">
            {cancelLabel}
          </button>
          <button className="button button--danger" disabled={isPending} onClick={onConfirm} type="button">
            {isPending ? "Deleting..." : confirmLabel}
          </button>
        </div>
      </section>
    </div>
  );
}
