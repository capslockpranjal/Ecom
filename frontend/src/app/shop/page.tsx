"use client";

import {
  Category,
  FilteringData,
  getCategories,
  getCategoryFilters,
  getProducts,
  imageUrl,
  isLoggedIn,
  ProductListItem,
  SpringPage,
} from "@/lib/api";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { Suspense, useEffect, useState } from "react";
import { PageHeader } from "@/components/ui/PageHeader";
import { Button } from "@/components/ui/Button";

const PAGE_SIZE = 12;

function ShopContent() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const categoryId = searchParams.get("categoryId") || undefined;
  const page = Number(searchParams.get("page") || "0");
  const sort = searchParams.get("sort") || "createdAt";
  const order = searchParams.get("order") || "desc";
  const brandFilter = searchParams.get("brand") || "";
  const minPrice = searchParams.get("minPrice") || "";
  const maxPrice = searchParams.get("maxPrice") || "";

  const [categories, setCategories] = useState<Category[]>([]);
  const [filters, setFilters] = useState<FilteringData | null>(null);
  const [metadataFilters, setMetadataFilters] = useState<Record<string, string>>({});
  const [productPage, setProductPage] = useState<SpringPage<ProductListItem> | null>(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  function updateParams(updates: Record<string, string | null>) {
    const params = new URLSearchParams(searchParams.toString());
    Object.entries(updates).forEach(([key, value]) => {
      if (value === null || value === "") params.delete(key);
      else params.set(key, value);
    });
    router.push(`/shop?${params.toString()}`);
  }

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
          const metadata: Record<string, string> = {};
          searchParams.forEach((value, key) => {
            if (key.startsWith("metadata.") && value) {
              metadata[key.replace("metadata.", "")] = value;
            }
          });
          setMetadataFilters(metadata);

          const [products, filterData] = await Promise.all([
            getProducts(categoryId, {
              max: PAGE_SIZE,
              offset: page,
              sort,
              order,
              filters: {
                brand: brandFilter || undefined,
                minPrice: minPrice ? Number(minPrice) : undefined,
                maxPrice: maxPrice ? Number(maxPrice) : undefined,
                metadata: Object.keys(metadata).length > 0 ? metadata : undefined,
              },
            }),
            getCategoryFilters(categoryId),
          ]);
          setProductPage(products);
          setFilters(filterData);
        } else {
          setProductPage(null);
          setFilters(null);
        }
      } catch (err) {
        setError(err instanceof Error ? err.message : "Failed to load shop");
      } finally {
        setLoading(false);
      }
    }

    load();
  }, [categoryId, page, sort, order, brandFilter, minPrice, maxPrice, searchParams, router]);

  const products = productPage?.content || [];

  function applyMetadataFilter(field: string, value: string) {
    const next = { ...metadataFilters, [field]: value };
    const updates: Record<string, string | null> = { page: "0" };
    Object.keys(next).forEach((key) => {
      updates[`metadata.${key}`] = next[key];
    });
    updateParams(updates);
  }

  function clearMetadataFilter(field: string) {
    const next = { ...metadataFilters };
    delete next[field];
    const updates: Record<string, string | null> = { page: "0" };
    Object.keys(next).forEach((key) => {
      updates[`metadata.${key}`] = next[key];
    });
    updateParams({ ...updates, [`metadata.${field}`]: null });
  }

  return (
    <div className="space-y-8">
      <PageHeader
        title="Shop"
        subtitle={
          categoryId
            ? "Products in your selected category"
            : "Choose a category and discover curated finds"
        }
      />

      {categoryId && (
        <Link
          href="/shop"
          className="inline-flex items-center gap-1 text-sm font-medium text-primary hover:underline"
        >
          ← All categories
        </Link>
      )}

      {error && (
        <p className="rounded-lg bg-red-50 px-4 py-3 text-sm text-red-700">{error}</p>
      )}
      {loading && (
        <div className="flex items-center gap-3 text-zen-800/70">
          <span className="h-5 w-5 animate-spin rounded-full border-2 border-primary border-t-transparent" />
          Loading...
        </div>
      )}

      {!loading && !categoryId && (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {categories.map((category) => (
            <Link
              key={category.id}
              href={`/shop?categoryId=${category.id}`}
              className="group zenvy-card p-6 transition hover:border-primary/40 hover:shadow-zen-lg"
            >
              <div className="mb-3 flex h-10 w-10 items-center justify-center rounded-xl bg-zen-100 text-primary transition group-hover:bg-primary/10">
                <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
                </svg>
              </div>
              <h2 className="font-display text-xl font-semibold text-zen-900">{category.name}</h2>
              <p className="mt-1 text-sm text-zen-800/60">Browse collection →</p>
            </Link>
          ))}
        </div>
      )}

      {!loading && categoryId && categories.length > 0 && (
        <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
          {categories.map((category) => (
            <Link
              key={category.id}
              href={`/shop?categoryId=${category.id}`}
              className="zenvy-card p-4 transition hover:border-primary/40"
            >
              <h2 className="font-medium text-zen-900">{category.name}</h2>
              <p className="text-xs text-zen-800/50">Subcategory</p>
            </Link>
          ))}
        </div>
      )}

      {!loading && categoryId && (
        <div className="flex flex-col gap-6 lg:flex-row">
          {filters && (
            <aside className="zenvy-card w-full shrink-0 p-5 lg:w-72">
              <h2 className="font-display text-lg font-semibold text-zen-900">Filters</h2>

              <div className="mt-4">
                <p className="text-sm font-medium text-zen-800/70">Price range (₹)</p>
                <div className="mt-2 flex gap-2">
                  <input
                    type="number"
                    placeholder={String(filters.minPrice)}
                    value={minPrice}
                    onChange={(e) => updateParams({ minPrice: e.target.value || null, page: "0" })}
                    className="zenvy-input py-1.5 text-sm"
                  />
                  <input
                    type="number"
                    placeholder={String(filters.maxPrice)}
                    value={maxPrice}
                    onChange={(e) => updateParams({ maxPrice: e.target.value || null, page: "0" })}
                    className="zenvy-input py-1.5 text-sm"
                  />
                </div>
              </div>

              {filters.brands.length > 0 && (
                <div className="mt-4">
                  <p className="text-sm font-medium text-zen-800/70">Brand</p>
                  <div className="mt-2 space-y-1">
                    <button
                      onClick={() => updateParams({ brand: null, page: "0" })}
                      className={`block rounded-lg px-2 py-1 text-sm transition hover:bg-zen-100 ${!brandFilter ? "font-semibold text-primary" : "text-zen-800/70"}`}
                    >
                      All brands
                    </button>
                    {filters.brands.map((brand) => (
                      <button
                        key={brand}
                        onClick={() => updateParams({ brand, page: "0" })}
                        className={`block rounded-lg px-2 py-1 text-sm transition hover:bg-zen-100 ${brandFilter === brand ? "font-semibold text-primary" : "text-zen-800/70"}`}
                      >
                        {brand}
                      </button>
                    ))}
                  </div>
                </div>
              )}

              {filters.metadata.map((field) => (
                <div key={field.fieldId} className="mt-4">
                  <p className="text-sm font-medium text-zen-800/70">{field.name}</p>
                  <div className="mt-2 flex flex-wrap gap-1.5">
                    {field.values.map((value) => {
                      const active = metadataFilters[field.name] === value;
                      return (
                        <button
                          key={value}
                          onClick={() =>
                            active
                              ? clearMetadataFilter(field.name)
                              : applyMetadataFilter(field.name, value)
                          }
                          className={`rounded-full border px-2.5 py-0.5 text-xs transition ${
                            active
                              ? "border-primary bg-primary/10 font-medium text-primary"
                              : "border-zen-200 text-zen-800/70 hover:border-primary/40"
                          }`}
                        >
                          {value}
                        </button>
                      );
                    })}
                  </div>
                </div>
              ))}
            </aside>
          )}

          <div className="flex-1 space-y-5">
            <div className="flex flex-wrap items-center gap-3">
              <select
                value={sort}
                onChange={(e) => updateParams({ sort: e.target.value, page: "0" })}
                className="zenvy-input w-auto py-2 text-sm"
              >
                <option value="createdAt">Newest</option>
                <option value="name">Name</option>
                <option value="brand">Brand</option>
              </select>
              <select
                value={order}
                onChange={(e) => updateParams({ order: e.target.value, page: "0" })}
                className="zenvy-input w-auto py-2 text-sm"
              >
                <option value="desc">Descending</option>
                <option value="asc">Ascending</option>
              </select>
            </div>

            <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
              {products.length === 0 ? (
                <p className="col-span-full rounded-xl border border-dashed border-zen-200 bg-zen-50/50 p-8 text-center text-zen-800/60">
                  No products match your filters.
                </p>
              ) : (
                products.map((product) => {
                  const image = imageUrl(product.primaryImages?.[0]);
                  return (
                    <Link
                      key={product.id}
                      href={`/products/${product.id}`}
                      className="group overflow-hidden zenvy-card transition hover:border-primary/30 hover:shadow-zen-lg"
                    >
                      <div className="flex h-48 items-center justify-center bg-zen-100">
                        {image ? (
                          // eslint-disable-next-line @next/next/no-img-element
                          <img
                            src={image}
                            alt={product.name}
                            className="h-full w-full object-cover transition duration-300 group-hover:scale-105"
                          />
                        ) : (
                          <span className="text-sm text-zen-800/40">No image</span>
                        )}
                      </div>
                      <div className="p-4">
                        <p className="text-xs font-medium uppercase tracking-wide text-gold">
                          {product.brand}
                        </p>
                        <h2 className="mt-1 font-medium text-zen-900 group-hover:text-primary">
                          {product.name}
                        </h2>
                      </div>
                    </Link>
                  );
                })
              )}
            </div>

            {productPage && productPage.totalPages > 1 && (
              <div className="flex items-center gap-3">
                <Button
                  variant="outline"
                  size="sm"
                  disabled={page <= 0}
                  onClick={() => updateParams({ page: String(page - 1) })}
                >
                  Previous
                </Button>
                <span className="text-sm text-zen-800/60">
                  Page {page + 1} of {productPage.totalPages}
                </span>
                <Button
                  variant="outline"
                  size="sm"
                  disabled={productPage.last}
                  onClick={() => updateParams({ page: String(page + 1) })}
                >
                  Next
                </Button>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}

export default function ShopPage() {
  return (
    <Suspense
      fallback={
        <div className="flex items-center gap-3 text-zen-800/70">
          <span className="h-5 w-5 animate-spin rounded-full border-2 border-primary border-t-transparent" />
          Loading shop...
        </div>
      }
    >
      <ShopContent />
    </Suspense>
  );
}
