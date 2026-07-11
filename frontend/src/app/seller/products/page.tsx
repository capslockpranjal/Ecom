"use client";

import { getSellerProducts, isLoggedIn, SellerProduct } from "@/lib/api";
import { isSeller } from "@/lib/auth";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";

export default function SellerProductsPage() {
  const router = useRouter();
  const [products, setProducts] = useState<SellerProduct[]>([]);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!isLoggedIn()) {
      router.push("/login");
      return;
    }
    if (!isSeller()) {
      router.push("/");
      return;
    }

    getSellerProducts()
      .then(setProducts)
      .catch((err) => setError(err instanceof Error ? err.message : "Failed to load products"))
      .finally(() => setLoading(false));
  }, [router]);

  if (loading) return <p>Loading products...</p>;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-bold">My Products</h1>
        <Link
          href="/seller/products/new"
          className="rounded-lg bg-primary px-4 py-2 text-sm font-semibold text-white hover:bg-primary-dark"
        >
          Add product
        </Link>
      </div>

      {error && <p className="text-red-600">{error}</p>}

      {products.length === 0 ? (
        <p className="text-slate-600">
          No products yet. Create one and wait for admin approval before adding variations.
        </p>
      ) : (
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
              {products.map((product) => (
                <tr key={product.id} className="border-b border-slate-100">
                  <td className="px-4 py-3">
                    <Link
                      href={`/seller/products/${product.id}`}
                      className="font-medium text-primary"
                    >
                      {product.name}
                    </Link>
                  </td>
                  <td className="px-4 py-3">{product.brand}</td>
                  <td className="px-4 py-3">{product.categoryName}</td>
                  <td className="px-4 py-3">
                    <span
                      className={
                        product.isActive ? "text-green-700" : "text-amber-700"
                      }
                    >
                      {product.isActive ? "Active" : "Pending approval"}
                    </span>
                  </td>
                  <td className="px-4 py-3">
                    <Link
                      href={`/seller/products/${product.id}`}
                      className="font-medium text-primary hover:underline"
                    >
                      {product.isActive ? "Manage variations" : "View details"}
                    </Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
