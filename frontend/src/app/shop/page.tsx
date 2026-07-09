"use client";

import {
  Category,
  getCategories,
  getProducts,
  imageUrl,
  isLoggedIn,
  ProductListItem,
} from "@/lib/api";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { Suspense, useEffect, useState } from "react";

function ShopContent() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const categoryId = searchParams.get("categoryId") || undefined;

  const [categories, setCategories] = useState<Category[]>([]);
  const [products, setProducts] = useState<ProductListItem[]>([]);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!isLoggedIn()) {
      router.push("/login");
      return;
    }

    async function load() {
      setLoading(true);
      setError("");
      try {
        const categoryData = await getCategories(categoryId);
        setCategories(categoryData);

        if (categoryId) {
          const productData = await getProducts(categoryId);
          setProducts(productData);
        } else {
          setProducts([]);
        }
      } catch (err) {
        setError(err instanceof Error ? err.message : "Failed to load shop");
      } finally {
        setLoading(false);
      }
    }

    load();
  }, [categoryId, router]);

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold">Shop</h1>
        <p className="text-slate-600">
          {categoryId
            ? "Products in selected category"
            : "Choose a category to browse products"}
        </p>
      </div>

      {categoryId && (
        <Link href="/shop" className="text-sm font-medium text-primary">
          ← Back to categories
        </Link>
      )}

      {error && <p className="text-red-600">{error}</p>}
      {loading && <p className="text-slate-600">Loading...</p>}

      {!loading && !categoryId && (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {categories.map((category) => (
            <Link
              key={category.id}
              href={`/shop?categoryId=${category.id}`}
              className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm transition hover:border-primary hover:shadow-md"
            >
              <h2 className="text-lg font-semibold">{category.name}</h2>
              <p className="mt-2 text-sm text-slate-600">Browse products</p>
            </Link>
          ))}
        </div>
      )}

      {!loading && categoryId && categories.length > 0 && (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {categories.map((category) => (
            <Link
              key={category.id}
              href={`/shop?categoryId=${category.id}`}
              className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm transition hover:border-primary hover:shadow-md"
            >
              <h2 className="text-lg font-semibold">{category.name}</h2>
              <p className="mt-2 text-sm text-slate-600">Browse subcategory</p>
            </Link>
          ))}
        </div>
      )}

      {!loading && categoryId && (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {products.length === 0 ? (
            <p className="text-slate-600">No products found in this category.</p>
          ) : (
            products.map((product) => {
              const image = imageUrl(product.primaryImages?.[0]);
              return (
                <Link
                  key={product.id}
                  href={`/products/${product.id}`}
                  className="overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm transition hover:shadow-md"
                >
                  <div className="flex h-44 items-center justify-center bg-slate-100">
                    {image ? (
                      // eslint-disable-next-line @next/next/no-img-element
                      <img
                        src={image}
                        alt={product.name}
                        className="h-full w-full object-cover"
                      />
                    ) : (
                      <span className="text-sm text-slate-500">No image</span>
                    )}
                  </div>
                  <div className="p-4">
                    <h2 className="font-semibold">{product.name}</h2>
                    <p className="text-sm text-slate-600">{product.brand}</p>
                  </div>
                </Link>
              );
            })
          )}
        </div>
      )}
    </div>
  );
}

export default function ShopPage() {
  return (
    <Suspense fallback={<p>Loading shop...</p>}>
      <ShopContent />
    </Suspense>
  );
}
