import { FeatureCard } from "@/components/ui/FeatureCard";
import { ButtonLink } from "@/components/ui/ButtonLink";

export default function HomePage() {
  return (
    <section className="space-y-16">
      <div className="relative overflow-hidden rounded-3xl bg-zen-gradient p-8 text-white shadow-zen-lg md:p-14">
        <div className="pointer-events-none absolute -right-16 -top-16 h-64 w-64 rounded-full bg-gold/10 blur-3xl" />
        <div className="pointer-events-none absolute -bottom-20 -left-10 h-48 w-48 rounded-full bg-white/5 blur-2xl" />

        <div className="relative max-w-2xl">
          <p className="text-sm font-medium uppercase tracking-[0.3em] text-gold-light/90">
            Zen · Curate · Desire
          </p>
          <h1 className="mt-4 font-display text-4xl font-semibold leading-tight md:text-5xl lg:text-6xl">
            Shop with intention.
            <span className="block text-gold-light">Discover what you&apos;ll envy.</span>
          </h1>
          <p className="mt-5 max-w-xl text-base leading-relaxed text-zen-100/90 md:text-lg">
            Zenvy brings together mindful curation and multi-vendor discovery — a calm
            marketplace where every product feels worth wanting.
          </p>
          <div className="mt-8 flex flex-wrap gap-3">
            <ButtonLink href="/shop" variant="secondary" size="lg">
              Explore the Shop
            </ButtonLink>
            <ButtonLink
              href="/register"
              variant="outline"
              size="lg"
              className="border-white/30 bg-white/10 text-white hover:bg-white/20 hover:text-white"
            >
              Create Account
            </ButtonLink>
          </div>
        </div>
      </div>

      <div className="grid gap-5 md:grid-cols-3">
        <FeatureCard
          icon={
            <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M4 6h16M4 10h16M4 14h10M4 18h6" />
            </svg>
          }
          title="Curated catalog"
          description="Browse categories and variations with rich metadata — size, color, and more — from trusted sellers."
        />
        <FeatureCard
          icon={
            <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
            </svg>
          }
          title="Peaceful checkout"
          description="Save addresses, review your cart with clarity, and place orders with cash on delivery."
        />
        <FeatureCard
          icon={
            <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M13 10V3L4 14h7v7l9-11h-7z" />
            </svg>
          }
          title="Order clarity"
          description="Track fulfillment, manage returns, and stay informed — without the noise."
        />
      </div>

      <div className="zenvy-card flex flex-col items-center gap-4 p-8 text-center md:flex-row md:justify-between md:text-left">
        <div>
          <h2 className="font-display text-2xl font-semibold text-zen-900">
            Ready to find something special?
          </h2>
          <p className="mt-1 text-zen-800/70">
            Join thousands discovering products they actually want.
          </p>
        </div>
        <ButtonLink href="/shop" size="lg">
          Start Browsing
        </ButtonLink>
      </div>
    </section>
  );
}
