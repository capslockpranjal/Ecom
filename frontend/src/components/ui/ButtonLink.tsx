import Link from "next/link";
import { ReactNode } from "react";

type ButtonLinkProps = {
  href: string;
  variant?: "primary" | "secondary" | "outline";
  size?: "sm" | "md" | "lg";
  className?: string;
  children: ReactNode;
};

const variantClasses = {
  primary:
    "bg-primary text-white shadow-zen hover:bg-primary-dark",
  secondary:
    "bg-gold text-white shadow-zen hover:bg-gold-dark",
  outline:
    "border border-zen-200 bg-white text-zen-900 hover:border-primary/40 hover:bg-zen-50",
};

const sizeClasses = {
  sm: "px-3 py-1.5 text-sm",
  md: "px-5 py-2.5 text-sm",
  lg: "px-6 py-3 text-base",
};

export function ButtonLink({
  href,
  variant = "primary",
  size = "md",
  className = "",
  children,
}: ButtonLinkProps) {
  return (
    <Link
      href={href}
      className={`inline-flex items-center justify-center rounded-xl font-semibold transition ${variantClasses[variant]} ${sizeClasses[size]} ${className}`}
    >
      {children}
    </Link>
  );
}
