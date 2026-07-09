"use client";

import { cancelOrder, confirmPayment, getOrder, isLoggedIn, Order } from "@/lib/api";
import Link from "next/link";
import { useParams, useRouter } from "next/navigation";
import { useEffect, useState } from "react";

export default function OrderDetailPage() {
  const params = useParams<{ id: string }>();
  const router = useRouter();
  const [order, setOrder] = useState<Order | null>(null);
  const [error, setError] = useState("");
  const [actionMessage, setActionMessage] = useState("");
  const [loading, setLoading] = useState(true);
  const [acting, setActing] = useState(false);

  async function loadOrder() {
    const data = await getOrder(params.id);
    setOrder(data);
  }

  useEffect(() => {
    if (!isLoggedIn()) {
      router.push("/login");
      return;
    }

    loadOrder()
      .catch((err) =>
        setError(err instanceof Error ? err.message : "Failed to load order")
      )
      .finally(() => setLoading(false));
  }, [params.id, router]);

  const canCancel =
    order &&
    order.status === "PLACED" &&
    order.sellerOrders.every((so) => so.status === "PENDING");

  const needsPayment =
    order &&
    order.paymentMethod === "ONLINE" &&
    order.paymentStatus === "PENDING" &&
    order.status !== "CANCELLED";

  async function handleCancel() {
    if (!order || !canCancel) return;
    setActing(true);
    setError("");
    setActionMessage("");
    try {
      await cancelOrder(order.id);
      setActionMessage("Order cancelled");
      await loadOrder();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to cancel order");
    } finally {
      setActing(false);
    }
  }

  async function handleConfirmPayment() {
    if (!order || !needsPayment) return;
    setActing(true);
    setError("");
    setActionMessage("");
    try {
      const updated = await confirmPayment(order.id);
      setOrder(updated);
      setActionMessage("Payment confirmed");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Payment failed");
    } finally {
      setActing(false);
    }
  }

  if (loading) return <p>Loading order...</p>;
  if (!order) return <p className="text-red-600">{error || "Order not found"}</p>;

  return (
    <div className="space-y-6">
      <Link href="/orders" className="text-sm font-medium text-primary">
        ← Back to orders
      </Link>

      <div className="rounded-xl border border-slate-200 bg-white p-5">
        <div className="flex flex-wrap items-start justify-between gap-4">
          <div>
            <h1 className="text-2xl font-bold">Order #{order.id.slice(0, 8)}</h1>
            <p className="mt-1 text-sm text-slate-600">
              Placed on {new Date(order.createdAt).toLocaleString()}
            </p>
          </div>
          <div className="flex gap-2">
            {needsPayment && (
              <button
                onClick={handleConfirmPayment}
                disabled={acting}
                className="rounded-lg bg-primary px-4 py-2 text-sm font-semibold text-white hover:bg-primary-dark"
              >
                Confirm Payment
              </button>
            )}
            {canCancel && (
              <button
                onClick={handleCancel}
                disabled={acting}
                className="rounded-lg border border-red-300 px-4 py-2 text-sm font-semibold text-red-600 hover:bg-red-50"
              >
                Cancel Order
              </button>
            )}
          </div>
        </div>

        <div className="mt-4 grid gap-2 text-sm md:grid-cols-2">
          <p>
            <span className="font-medium">Status:</span> {order.status}
          </p>
          <p>
            <span className="font-medium">Payment:</span> {order.paymentStatus} (
            {order.paymentMethod})
          </p>
          <p>
            <span className="font-medium">Total:</span> ₹{order.totalAmount}
          </p>
          <p>
            <span className="font-medium">Address:</span> {order.addressLine},{" "}
            {order.city}, {order.state} {order.zipCode}
          </p>
        </div>

        {actionMessage && (
          <p className="mt-3 text-sm text-green-700">{actionMessage}</p>
        )}
        {error && <p className="mt-3 text-sm text-red-600">{error}</p>}
      </div>

      {order.sellerOrders.map((sellerOrder) => (
        <div
          key={sellerOrder.id}
          className="rounded-xl border border-slate-200 bg-white p-5"
        >
          <div className="flex items-center justify-between">
            <h2 className="text-lg font-semibold">
              {sellerOrder.sellerCompanyName}
            </h2>
            <span className="text-sm text-slate-600">{sellerOrder.status}</span>
          </div>
          <div className="mt-4 space-y-3">
            {sellerOrder.items.map((item) => (
              <div
                key={item.id}
                className="flex items-center justify-between border-t border-slate-100 pt-3 text-sm"
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
      ))}
    </div>
  );
}
