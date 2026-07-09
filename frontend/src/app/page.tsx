import Link from "next/link";

export default function HomePage() {
  return (
    <section className="space-y-8">
      <div className="rounded-2xl bg-gradient-to-br from-teal-700 to-emerald-600 p-10 text-white shadow-lg">
        <h1 className="text-4xl font-bold">Welcome to Zenvy</h1>
        <p className="mt-3 max-w-2xl text-lg text-teal-50">
          Browse products from multiple sellers, manage your cart, and checkout
          with cash on delivery.
        </p>
        <div className="mt-6 flex gap-3">
          <Link
            href="/shop"
            className="rounded-lg bg-white px-5 py-2.5 font-semibold text-teal-800 hover:bg-teal-50"
          >
            Start Shopping
          </Link>
          <Link
            href="/register"
            className="rounded-lg border border-white/70 px-5 py-2.5 font-semibold hover:bg-white/10"
          >
            Create Account
          </Link>
        </div>
      </div>

      <div className="grid gap-4 md:grid-cols-3">
        {[
          {
            title: "Multi-vendor catalog",
            text: "Explore categories and product variations with metadata like size and color.",
          },
          {
            title: "Secure checkout",
            text: "Place orders with saved addresses and COD payment support.",
          },
          {
            title: "Order tracking",
            text: "View order history and track seller fulfillment status.",
          },
        ].map((card) => (
          <div
            key={card.title}
            className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm"
          >
            <h2 className="font-semibold text-slate-900">{card.title}</h2>
            <p className="mt-2 text-sm text-slate-600">{card.text}</p>
          </div>
        ))}
      </div>
    </section>
  );
}
