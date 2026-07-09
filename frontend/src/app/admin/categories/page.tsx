"use client";

import {
  createCategory,
  getAdminCategories,
  isLoggedIn,
  CategoryTree,
  updateCategoryName,
} from "@/lib/api";
import { isAdmin } from "@/lib/auth";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { FormEvent, useEffect, useState } from "react";

export default function AdminCategoriesPage() {
  const router = useRouter();
  const [categories, setCategories] = useState<CategoryTree[]>([]);
  const [name, setName] = useState("");
  const [parentId, setParentId] = useState("");
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(true);

  async function load() {
    const data = await getAdminCategories();
    setCategories(data);
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

  async function handleCreate(event: FormEvent) {
    event.preventDefault();
    setError("");
    setMessage("");
    try {
      await createCategory(name, parentId || undefined);
      setMessage("Category created");
      setName("");
      setParentId("");
      await load();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Create failed");
    }
  }

  async function handleRename(id: string, currentName: string) {
    const newName = prompt("New category name", currentName);
    if (!newName || newName === currentName) return;
    try {
      await updateCategoryName(id, newName);
      await load();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Rename failed");
    }
  }

  if (loading) return <p>Loading categories...</p>;

  const flatCategories = categories.flatMap((c) => [
    { id: c.id, name: c.name },
    ...c.children.map((ch) => ({ id: ch.id, name: `— ${ch.name}` })),
  ]);

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center gap-4">
        <h1 className="text-3xl font-bold">Admin — Categories</h1>
        <nav className="flex gap-3 text-sm">
          <Link href="/admin/customers" className="text-primary">Customers</Link>
          <Link href="/admin/sellers" className="text-primary">Sellers</Link>
          <Link href="/admin/products" className="text-primary">Products</Link>
          <Link href="/admin/orders" className="text-primary">Orders</Link>
        </nav>
      </div>

      <form
        onSubmit={handleCreate}
        className="flex flex-wrap items-end gap-3 rounded-xl border border-slate-200 bg-white p-4"
      >
        <div>
          <label className="mb-1 block text-sm font-medium">Name</label>
          <input
            required
            value={name}
            onChange={(e) => setName(e.target.value)}
            className="rounded-lg border border-slate-300 px-3 py-2"
          />
        </div>
        <div>
          <label className="mb-1 block text-sm font-medium">Parent (optional)</label>
          <select
            value={parentId}
            onChange={(e) => setParentId(e.target.value)}
            className="rounded-lg border border-slate-300 px-3 py-2"
          >
            <option value="">None (root)</option>
            {flatCategories.map((c) => (
              <option key={c.id} value={c.id}>
                {c.name}
              </option>
            ))}
          </select>
        </div>
        <button
          type="submit"
          className="rounded-lg bg-primary px-4 py-2 font-semibold text-white hover:bg-primary-dark"
        >
          Create
        </button>
      </form>

      {message && <p className="text-green-700">{message}</p>}
      {error && <p className="text-red-600">{error}</p>}

      <div className="space-y-3">
        {categories.map((cat) => (
          <div key={cat.id} className="rounded-xl border border-slate-200 bg-white p-4">
            <div className="flex items-center justify-between">
              <p className="font-semibold">{cat.name}</p>
              <button
                onClick={() => handleRename(cat.id, cat.name)}
                className="text-sm text-primary"
              >
                Rename
              </button>
            </div>
            {cat.children.length > 0 && (
              <ul className="mt-2 space-y-1 text-sm text-slate-600">
                {cat.children.map((child) => (
                  <li key={child.id} className="flex items-center justify-between">
                    <span>{child.name}</span>
                    <button
                      onClick={() => handleRename(child.id, child.name)}
                      className="text-primary"
                    >
                      Rename
                    </button>
                  </li>
                ))}
              </ul>
            )}
            {cat.metadataFields.length > 0 && (
              <p className="mt-2 text-xs text-slate-500">
                Metadata: {cat.metadataFields.map((f) => f.name).join(", ")}
              </p>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}
