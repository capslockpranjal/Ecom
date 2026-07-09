"use client";

import {
  activateCustomer,
  deactivateCustomer,
  getAdminCustomers,
  isLoggedIn,
  AdminCustomer,
} from "@/lib/api";
import { isAdmin } from "@/lib/auth";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";

export default function AdminCustomersPage() {
  const router = useRouter();
  const [customers, setCustomers] = useState<AdminCustomer[]>([]);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  async function load() {
    const data = await getAdminCustomers();
    setCustomers(data.content);
  }

  useEffect(() => {
    if (!isLoggedIn()) {
      router.push("/login");
      return;
    }
    if (!isAdmin()) {
      router.push("/shop");
      return;
    }
    load()
      .catch((err) => setError(err instanceof Error ? err.message : "Failed to load"))
      .finally(() => setLoading(false));
  }, [router]);

  async function toggleActive(customer: AdminCustomer) {
    try {
      if (customer.isActive) await deactivateCustomer(customer.id);
      else await activateCustomer(customer.id);
      await load();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Action failed");
    }
  }

  if (loading) return <p>Loading customers...</p>;

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center gap-4">
        <h1 className="text-3xl font-bold">Admin — Customers</h1>
        <nav className="flex gap-3 text-sm">
          <Link href="/admin/sellers" className="text-primary">Sellers</Link>
          <Link href="/admin/products" className="text-primary">Products</Link>
          <Link href="/admin/categories" className="text-primary">Categories</Link>
          <Link href="/admin/orders" className="text-primary">Orders</Link>
        </nav>
      </div>
      {error && <p className="text-red-600">{error}</p>}
      <div className="overflow-hidden rounded-xl border border-slate-200 bg-white">
        <table className="w-full text-left text-sm">
          <thead className="border-b border-slate-200 bg-slate-50">
            <tr>
              <th className="px-4 py-3">Name</th>
              <th className="px-4 py-3">Email</th>
              <th className="px-4 py-3">Status</th>
              <th className="px-4 py-3">Action</th>
            </tr>
          </thead>
          <tbody>
            {customers.map((c) => (
              <tr key={c.id} className="border-b border-slate-100">
                <td className="px-4 py-3">{c.fullName}</td>
                <td className="px-4 py-3">{c.email}</td>
                <td className="px-4 py-3">{c.isActive ? "Active" : "Inactive"}</td>
                <td className="px-4 py-3">
                  <button
                    onClick={() => toggleActive(c)}
                    className="text-primary hover:underline"
                  >
                    {c.isActive ? "Deactivate" : "Activate"}
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
