"use client";

import {
  addToCart,
  getProduct,
  imageUrl,
  isLoggedIn,
  ProductDetail,
  ProductVariation,
} from "@/lib/api";
import { useParams, useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { Button } from "@/components/ui/Button";
import { ButtonLink } from "@/components/ui/ButtonLink";

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

  if (loading) {
    return (
      <div className="flex items-center gap-3 text-zen-800/70">
        <span className="h-5 w-5 animate-spin rounded-full border-2 border-primary border-t-transparent" />
        Loading product...
      </div>
    );
  }
  if (!product) {
    return (
      <p className="rounded-lg bg-red-50 px-4 py-3 text-red-700">
        {error || "Product not found"}
      </p>
    );
  }

  const image = imageUrl(selectedVariation?.primaryImage);

  return (
    <div className="grid gap-8 lg:grid-cols-2">
      <div className="overflow-hidden zenvy-card">
        <div className="flex h-96 items-center justify-center bg-zen-100">
          {image ? (
            // eslint-disable-next-line @next/next/no-img-element
            <img src={image} alt={product.name} className="h-full w-full object-cover" />
          ) : (
            <span className="text-zen-800/40">No image</span>
          )}
        </div>
      </div>

      <div className="space-y-6">
        <div>
          <p className="text-xs font-medium uppercase tracking-[0.2em] text-gold">
            {product.brand}
          </p>
          <h1 className="mt-2 font-display text-3xl font-semibold text-zen-900 md:text-4xl">
            {product.name}
          </h1>
          <p className="mt-3 leading-relaxed text-zen-800/70">{product.description}</p>
        </div>

        <div className="space-y-3">
          <p className="text-sm font-medium text-zen-800">Select variation</p>
          <div className="flex flex-wrap gap-2">
            {product.variations.map((variation) => (
              <button
                key={variation.id}
                onClick={() => setSelectedVariation(variation)}
                className={`rounded-xl border px-4 py-2.5 text-sm transition ${
                  selectedVariation?.id === variation.id
                    ? "border-primary bg-primary/10 font-medium text-primary"
                    : "border-zen-200 hover:border-primary/40"
                }`}
              >
                {Object.entries(variation.metadata)
                  .map(([key, value]) => `${key}: ${value}`)
                  .join(", ")}{" "}
                — ₹{variation.price}
              </button>
            ))}
          </div>
        </div>

        {selectedVariation && (
          <p className="text-sm text-zen-800/60">
            In stock: <span className="font-medium text-zen-900">{selectedVariation.quantityAvailable}</span>
          </p>
        )}

        <div className="flex items-center gap-3">
          <label className="text-sm font-medium text-zen-800">Quantity</label>
          <input
            type="number"
            min={1}
            max={selectedVariation?.quantityAvailable || 1}
            value={quantity}
            onChange={(e) => setQuantity(Number(e.target.value))}
            className="zenvy-input w-24"
          />
        </div>

        {error && (
          <p className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">{error}</p>
        )}
        {message && (
          <p className="rounded-lg bg-primary/10 px-3 py-2 text-sm text-primary-dark">{message}</p>
        )}

        <div className="flex flex-wrap gap-3 pt-2">
          <Button onClick={handleAddToCart} disabled={!selectedVariation}>
            Add to Cart
          </Button>
          <ButtonLink href="/cart" variant="outline">
            View Cart
          </ButtonLink>
        </div>
      </div>
    </div>
  );
}
