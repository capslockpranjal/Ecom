import { ReactNode } from "react";

type PageHeaderProps = {
  title: string;
  subtitle?: string;
  action?: ReactNode;
};

export function PageHeader({ title, subtitle, action }: PageHeaderProps) {
  return (
    <div className="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
      <div>
        <h1 className="zenvy-page-title">{title}</h1>
        {subtitle && <p className="zenvy-page-subtitle">{subtitle}</p>}
      </div>
      {action}
    </div>
  );
}
