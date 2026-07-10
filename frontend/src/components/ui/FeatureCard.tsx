import { ReactNode } from "react";

type FeatureCardProps = {
  icon: ReactNode;
  title: string;
  description: string;
};

export function FeatureCard({ icon, title, description }: FeatureCardProps) {
  return (
    <div className="group zenvy-card p-6 transition hover:border-primary/30 hover:shadow-zen-lg">
      <div className="mb-4 flex h-11 w-11 items-center justify-center rounded-xl bg-zen-100 text-primary transition group-hover:bg-primary/10">
        {icon}
      </div>
      <h2 className="font-display text-lg font-semibold text-zen-900">{title}</h2>
      <p className="mt-2 text-sm leading-relaxed text-zen-800/70">{description}</p>
    </div>
  );
}
