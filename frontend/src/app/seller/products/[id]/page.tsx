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

type MetadataFieldInput = {
  name: string;
  values: string[];
};

function asArray<T>(value: T[] | null | undefined): T[] {
  return Array.isArray(value) ? value : [];
}

function normalizeMetadataFields(fields: MetadataFieldInput[]): MetadataFieldInput[] {
  const seen = new Set<string>();
  return fields
    .map((field) => ({
      name: field.name.trim(),
      values: asArray(field.values).map((value) => value.trim()).filter(Boolean),
    }))
    .filter((field) => {
      const key = field.name.toLowerCase();
      if (!field.name || seen.has(key)) return false;
      seen.add(key);
      return true;
    });
}

function getVariationMetadataFields(
  categoryFields: MetadataFieldInput[],
  variations: ProductVariation[]
): MetadataFieldInput[] {
  return Object.keys(variations[0]?.metadata ?? {}).map((key) => {
    const categoryField = categoryFields.find(
      (field) => field.name.toLowerCase() === key.toLowerCase()
    );
    return {
      name: key,
      values: categoryField?.values ?? [],
    };
  });
}

function getFormMetadataFields(
  categoryFields: MetadataFieldInput[],
  variations: ProductVariation[]
): MetadataFieldInput[] {
  if (variations.length > 0) {
    return getVariationMetadataFields(categoryFields, variations);
  }
  return categoryFields;
}

function buildInitialMetadata(fields: MetadataFieldInput[], variations: ProductVariation[]) {
  const initial: Record<string, string> = {};
  getFormMetadataFields(fields, variations).forEach((field) => {
    initial[field.name] = "";
  });
  return initial;
}

function sanitizeMetadata(metadata: Record<string, string>) {
  return Object.fromEntries(
    Object.entries(metadata)
      .map(([key, value]) => [key.trim(), String(value).trim()])
      .filter(([key, value]) => key && value)
  );
}

function isValidPriceInput(value: string) {
  return /^\d+(\.\d{1,2})?$/.test(value.trim());
}

function shiftPrice(value: string, delta: number) {
  const current = Number.parseFloat(value);
  const next = Math.max(0, (Number.isFinite(current) ? current : 0) + delta);
  return Number.isInteger(next) ? String(next) : next.toFixed(2);
}

export default function SellerProductDetailPage() {
  const params = useParams<{ id: string }>();
  const router = useRouter();
  const [product, setProduct] = useState<SellerProduct | null>(null);
  const [variations, setVariations] = useState<ProductVariation[]>([]);
  const [metadataFields, setMetadataFields] = useState<MetadataFieldInput[]>([]);
  const [metadata, setMetadata] = useState<Record<string, string>>({});
  const [quantity, setQuantity] = useState(1);
  const [price, setPrice] = useState("");
  const [image, setImage] = useState<File | null>(null);
  const [imageInputKey, setImageInputKey] = useState(0);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  async function load() {
    const products = asArray(await getSellerProducts({ productId: params.id }));
    const found = products[0];
    if (!found) throw new Error("Product not found");
    setProduct(found);
    const vars = asArray(await getSellerProductVariations(params.id));
    setVariations(vars);
    const cats = asArray(await getSellerCategories());
    const cat = cats.find((c) => c.id === found.categoryId);
    const fields = normalizeMetadataFields(asArray(cat?.metadataFields).map((field) => ({
      name: field.name,
      values: asArray(field.values),
    })));
    setMetadataFields(fields);
    setMetadata(buildInitialMetadata(fields, vars));
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

    if (!isValidPriceInput(price)) {
      setError("Enter a valid price with up to 2 decimal places");
      return;
    }

    const requestMetadata = sanitizeMetadata(metadata);
    if (Object.keys(requestMetadata).length === 0) {
      setError("At least one metadata field is required");
      return;
    }

    const requiredMetadataKeys =
      variations.length > 0 ? Object.keys(variations[0]?.metadata ?? {}) : [];
    const missingExistingKeys = requiredMetadataKeys.filter((key) => !requestMetadata[key]);
    if (missingExistingKeys.length > 0) {
      setError("Use the same metadata fields for all variations");
      return;
    }

    setSaving(true);
    setError("");
    try {
      await addVariationWithImage(
        {
          productId: product.id,
          quantityAvailable: quantity,
          price: Number(price),
          metadata: requestMetadata,
        },
        image
      );
      setMessage("Variation added");
      await load();
      setImage(null);
      setImageInputKey((key) => key + 1);
      setPrice("");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to add variation");
    } finally {
      setSaving(false);
    }
  }

  if (loading) return <p>Loading product...</p>;
  if (!product) return <p className="text-red-600">{error || "Product not found"}</p>;

  const formMetadataFields = getFormMetadataFields(metadataFields, variations);

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
                    {Object.entries(v.metadata ?? {})
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

      {!product.isActive && (
        <div className="rounded-xl border border-amber-200 bg-amber-50 p-5 text-sm text-amber-800">
          Add variation will be available after an admin approves this product.
        </div>
      )}

      {product.isActive && (
        <form
          onSubmit={handleAddVariation}
          className="rounded-xl border border-slate-200 bg-white p-5"
        >
          <h2 className="text-lg font-semibold">Add variation</h2>
          <div className="mt-4 grid gap-3 sm:grid-cols-2">
            <div>
              <label className="mb-1 block text-sm font-medium">Price (₹)</label>
              <div className="flex overflow-hidden rounded-lg border border-slate-300 bg-white">
                <button
                  type="button"
                  onClick={() => setPrice((value) => shiftPrice(value, -1))}
                  className="border-r border-slate-300 px-3 text-lg font-medium text-slate-700 hover:bg-slate-50"
                  aria-label="Decrease price"
                >
                  -
                </button>
                <input
                  type="text"
                  required
                  inputMode="decimal"
                  pattern="^\d+(\.\d{1,2})?$"
                  placeholder="0"
                  value={price}
                  onChange={(e) => {
                    const value = e.target.value.trim();
                    if (value === "" || /^\d+(\.\d{0,2})?$/.test(value)) {
                      setPrice(value);
                    }
                  }}
                  className="w-full border-0 px-3 py-2 outline-none"
                />
                <button
                  type="button"
                  onClick={() => setPrice((value) => shiftPrice(value, 1))}
                  className="border-l border-slate-300 px-3 text-lg font-medium text-slate-700 hover:bg-slate-50"
                  aria-label="Increase price"
                >
                  +
                </button>
              </div>
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
            {formMetadataFields.length > 0
              ? formMetadataFields.map((field) => (
                  <div key={field.name}>
                    <label className="mb-1 block text-sm font-medium capitalize">
                      {field.name}
                    </label>
                    {field.values.length > 0 ? (
                      <select
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
                      value={metadata[key] || ""}
                      onChange={(e) =>
                        setMetadata((prev) => ({ ...prev, [key]: e.target.value }))
                      }
                      className="w-full rounded-lg border border-slate-300 px-3 py-2"
                    />
                  </div>
                ))}
            {formMetadataFields.length === 0 && Object.keys(metadata).length === 0 && variations.length === 0 && (
              <p className="rounded-lg border border-amber-200 bg-amber-50 px-3 py-2 text-sm text-amber-800 sm:col-span-2">
                No metadata fields are configured for this category. Add category metadata before
                creating the first variation.
              </p>
            )}
            <div className="sm:col-span-2">
              <label className="mb-1 block text-sm font-medium">Primary image</label>
              <input
                key={imageInputKey}
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
