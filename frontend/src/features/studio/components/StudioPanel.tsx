import { AudioLines, Brain, FileChartColumn, Layers3, WandSparkles } from "lucide-react";

const studioActions = [
  {
    icon: AudioLines,
    title: "Podcast brief",
    description: "Generate an audio-style script from notebook sources.",
    state: "Planned",
  },
  {
    icon: Brain,
    title: "Flashcards",
    description: "Turn source chunks into study prompts and answers.",
    state: "Planned",
  },
  {
    icon: FileChartColumn,
    title: "Infographic",
    description: "Extract concepts, relationships, and visual summary blocks.",
    state: "Planned",
  },
  {
    icon: Layers3,
    title: "Quiz",
    description: "Create knowledge checks from grounded context.",
    state: "Planned",
  },
];

export function StudioPanel() {
  return (
    <section className="studio-panel">
      <div className="panel-heading">
        <span><WandSparkles size={17} /> Studio</span>
      </div>
      <div className="studio-grid">
        {studioActions.map((action) => {
          const Icon = action.icon;
          return (
            <article className="studio-card" key={action.title}>
              <Icon size={20} />
              <div>
                <h3>{action.title}</h3>
                <p>{action.description}</p>
              </div>
              <span>{action.state}</span>
            </article>
          );
        })}
      </div>
    </section>
  );
}
