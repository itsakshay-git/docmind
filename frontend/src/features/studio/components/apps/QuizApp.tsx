import { useEffect, useMemo, useState } from "react";
import type { StudioArtifact } from "../../../../shared/types/api";
import { readJson } from "../../utils/artifactJson";
import { MarkdownArtifactApp } from "./MarkdownArtifactApp";

type QuizQuestion = {
  question: string;
  options: string[];
  answer: string;
  explanation: string;
};

export function QuizApp({ artifact }: { artifact: StudioArtifact }) {
  const questions = useMemo(() => readJson<{ questions?: QuizQuestion[] }>(artifact).questions ?? [], [artifact]);
  const [index, setIndex] = useState(0);
  const [answers, setAnswers] = useState<Record<number, string>>({});
  const [isFinished, setIsFinished] = useState(false);
  const question = questions[index];
  const score = questions.filter((item, questionIndex) => answers[questionIndex] === item.answer).length;

  useEffect(() => {
    setIndex(0);
    setAnswers({});
    setIsFinished(false);
  }, [artifact.id]);

  if (!question) {
    return <MarkdownArtifactApp artifact={artifact} />;
  }

  return (
    <article className="studio-mini-app">
      <div>
        <h3>{artifact.title}</h3>
        <p>{isFinished ? `Score ${score} / ${questions.length}` : `Question ${index + 1} / ${questions.length}`}</p>
      </div>
      <div className="quiz-card">
        <strong>{question.question}</strong>
        <div className="quiz-options">
          {question.options.map((option) => {
            const selected = answers[index] === option;
            const correct = isFinished && option === question.answer;
            const wrong = isFinished && selected && option !== question.answer;

            return (
              <button
                className={`${selected ? "selected" : ""} ${correct ? "correct" : ""} ${wrong ? "wrong" : ""}`}
                disabled={isFinished}
                key={option}
                onClick={() => setAnswers((current) => ({ ...current, [index]: option }))}
                type="button"
              >
                {option}
              </button>
            );
          })}
        </div>
        {isFinished ? <p>{question.explanation}</p> : null}
      </div>
      <div className="studio-app-actions">
        <button className="button button--secondary" disabled={index === 0} onClick={() => setIndex(index - 1)} type="button">Prev</button>
        <button className="button button--secondary" disabled={index === questions.length - 1} onClick={() => setIndex(index + 1)} type="button">Next</button>
        <button className="button button--primary" disabled={Object.keys(answers).length < questions.length} onClick={() => setIsFinished(true)} type="button">Finish</button>
      </div>
    </article>
  );
}
