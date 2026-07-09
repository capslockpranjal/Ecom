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
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold">Shop</h1>
        <p className="text-slate-600">
          {categoryId ? "Products in selected category" : "Choose a category to browse products"}
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
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          {categories.map((category) => (
            <Link
              key={category.id}
              href={`/shop?categoryId=${category.id}`}
              className="rounded-xl border border-slate-200 bg-white p-4 shadow-sm transition hover:border-primary"
            >
              <h2 className="font-semibold">{category.name}</h2>
              <p className="text-sm text-slate-600">Subcategory</p>
            </Link>
          ))}
        </div>
      )}

      {!loading && categoryId && (
        <div className="flex flex-col gap-6 lg:flex-row">
          {filters && (
            <aside className="w-full shrink-0 rounded-xl border border-slate-200 bg-white p-4 lg:w-64">
              <h2 className="font-semibold">Filters</h2>

              <div className="mt-3">
                <p className="text-sm font-medium text-slate-600">Price range (₹)</p>
                <div className="mt-2 flex gap-2">
                  <input
                    type="number"
                    placeholder={String(filters.minPrice)}
                    value={minPrice}
                    onChange={(e) => updateParams({ minPrice: e.target.value || null, page: "0" })}
                    className="w-full rounded border border-slate-300 px-2 py-1 text-sm"
                  />
                  <input
                    type="number"
                    placeholder={String(filters.maxPrice)}
                    value={maxPrice}
                    onChange={(e) => updateParams({ maxPrice: e.target.value || null, page: "0" })}
                    className="w-full rounded border border-slate-300 px-2 py-1 text-sm"
                  />
                </div>
              </div>

              {filters.brands.length > 0 && (
                <div className="mt-3">
                  <p className="text-sm font-medium text-slate-600">Brand</p>
                  <div className="mt-2 space-y-1">
                    <button
                      onClick={() => updateParams({ brand: null, page: "0" })}
                      className={`block text-sm ${!brandFilter ? "font-semibold text-primary" : ""}`}
                    >
                      All brands
                    </button>
                    {filters.brands.map((brand) => (
                      <button
                        key={brand}
                        onClick={() => updateParams({ brand, page: "0" })}
                        className={`block text-sm ${brandFilter === brand ? "font-semibold text-primary" : ""}`}
                      >
                        {brand}
                      </button>
                    ))}
                  </div>
                </div>
              )}

              {filters.metadata.map((field) => (
                <div key={field.fieldId} className="mt-3">
                  <p className="text-sm font-medium text-slate-600">{field.name}</p>
                  <div className="mt-2 flex flex-wrap gap-1">
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
                          className={`rounded-full border px-2 py-0.5 text-xs ${
                            active
                              ? "border-primary bg-teal-50 text-primary"
                              : "border-slate-300"
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

          <div className="flex-1 space-y-4">
            <div className="flex flex-wrap items-center gap-3">
              <select
                value={sort}
                onChange={(e) => updateParams({ sort: e.target.value, page: "0" })}
                className="rounded-lg border border-slate-300 px-3 py-2 text-sm"
              >
                <option value="createdAt">Newest</option>
                <option value="name">Name</option>
                <option value="brand">Brand</option>
              </select>
              <select
                value={order}
                onChange={(e) => updateParams({ order: e.target.value, page: "0" })}
                className="rounded-lg border border-slate-300 px-3 py-2 text-sm"
              >
                <option value="desc">Descending</option>
                <option value="asc">Ascending</option>
              </select>
            </div>

            <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
              {products.length === 0 ? (
                <p className="text-slate-600">No products match your filters.</p>
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
                          <img src={image} alt={product.name} className="h-full w-full object-cover" />
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

            {productPage && productPage.totalPages > 1 && (
              <div className="flex items-center gap-3">
                <button
                  disabled={page <= 0}
                  onClick={() => updateParams({ page: String(page - 1) })}
                  className="rounded-lg border border-slate-300 px-3 py-1.5 text-sm disabled:opacity-50"
                >
                  Previous
                </button>
                <span className="text-sm text-slate-600">
                  Page {page + 1} of {productPage.totalPages}
                </span>
                <button
                  disabled={productPage.last}
                  onClick={() => updateParams({ page: String(page + 1) })}
                  className="rounded-lg border border-slate-300 px-3 py-1.5 text-sm disabled:opacity-50"
                >
                  Next
                </button>
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
    <Suspense fallback={<p>Loading shop...</p>}>
      <ShopContent />
    </Suspense>
  );
}
