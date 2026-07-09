"use client";

import { createProduct, getSellerCategories, isLoggedIn } from "@/lib/api";
import { isSeller } from "@/lib/auth";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { FormEvent, useEffect, useState } from "react";

export default function NewProductPage() {
  const router = useRouter();
  const [categories, setCategories] = useState<
    { id: string; name: string; metadataFields: { name: string; values: string[] }[] }[]
  >([]);
  const [form, setForm] = useState({
    name: "",
    brand: "",
    description: "",
    categoryId: "",
    isCancellable: false,
    isReturnable: false,
  });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (!isLoggedIn()) {
      router.push("/login");
      return;
    }
    if (!isSeller()) {
      router.push("/");
      return;
    }

    getSellerCategories()
      .then((cats) => {
        const flat = cats.flatMap((c) => [
          {
            id: c.id,
            name: c.name,
            metadataFields: c.metadataFields || [],
          },
          ...c.children.map((ch) => ({
            id: ch.id,
            name: `— ${ch.name}`,
            metadataFields: c.metadataFields || [],
          })),
        ]);
        setCategories(flat);
      })
      .catch((err) => setError(err instanceof Error ? err.message : "Failed to load categories"))
      .finally(() => setLoading(false));
  }, [router]);

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setSaving(true);
    setError("");
    try {
      const productId = await createProduct(form);
      router.push(`/seller/products/${productId}`);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to create product");
      setSaving(false);
    }
  }

  if (loading) return <p>Loading...</p>;

  return (
    <div className="mx-auto max-w-lg space-y-6">
      <Link href="/seller/products" className="text-sm text-primary">
        ← Back to products
      </Link>
      <h1 className="text-3xl font-bold">New Product</h1>
      <p className="text-sm text-slate-600">
        Products are inactive until an admin approves them. You can add variations after approval.
      </p>

      <form onSubmit={handleSubmit} className="space-y-4 rounded-xl border border-slate-200 bg-white p-5">
        <div>
          <label className="mb-1 block text-sm font-medium">Name</label>
          <input
            required
            value={form.name}
            onChange={(e) => setForm((p) => ({ ...p, name: e.target.value }))}
            className="w-full rounded-lg border border-slate-300 px-3 py-2"
          />
        </div>
        <div>
          <label className="mb-1 block text-sm font-medium">Brand</label>
          <input
            required
            value={form.brand}
            onChange={(e) => setForm((p) => ({ ...p, brand: e.target.value }))}
            className="w-full rounded-lg border border-slate-300 px-3 py-2"
          />
        </div>
        <div>
          <label className="mb-1 block text-sm font-medium">Category</label>
          <select
            required
            value={form.categoryId}
            onChange={(e) => setForm((p) => ({ ...p, categoryId: e.target.value }))}
            className="w-full rounded-lg border border-slate-300 px-3 py-2"
          >
            <option value="">Select category</option>
            {categories.map((c) => (
              <option key={c.id} value={c.id}>
                {c.name}
              </option>
            ))}
          </select>
        </div>
        <div>
          <label className="mb-1 block text-sm font-medium">Description</label>
          <textarea
            value={form.description}
            onChange={(e) => setForm((p) => ({ ...p, description: e.target.value }))}
            className="w-full rounded-lg border border-slate-300 px-3 py-2"
            rows={3}
          />
        </div>
        <label className="flex items-center gap-2 text-sm">
          <input
            type="checkbox"
            checked={form.isCancellable}
            onChange={(e) => setForm((p) => ({ ...p, isCancellable: e.target.checked }))}
          />
          Cancellable
        </label>
        <label className="flex items-center gap-2 text-sm">
          <input
            type="checkbox"
            checked={form.isReturnable}
            onChange={(e) => setForm((p) => ({ ...p, isReturnable: e.target.checked }))}
          />
          Returnable
        </label>
        {error && <p className="text-red-600">{error}</p>}
        <button
          type="submit"
          disabled={saving}
          className="w-full rounded-lg bg-primary px-4 py-2.5 font-semibold text-white hover:bg-primary-dark"
        >
          {saving ? "Creating..." : "Create product"}
        </button>
      </form>
    </div>
  );
}
