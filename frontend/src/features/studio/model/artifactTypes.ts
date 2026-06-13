import { AudioLines, Brain, FileChartColumn, FileText, Layers3 } from "lucide-react";
import type { LucideIcon } from "lucide-react";
import type { StudioArtifactType } from "../../../shared/types";

export const artifactTypes: Array<{
  type: StudioArtifactType;
  title: string;
  description: string;
  icon: LucideIcon;
}> = [
  {
    type: "FLASHCARDS",
    title: "Flashcards",
    description: "Review cards with got it / missed tracking.",
    icon: Brain,
  },
  {
    type: "QUIZ",
    title: "Quiz",
    description: "Answer questions and see your score.",
    icon: Layers3,
  },
  {
    type: "BRIEFING",
    title: "Briefing",
    description: "A concise study document with key points.",
    icon: FileText,
  },
  {
    type: "PODCAST_SCRIPT",
    title: "Podcast audio",
    description: "Generate playable podcast audio with a saved script.",
    icon: AudioLines,
  },
  {
    type: "INFOGRAPHIC_OUTLINE",
    title: "Infographic",
    description: "Visual sections, hierarchy, and summary blocks.",
    icon: FileChartColumn,
  },
];
