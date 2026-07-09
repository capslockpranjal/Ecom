"use client";

import { getAdminOrder, isLoggedIn, Order } from "@/lib/api";
import { isAdmin } from "@/lib/auth";
import Link from "next/link";
import { useParams, useRouter } from "next/navigation";
import { useEffect, useState } from "react";

export default function AdminOrderDetailPage() {
  const params = useParams<{ id: string }>();
  const router = useRouter();
  const [order, setOrder] = useState<Order | null>(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!isLoggedIn()) {
      router.push("/login");
      return;
    }
    if (!isAdmin()) {
      router.push("/shop");
      return;
    }

    getAdminOrder(params.id)
      .then(setOrder)
      .catch((err) =>
        setError(err instanceof Error ? err.message : "Failed to load order")
      )
      .finally(() => setLoading(false));
  }, [params.id, router]);

  if (loading) return <p>Loading order...</p>;
  if (!order) return <p className="text-red-600">{error || "Order not found"}</p>;

  return (
    <div className="space-y-6">
      <Link href="/admin/orders" className="text-sm font-medium text-primary">
        ← Back to all orders
      </Link>

      <div className="rounded-xl border border-slate-200 bg-white p-5">
        <h1 className="text-2xl font-bold">Order #{order.id.slice(0, 8)}</h1>
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
            <span className="font-medium">Sellers:</span> {order.sellerOrders.length}
          </p>
          <p className="md:col-span-2">
            <span className="font-medium">Address:</span> {order.addressLine},{" "}
            {order.city}, {order.state} {order.zipCode}, {order.country}
          </p>
        </div>
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
          <div className="mt-4 space-y-2 text-sm">
            {sellerOrder.items.map((item) => (
              <div key={item.id} className="flex justify-between">
                <span>
                  {item.productName} × {item.quantity}
                </span>
                <span>₹{item.lineTotal}</span>
              </div>
            ))}
          </div>
          <p className="mt-3 text-right font-semibold">
            Subtotal: ₹{sellerOrder.subtotal}
          </p>
        </div>
      ))}
    </div>
  );
}
