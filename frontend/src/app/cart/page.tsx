"use client";

import {
  Cart,
  getCart,
  imageUrl,
  isLoggedIn,
  removeCartItem,
  updateCartItem,
} from "@/lib/api";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { PageHeader } from "@/components/ui/PageHeader";
import { ButtonLink } from "@/components/ui/ButtonLink";

export default function CartPage() {
  const router = useRouter();
  const [cart, setCart] = useState<Cart | null>(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  async function loadCart() {
    setLoading(true);
    setError("");
    try {
      const data = await getCart();
      setCart(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to load cart");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    if (!isLoggedIn()) {
      router.push("/login");
      return;
    }
    loadCart();
  }, [router]);

  async function handleQuantityChange(itemId: string, quantity: number) {
    try {
      const data = await updateCartItem(itemId, quantity);
      setCart(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to update item");
    }
  }

  async function handleRemove(itemId: string) {
    try {
      const data = await removeCartItem(itemId);
      setCart(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to remove item");
    }
  }

  if (loading) {
    return (
      <div className="flex items-center gap-3 text-zen-800/70">
        <span className="h-5 w-5 animate-spin rounded-full border-2 border-primary border-t-transparent" />
        Loading cart...
      </div>
    );
  }

  return (
    <div className="space-y-8">
      <PageHeader
        title="Your Cart"
        subtitle="Review your selections before checkout"
      />

      {error && (
        <p className="rounded-lg bg-red-50 px-4 py-3 text-sm text-red-700">{error}</p>
      )}

      {!cart || cart.items.length === 0 ? (
        <div className="rounded-2xl border border-dashed border-zen-200 bg-zen-50/50 p-12 text-center">
          <p className="font-display text-xl text-zen-800/70">Your cart is empty</p>
          <p className="mt-2 text-sm text-zen-800/50">
            Discover something you&apos;ll love in the shop.
          </p>
          <ButtonLink href="/shop" className="mt-6">
            Continue Shopping
          </ButtonLink>
        </div>
      ) : (
        <div className="grid gap-6 lg:grid-cols-[2fr_1fr]">
          <div className="space-y-4">
            {cart.items.map((item) => {
              const image = imageUrl(item.primaryImage);
              return (
                <div key={item.id} className="flex gap-4 zenvy-card p-4 md:p-5">
                  <div className="h-24 w-24 shrink-0 overflow-hidden rounded-xl bg-zen-100">
                    {image ? (
                      // eslint-disable-next-line @next/next/no-img-element
                      <img
                        src={image}
                        alt={item.productName}
                        className="h-full w-full object-cover"
                      />
                    ) : null}
                  </div>
                  <div className="min-w-0 flex-1">
                    <p className="text-xs font-medium uppercase tracking-wide text-gold">
                      {item.brand}
                    </p>
                    <h2 className="font-medium text-zen-900">{item.productName}</h2>
                    <p className="mt-1 text-sm text-zen-800/60">
                      {Object.entries(item.metadata)
                        .map(([k, v]) => `${k}: ${v}`)
                        .join(", ")}
                    </p>
                    <div className="mt-3 flex flex-wrap items-center gap-3">
                      <input
                        type="number"
                        min={1}
                        max={item.quantityAvailable}
                        value={item.quantity}
                        onChange={(e) =>
                          handleQuantityChange(item.id, Number(e.target.value))
                        }
                        className="zenvy-input w-20 py-1.5 text-sm"
                      />
                      <button
                        onClick={() => handleRemove(item.id)}
                        className="text-sm text-red-600/80 transition hover:text-red-700"
                      >
                        Remove
                      </button>
                    </div>
                  </div>
                  <div className="shrink-0 font-display text-lg font-semibold text-zen-900">
                    ₹{item.lineTotal}
                  </div>
                </div>
              );
            })}
          </div>

          <div className="zenvy-card h-fit p-6">
            <h2 className="font-display text-xl font-semibold text-zen-900">Order Summary</h2>
            <div className="mt-5 flex justify-between text-sm text-zen-800/70">
              <span>Items ({cart.itemCount})</span>
              <span>₹{cart.subtotal}</span>
            </div>
            <div className="mt-3 flex justify-between border-t border-zen-100 pt-4 font-display text-lg font-semibold text-zen-900">
              <span>Total</span>
              <span>₹{cart.subtotal}</span>
            </div>
            <ButtonLink href="/checkout" className="mt-6 w-full">
              Proceed to Checkout
            </ButtonLink>
          </div>
        </div>
      )}
    </div>
  );
}
