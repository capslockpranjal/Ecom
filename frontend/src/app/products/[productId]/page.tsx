"use client";

import {
  addToCart,
  getProduct,
  imageUrl,
  isLoggedIn,
  ProductDetail,
  ProductVariation,
} from "@/lib/api";
import Link from "next/link";
import { useParams, useRouter } from "next/navigation";
import { useEffect, useState } from "react";

export default function ProductPage() {
  const params = useParams<{ productId: string }>();
  const router = useRouter();
  const [product, setProduct] = useState<ProductDetail | null>(null);
  const [selectedVariation, setSelectedVariation] =
    useState<ProductVariation | null>(null);
  const [quantity, setQuantity] = useState(1);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!isLoggedIn()) {
      router.push("/login");
      return;
    }

    async function load() {
      try {
        const data = await getProduct(params.productId);
        setProduct(data);
        setSelectedVariation(data.variations[0] || null);
      } catch (err) {
        setError(err instanceof Error ? err.message : "Failed to load product");
      } finally {
        setLoading(false);
      }
    }

    load();
  }, [params.productId, router]);

  async function handleAddToCart() {
    if (!selectedVariation) return;
    setError("");
    setMessage("");

    try {
      await addToCart(selectedVariation.id, quantity);
      setMessage("Added to cart");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to add to cart");
    }
  }

  if (loading) return <p>Loading product...</p>;
  if (!product) return <p className="text-red-600">{error || "Product not found"}</p>;

  const image = imageUrl(selectedVariation?.primaryImage);

  return (
    <div className="grid gap-8 lg:grid-cols-2">
      <div className="overflow-hidden rounded-xl border border-slate-200 bg-white">
        <div className="flex h-80 items-center justify-center bg-slate-100">
          {image ? (
            // eslint-disable-next-line @next/next/no-img-element
            <img src={image} alt={product.name} className="h-full w-full object-cover" />
          ) : (
            <span className="text-slate-500">No image</span>
          )}
        </div>
      </div>

      <div className="space-y-4">
        <div>
          <p className="text-sm uppercase tracking-wide text-slate-500">
            {product.brand}
          </p>
          <h1 className="text-3xl font-bold">{product.name}</h1>
          <p className="mt-2 text-slate-600">{product.description}</p>
        </div>

        <div className="space-y-2">
          <p className="font-medium">Select variation</p>
          <div className="flex flex-wrap gap-2">
            {product.variations.map((variation) => (
              <button
                key={variation.id}
                onClick={() => setSelectedVariation(variation)}
                className={`rounded-lg border px-3 py-2 text-sm ${
                  selectedVariation?.id === variation.id
                    ? "border-primary bg-teal-50 text-primary"
                    : "border-slate-300"
                }`}
              >
                {Object.entries(variation.metadata)
                  .map(([key, value]) => `${key}: ${value}`)
                  .join(", ")}{" "}
                - ₹{variation.price}
              </button>
            ))}
          </div>
        </div>

        {selectedVariation && (
          <p className="text-sm text-slate-600">
            In stock: {selectedVariation.quantityAvailable}
          </p>
        )}

        <div className="flex items-center gap-3">
          <label className="text-sm font-medium">Qty</label>
          <input
            type="number"
            min={1}
            max={selectedVariation?.quantityAvailable || 1}
            value={quantity}
            onChange={(e) => setQuantity(Number(e.target.value))}
            className="w-20 rounded-lg border border-slate-300 px-3 py-2"
          />
        </div>

        {error && <p className="text-sm text-red-600">{error}</p>}
        {message && <p className="text-sm text-green-700">{message}</p>}

        <div className="flex gap-3">
          <button
            onClick={handleAddToCart}
            disabled={!selectedVariation}
            className="rounded-lg bg-primary px-5 py-2.5 font-semibold text-white hover:bg-primary-dark"
          >
            Add to Cart
          </button>
          <Link
            href="/cart"
            className="rounded-lg border border-slate-300 px-5 py-2.5 font-semibold hover:bg-slate-50"
          >
            View Cart
          </Link>
        </div>
      </div>
    </div>
  );
}
