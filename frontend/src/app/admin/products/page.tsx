"use client";

import {
  activateProduct,
  deactivateProduct,
  getAdminProducts,
  imageUrl,
  isLoggedIn,
  AdminProduct,
} from "@/lib/api";
import { isAdmin } from "@/lib/auth";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";

export default function AdminProductsPage() {
  const router = useRouter();
  const [products, setProducts] = useState<AdminProduct[]>([]);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  async function load() {
    const data = await getAdminProducts({ max: 50 });
    setProducts(data.content);
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

  async function toggleActive(product: AdminProduct) {
    try {
      if (product.isActive) await deactivateProduct(product.id);
      else await activateProduct(product.id);
      await load();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Action failed");
    }
  }

  if (loading) return <p>Loading products...</p>;

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center gap-4">
        <h1 className="text-3xl font-bold">Admin — Products</h1>
        <nav className="flex gap-3 text-sm">
          <Link href="/admin/customers" className="text-primary">Customers</Link>
          <Link href="/admin/sellers" className="text-primary">Sellers</Link>
          <Link href="/admin/categories" className="text-primary">Categories</Link>
          <Link href="/admin/orders" className="text-primary">Orders</Link>
        </nav>
      </div>
      {error && <p className="text-red-600">{error}</p>}
      <div className="overflow-hidden rounded-xl border border-slate-200 bg-white">
        <table className="w-full text-left text-sm">
          <thead className="border-b border-slate-200 bg-slate-50">
            <tr>
              <th className="px-4 py-3">Product</th>
              <th className="px-4 py-3">Brand</th>
              <th className="px-4 py-3">Category</th>
              <th className="px-4 py-3">Status</th>
              <th className="px-4 py-3">Action</th>
            </tr>
          </thead>
          <tbody>
            {products.map((p) => (
              <tr key={p.id} className="border-b border-slate-100">
                <td className="px-4 py-3">
                  <div className="flex items-center gap-3">
                    {p.primaryImages[0] && (
                      // eslint-disable-next-line @next/next/no-img-element
                      <img
                        src={imageUrl(p.primaryImages[0]) || ""}
                        alt=""
                        className="h-10 w-10 rounded object-cover"
                      />
                    )}
                    <span className="font-medium">{p.name}</span>
                  </div>
                </td>
                <td className="px-4 py-3">{p.brand}</td>
                <td className="px-4 py-3">{p.category.name}</td>
                <td className="px-4 py-3">{p.isActive ? "Active" : "Inactive"}</td>
                <td className="px-4 py-3">
                  <button
                    onClick={() => toggleActive(p)}
                    className="text-primary hover:underline"
                  >
                    {p.isActive ? "Deactivate" : "Activate"}
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
