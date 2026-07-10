import Link from "next/link";

type LogoProps = {
  className?: string;
  showTagline?: boolean;
};

export function Logo({ className = "", showTagline = false }: LogoProps) {
  return (
    <Link href="/" className={`group inline-flex items-center gap-2.5 ${className}`}>
      <span className="relative flex h-9 w-9 items-center justify-center">
        <span className="absolute inset-0 rounded-full bg-gradient-to-br from-primary/20 to-gold/20 transition group-hover:from-primary/30 group-hover:to-gold/30" />
        <span className="relative font-display text-lg font-bold text-primary">Z</span>
        <span className="absolute -bottom-0.5 -right-0.5 h-2 w-2 rounded-full bg-gold/80" />
      </span>
      <span className="flex flex-col">
        <span className="font-display text-xl font-semibold tracking-tight text-zen-900">
          Zenvy
        </span>
        {showTagline && (
          <span className="text-[10px] font-medium uppercase tracking-[0.2em] text-zen-800/50">
            Curated with calm
          </span>
        )}
      </span>
    </Link>
  );
}
