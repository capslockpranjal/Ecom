"use client";

import { getAdminOrders, isLoggedIn, OrderSummary } from "@/lib/api";
import { isAdmin } from "@/lib/auth";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";

export default function AdminOrdersPage() {
  const router = useRouter();
  const [orders, setOrders] = useState<OrderSummary[]>([]);
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

    getAdminOrders()
      .then(setOrders)
      .catch((err) =>
        setError(err instanceof Error ? err.message : "Failed to load orders")
      )
      .finally(() => setLoading(false));
  }, [router]);

  if (loading) return <p>Loading admin orders...</p>;

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center gap-4">
        <h1 className="text-3xl font-bold">Admin — All Orders</h1>
        <nav className="flex gap-3 text-sm">
          <Link href="/admin/customers" className="text-primary">Customers</Link>
          <Link href="/admin/sellers" className="text-primary">Sellers</Link>
          <Link href="/admin/products" className="text-primary">Products</Link>
          <Link href="/admin/categories" className="text-primary">Categories</Link>
        </nav>
      </div>
      {error && <p className="text-red-600">{error}</p>}

      {orders.length === 0 ? (
        <p className="text-slate-600">No orders in the system.</p>
      ) : (
        <div className="overflow-hidden rounded-xl border border-slate-200 bg-white">
          <table className="w-full text-left text-sm">
            <thead className="border-b border-slate-200 bg-slate-50">
              <tr>
                <th className="px-4 py-3">Order</th>
                <th className="px-4 py-3">Date</th>
                <th className="px-4 py-3">Status</th>
                <th className="px-4 py-3">Payment</th>
                <th className="px-4 py-3">Total</th>
              </tr>
            </thead>
            <tbody>
              {orders.map((order) => (
                <tr key={order.id} className="border-b border-slate-100">
                  <td className="px-4 py-3">
                    <Link
                      href={`/admin/orders/${order.id}`}
                      className="font-medium text-primary"
                    >
                      #{order.id.slice(0, 8)}
                    </Link>
                  </td>
                  <td className="px-4 py-3">
                    {new Date(order.createdAt).toLocaleString()}
                  </td>
                  <td className="px-4 py-3">{order.status}</td>
                  <td className="px-4 py-3">
                    {order.paymentStatus} ({order.paymentMethod})
                  </td>
                  <td className="px-4 py-3">₹{order.totalAmount}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
