"use client";

import {
  Cart,
  getCart,
  imageUrl,
  isLoggedIn,
  removeCartItem,
  updateCartItem,
} from "@/lib/api";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";

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

  if (loading) return <p>Loading cart...</p>;

  return (
    <div className="space-y-6">
      <h1 className="text-3xl font-bold">Your Cart</h1>
      {error && <p className="text-red-600">{error}</p>}

      {!cart || cart.items.length === 0 ? (
        <div className="rounded-xl border border-dashed border-slate-300 bg-white p-8 text-center">
          <p className="text-slate-600">Your cart is empty.</p>
          <Link href="/shop" className="mt-4 inline-block text-primary">
            Continue shopping
          </Link>
        </div>
      ) : (
        <div className="grid gap-6 lg:grid-cols-[2fr_1fr]">
          <div className="space-y-4">
            {cart.items.map((item) => {
              const image = imageUrl(item.primaryImage);
              return (
                <div
                  key={item.id}
                  className="flex gap-4 rounded-xl border border-slate-200 bg-white p-4"
                >
                  <div className="h-24 w-24 overflow-hidden rounded-lg bg-slate-100">
                    {image ? (
                      // eslint-disable-next-line @next/next/no-img-element
                      <img
                        src={image}
                        alt={item.productName}
                        className="h-full w-full object-cover"
                      />
                    ) : null}
                  </div>
                  <div className="flex-1">
                    <h2 className="font-semibold">{item.productName}</h2>
                    <p className="text-sm text-slate-600">{item.brand}</p>
                    <p className="text-sm text-slate-600">
                      {Object.entries(item.metadata)
                        .map(([k, v]) => `${k}: ${v}`)
                        .join(", ")}
                    </p>
                    <div className="mt-3 flex items-center gap-3">
                      <input
                        type="number"
                        min={1}
                        max={item.quantityAvailable}
                        value={item.quantity}
                        onChange={(e) =>
                          handleQuantityChange(item.id, Number(e.target.value))
                        }
                        className="w-20 rounded-lg border border-slate-300 px-2 py-1"
                      />
                      <button
                        onClick={() => handleRemove(item.id)}
                        className="text-sm text-red-600"
                      >
                        Remove
                      </button>
                    </div>
                  </div>
                  <div className="font-semibold">₹{item.lineTotal}</div>
                </div>
              );
            })}
          </div>

          <div className="h-fit rounded-xl border border-slate-200 bg-white p-5">
            <h2 className="text-lg font-semibold">Order Summary</h2>
            <div className="mt-4 flex justify-between text-sm">
              <span>Items ({cart.itemCount})</span>
              <span>₹{cart.subtotal}</span>
            </div>
            <div className="mt-2 flex justify-between border-t border-slate-200 pt-3 font-semibold">
              <span>Total</span>
              <span>₹{cart.subtotal}</span>
            </div>
            <Link
              href="/checkout"
              className="mt-5 block rounded-lg bg-primary px-4 py-2.5 text-center font-semibold text-white hover:bg-primary-dark"
            >
              Proceed to Checkout
            </Link>
          </div>
        </div>
      )}
    </div>
  );
}
