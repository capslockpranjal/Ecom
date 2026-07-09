"use client";

import {
  activateSeller,
  deactivateSeller,
  getAdminSellers,
  isLoggedIn,
  AdminSeller,
} from "@/lib/api";
import { isAdmin } from "@/lib/auth";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";

export default function AdminSellersPage() {
  const router = useRouter();
  const [sellers, setSellers] = useState<AdminSeller[]>([]);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  async function load() {
    const data = await getAdminSellers();
    setSellers(data.content);
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

  async function toggleActive(seller: AdminSeller) {
    try {
      if (seller.isActive) await deactivateSeller(seller.id);
      else await activateSeller(seller.id);
      await load();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Action failed");
    }
  }

  if (loading) return <p>Loading sellers...</p>;

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center gap-4">
        <h1 className="text-3xl font-bold">Admin — Sellers</h1>
        <nav className="flex gap-3 text-sm">
          <Link href="/admin/customers" className="text-primary">Customers</Link>
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
              <th className="px-4 py-3">Company</th>
              <th className="px-4 py-3">Email</th>
              <th className="px-4 py-3">Status</th>
              <th className="px-4 py-3">Action</th>
            </tr>
          </thead>
          <tbody>
            {sellers.map((s) => (
              <tr key={s.id} className="border-b border-slate-100">
                <td className="px-4 py-3">{s.fullName}</td>
                <td className="px-4 py-3">{s.companyName}</td>
                <td className="px-4 py-3">{s.email}</td>
                <td className="px-4 py-3">{s.isActive ? "Active" : "Pending"}</td>
                <td className="px-4 py-3">
                  <button
                    onClick={() => toggleActive(s)}
                    className="text-primary hover:underline"
                  >
                    {s.isActive ? "Deactivate" : "Activate"}
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
