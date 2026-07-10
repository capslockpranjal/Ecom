import { ReactNode } from "react";

type AuthCardProps = {
  title: string;
  subtitle: string;
  children: ReactNode;
  footer?: ReactNode;
};

export function AuthCard({ title, subtitle, children, footer }: AuthCardProps) {
  return (
    <div className="mx-auto w-full max-w-md">
      <div className="mb-8 text-center">
        <p className="font-display text-sm font-medium uppercase tracking-[0.25em] text-gold">
          Welcome
        </p>
        <h1 className="mt-2 font-display text-3xl font-semibold text-zen-900">{title}</h1>
        <p className="mt-2 text-sm text-zen-800/70">{subtitle}</p>
      </div>

      <div className="zenvy-card p-6 md:p-8">
        {children}
        {footer && <div className="mt-6 border-t border-zen-100 pt-6">{footer}</div>}
      </div>
    </div>
  );
}
