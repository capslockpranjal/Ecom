"use client";

import {
  addVariationWithImage,
  getSellerCategories,
  getSellerProductVariations,
  getSellerProducts,
  imageUrl,
  isLoggedIn,
  ProductVariation,
  SellerProduct,
  updateProduct,
} from "@/lib/api";
import { isSeller } from "@/lib/auth";
import Link from "next/link";
import { useParams, useRouter } from "next/navigation";
import { FormEvent, useEffect, useState } from "react";

export default function SellerProductDetailPage() {
  const params = useParams<{ id: string }>();
  const router = useRouter();
  const [product, setProduct] = useState<SellerProduct | null>(null);
  const [variations, setVariations] = useState<ProductVariation[]>([]);
  const [metadataFields, setMetadataFields] = useState<{ name: string; values: string[] }[]>([]);
  const [metadata, setMetadata] = useState<Record<string, string>>({});
  const [quantity, setQuantity] = useState(1);
  const [price, setPrice] = useState(0);
  const [image, setImage] = useState<File | null>(null);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  async function load() {
    const products = await getSellerProducts({ productId: params.id });
    const found = products[0];
    if (!found) throw new Error("Product not found");
    setProduct(found);
    const vars = await getSellerProductVariations(params.id);
    setVariations(vars);
    const cats = await getSellerCategories();
    const cat = cats.find((c) => c.id === found.categoryId);
    const fields = cat?.metadataFields || [];
    setMetadataFields(fields);
    if (vars[0]) {
      const initial: Record<string, string> = {};
      Object.keys(vars[0].metadata).forEach((k) => (initial[k] = ""));
      setMetadata(initial);
    } else if (fields.length > 0) {
      const initial: Record<string, string> = {};
      fields.forEach((f) => (initial[f.name] = ""));
      setMetadata(initial);
    }
  }

  useEffect(() => {
    if (!isLoggedIn()) {
      router.push("/login");
      return;
    }
    if (!isSeller()) {
      router.push("/");
      return;
    }

    load()
      .catch((err) => setError(err instanceof Error ? err.message : "Failed to load"))
      .finally(() => setLoading(false));
  }, [params.id, router]);

  async function handleUpdateProduct() {
    if (!product) return;
    setSaving(true);
    setError("");
    try {
      await updateProduct(product.id, {
        name: product.name,
        description: product.description,
        isCancellable: product.isCancellable,
        isReturnable: product.isReturnable,
      });
      setMessage("Product updated");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Update failed");
    } finally {
      setSaving(false);
    }
  }

  async function handleAddVariation(event: FormEvent) {
    event.preventDefault();
    if (!product || !image) {
      setError("Primary image is required");
      return;
    }
    setSaving(true);
    setError("");
    try {
      await addVariationWithImage(
        {
          productId: product.id,
          quantityAvailable: quantity,
          price,
          metadata,
        },
        image
      );
      setMessage("Variation added");
      await load();
      setImage(null);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to add variation");
    } finally {
      setSaving(false);
    }
  }

  if (loading) return <p>Loading product...</p>;
  if (!product) return <p className="text-red-600">{error || "Product not found"}</p>;

  return (
    <div className="space-y-6">
      <Link href="/seller/products" className="text-sm text-primary">
        ← Back to products
      </Link>

      <div className="rounded-xl border border-slate-200 bg-white p-5">
        <div className="flex items-start justify-between">
          <div>
            <h1 className="text-2xl font-bold">{product.name}</h1>
            <p className="text-slate-600">{product.brand} · {product.categoryName}</p>
            <p className="mt-2 text-sm">
              Status:{" "}
              <span className={product.isActive ? "text-green-700" : "text-amber-700"}>
                {product.isActive ? "Active" : "Pending admin approval"}
              </span>
            </p>
          </div>
        </div>
        <div className="mt-4 grid gap-3 sm:grid-cols-2">
          <input
            value={product.name}
            onChange={(e) => setProduct((p) => p && { ...p, name: e.target.value })}
            className="rounded-lg border border-slate-300 px-3 py-2"
          />
          <textarea
            value={product.description || ""}
            onChange={(e) => setProduct((p) => p && { ...p, description: e.target.value })}
            className="rounded-lg border border-slate-300 px-3 py-2 sm:col-span-2"
            rows={2}
          />
          <label className="flex items-center gap-2 text-sm">
            <input
              type="checkbox"
              checked={product.isCancellable}
              onChange={(e) =>
                setProduct((p) => p && { ...p, isCancellable: e.target.checked })
              }
            />
            Cancellable
          </label>
          <label className="flex items-center gap-2 text-sm">
            <input
              type="checkbox"
              checked={product.isReturnable}
              onChange={(e) =>
                setProduct((p) => p && { ...p, isReturnable: e.target.checked })
              }
            />
            Returnable
          </label>
        </div>
        <button
          onClick={handleUpdateProduct}
          disabled={saving}
          className="mt-4 rounded-lg border border-slate-300 px-4 py-2 text-sm font-medium hover:bg-slate-50"
        >
          Save product
        </button>
      </div>

      <div className="rounded-xl border border-slate-200 bg-white p-5">
        <h2 className="text-lg font-semibold">Variations ({variations.length})</h2>
        {variations.length === 0 ? (
          <p className="mt-2 text-sm text-slate-600">No variations yet.</p>
        ) : (
          <div className="mt-4 space-y-3">
            {variations.map((v) => (
              <div key={v.id} className="flex items-center gap-4 border-t border-slate-100 pt-3">
                {v.primaryImage && (
                  // eslint-disable-next-line @next/next/no-img-element
                  <img
                    src={imageUrl(v.primaryImage) || ""}
                    alt=""
                    className="h-16 w-16 rounded object-cover"
                  />
                )}
                <div className="text-sm">
                  <p>
                    {Object.entries(v.metadata)
                      .map(([k, val]) => `${k}: ${val}`)
                      .join(", ")}
                  </p>
                  <p className="text-slate-600">
                    ₹{v.price} · Stock: {v.quantityAvailable} ·{" "}
                    {v.isActive ? "Active" : "Inactive"}
                  </p>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {product.isActive && (
        <form
          onSubmit={handleAddVariation}
          className="rounded-xl border border-slate-200 bg-white p-5"
        >
          <h2 className="text-lg font-semibold">Add variation</h2>
          <div className="mt-4 grid gap-3 sm:grid-cols-2">
            <div>
              <label className="mb-1 block text-sm font-medium">Price (₹)</label>
              <input
                type="number"
                required
                min={0}
                step={0.01}
                value={price}
                onChange={(e) => setPrice(Number(e.target.value))}
                className="w-full rounded-lg border border-slate-300 px-3 py-2"
              />
            </div>
            <div>
              <label className="mb-1 block text-sm font-medium">Stock</label>
              <input
                type="number"
                required
                min={0}
                value={quantity}
                onChange={(e) => setQuantity(Number(e.target.value))}
                className="w-full rounded-lg border border-slate-300 px-3 py-2"
              />
            </div>
            {metadataFields.length > 0
              ? metadataFields.map((field) => (
                  <div key={field.name}>
                    <label className="mb-1 block text-sm font-medium capitalize">
                      {field.name}
                    </label>
                    {field.values.length > 0 ? (
                      <select
                        required
                        value={metadata[field.name] || ""}
                        onChange={(e) =>
                          setMetadata((prev) => ({ ...prev, [field.name]: e.target.value }))
                        }
                        className="w-full rounded-lg border border-slate-300 px-3 py-2"
                      >
                        <option value="">Select {field.name}</option>
                        {field.values.map((v) => (
                          <option key={v} value={v}>
                            {v}
                          </option>
                        ))}
                      </select>
                    ) : (
                      <input
                        required
                        value={metadata[field.name] || ""}
                        onChange={(e) =>
                          setMetadata((prev) => ({ ...prev, [field.name]: e.target.value }))
                        }
                        className="w-full rounded-lg border border-slate-300 px-3 py-2"
                      />
                    )}
                  </div>
                ))
              : Object.keys(metadata).map((key) => (
                  <div key={key}>
                    <label className="mb-1 block text-sm font-medium capitalize">{key}</label>
                    <input
                      required
                      value={metadata[key] || ""}
                      onChange={(e) =>
                        setMetadata((prev) => ({ ...prev, [key]: e.target.value }))
                      }
                      className="w-full rounded-lg border border-slate-300 px-3 py-2"
                    />
                  </div>
                ))}
            {metadataFields.length === 0 && Object.keys(metadata).length === 0 && variations.length === 0 && (
              <p className="text-sm text-slate-600 sm:col-span-2">
                Add the first variation with metadata keys matching your category (e.g. color, size).
                Use the same keys for all variations.
              </p>
            )}
            <div className="sm:col-span-2">
              <label className="mb-1 block text-sm font-medium">Primary image</label>
              <input
                type="file"
                accept="image/*"
                required
                onChange={(e) => setImage(e.target.files?.[0] || null)}
              />
            </div>
          </div>
          <button
            type="submit"
            disabled={saving}
            className="mt-4 rounded-lg bg-primary px-4 py-2 font-semibold text-white hover:bg-primary-dark"
          >
            Add variation
          </button>
        </form>
      )}

      {message && <p className="text-green-700">{message}</p>}
      {error && <p className="text-red-600">{error}</p>}
    </div>
  );
}
