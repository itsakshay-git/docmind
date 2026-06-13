import { Check, X } from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import type { StudioArtifact } from "../../../../shared/types";
import { readJson, unique } from "../../utils/artifactJson";
import { MarkdownArtifactApp } from "./MarkdownArtifactApp";

type Flashcard = {
  front: string;
  back: string;
  difficulty?: string;
};

export function FlashcardsApp({ artifact }: { artifact: StudioArtifact }) {
  const cards = useMemo(() => readJson<{ cards?: Flashcard[] }>(artifact).cards ?? [], [artifact]);
  const [index, setIndex] = useState(0);
  const [isFlipped, setIsFlipped] = useState(false);
  const [known, setKnown] = useState<string[]>([]);
  const [missed, setMissed] = useState<string[]>([]);
  const card = cards[index];

  useEffect(() => {
    setIndex(0);
    setIsFlipped(false);
    setKnown([]);
    setMissed([]);
  }, [artifact.id]);

  function mark(bucket: "known" | "missed") {
    if (!card) {
      return;
    }

    const key = `${index}-${card.front}`;

    setKnown((current) => (bucket === "known" ? unique([...current, key]) : current.filter((item) => item !== key)));
    setMissed((current) => (bucket === "missed" ? unique([...current, key]) : current.filter((item) => item !== key)));
    setIsFlipped(false);
    setIndex((current) => Math.min(current + 1, cards.length - 1));
  }

  if (!card) {
    return <MarkdownArtifactApp artifact={artifact} />;
  }

  const difficulty = (card.difficulty ?? "").toLowerCase();
  const toneClass = ["basic", "medium", "advanced"].includes(difficulty)
    ? `flashcard--${difficulty}`
    : `flashcard--tone-${index % 4}`;

  return (
    <article className="studio-mini-app flashcards-app">
      <div>
        <h3>{artifact.title}</h3>
        <p className="flashcard-progress">
          {index + 1} / {cards.length} cards | {known.length} got it | {missed.length} review again
        </p>
      </div>
      <button
        className={`flashcard ${toneClass} ${isFlipped ? "flipped" : ""}`}
        key={`${artifact.id}-${index}`}
        onClick={() => setIsFlipped((current) => !current)}
        type="button"
      >
        <span className="flashcard__shine" aria-hidden="true" />
        <span className="flashcard__content">{isFlipped ? card.back : card.front}</span>
        <small>{isFlipped ? "Answer" : (card.difficulty ?? "Card")}</small>
      </button>
      <div className="studio-app-actions">
        <button
          className="button button--secondary"
          disabled={index === 0}
          onClick={() => {
            setIndex(index - 1);
            setIsFlipped(false);
          }}
          type="button"
        >
          Prev
        </button>
        <button
          className="button button--secondary"
          disabled={index === cards.length - 1}
          onClick={() => {
            setIndex(index + 1);
            setIsFlipped(false);
          }}
          type="button"
        >
          Next
        </button>
      </div>
      <div className="studio-app-actions">
        <button className="button button--secondary" onClick={() => mark("missed")} type="button">
          <X size={15} /> Did not get it
        </button>
        <button className="button button--primary" onClick={() => mark("known")} type="button">
          <Check size={15} /> Got it
        </button>
      </div>
    </article>
  );
}
