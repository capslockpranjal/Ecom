"use client";

import {
  getSellerOrder,
  isLoggedIn,
  SellerOrderDetail,
  updateSellerOrderStatus,
} from "@/lib/api";
import { isSeller } from "@/lib/auth";
import Link from "next/link";
import { useParams, useRouter } from "next/navigation";
import { useEffect, useState } from "react";

const NEXT_STATUS: Record<string, string> = {
  PENDING: "CONFIRMED",
  CONFIRMED: "SHIPPED",
  SHIPPED: "DELIVERED",
};

export default function SellerOrderDetailPage() {
  const params = useParams<{ id: string }>();
  const router = useRouter();
  const [detail, setDetail] = useState<SellerOrderDetail | null>(null);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(true);
  const [acting, setActing] = useState(false);

  async function load() {
    const data = await getSellerOrder(params.id);
    setDetail(data);
  }

  useEffect(() => {
    if (!isLoggedIn()) {
      router.push("/login");
      return;
    }
    if (!isSeller()) {
      router.push("/shop");
      return;
    }

    load()
      .catch((err) =>
        setError(err instanceof Error ? err.message : "Failed to load order")
      )
      .finally(() => setLoading(false));
  }, [params.id, router]);

  async function advanceStatus() {
    if (!detail) return;
    const current = detail.sellerOrder.status;
    const next = NEXT_STATUS[current];
    if (!next) return;

    setActing(true);
    setError("");
    setMessage("");
    try {
      await updateSellerOrderStatus(
        detail.sellerOrderId,
        next as "CONFIRMED" | "SHIPPED" | "DELIVERED"
      );
      setMessage(`Status updated to ${next}`);
      await load();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to update status");
    } finally {
      setActing(false);
    }
  }

  async function cancelOrder() {
    if (!detail) return;
    setActing(true);
    setError("");
    try {
      await updateSellerOrderStatus(detail.sellerOrderId, "CANCELLED");
      setMessage("Order cancelled");
      await load();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to cancel");
    } finally {
      setActing(false);
    }
  }

  if (loading) return <p>Loading order...</p>;
  if (!detail) return <p className="text-red-600">{error || "Order not found"}</p>;

  const { sellerOrder } = detail;
  const nextStatus = NEXT_STATUS[sellerOrder.status];
  const canCancel = ["PENDING", "CONFIRMED"].includes(sellerOrder.status);

  return (
    <div className="space-y-6">
      <Link href="/seller/orders" className="text-sm font-medium text-primary">
        ← Back to seller orders
      </Link>

      <div className="rounded-xl border border-slate-200 bg-white p-5">
        <div className="flex flex-wrap items-start justify-between gap-4">
          <div>
            <h1 className="text-2xl font-bold">
              Order #{detail.orderId.slice(0, 8)}
            </h1>
            <p className="text-sm text-slate-600">
              Status: {sellerOrder.status} · Payment: {detail.paymentStatus}
            </p>
          </div>
          <div className="flex gap-2">
            {nextStatus && detail.paymentStatus === "PAID" && (
              <button
                onClick={advanceStatus}
                disabled={acting}
                className="rounded-lg bg-primary px-4 py-2 text-sm font-semibold text-white"
              >
                Mark {nextStatus}
              </button>
            )}
            {canCancel && (
              <button
                onClick={cancelOrder}
                disabled={acting}
                className="rounded-lg border border-red-300 px-4 py-2 text-sm text-red-600"
              >
                Cancel
              </button>
            )}
          </div>
        </div>

        <p className="mt-4 text-sm text-slate-600">
          Ship to: {detail.addressLine}, {detail.city}, {detail.state}{" "}
          {detail.zipCode}
        </p>

        {message && <p className="mt-3 text-sm text-green-700">{message}</p>}
        {error && <p className="mt-3 text-sm text-red-600">{error}</p>}
        {detail.paymentStatus !== "PAID" && (
          <p className="mt-3 text-sm text-amber-700">
            Waiting for customer payment before fulfillment.
          </p>
        )}
      </div>

      <div className="rounded-xl border border-slate-200 bg-white p-5">
        <h2 className="text-lg font-semibold">Items</h2>
        <div className="mt-4 space-y-3">
          {sellerOrder.items.map((item) => (
            <div
              key={item.id}
              className="flex justify-between border-t border-slate-100 pt-3 text-sm"
            >
              <div>
                <p className="font-medium">{item.productName}</p>
                <p className="text-slate-600">
                  {Object.entries(item.metadata)
                    .map(([k, v]) => `${k}: ${v}`)
                    .join(", ")}{" "}
                  × {item.quantity}
                </p>
              </div>
              <p className="font-medium">₹{item.lineTotal}</p>
            </div>
          ))}
        </div>
        <p className="mt-4 text-right font-semibold">
          Subtotal: ₹{sellerOrder.subtotal}
        </p>
      </div>
    </div>
  );
}
