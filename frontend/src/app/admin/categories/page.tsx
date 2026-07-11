"use client";

import {
  addCategoryMetadataValues,
  addMetadataField,
  createCategory,
  getAdminCategories,
  getMetadataFieldDefinitions,
  isLoggedIn,
  CategoryTree,
  MetadataFieldDefinition,
  updateCategoryName,
} from "@/lib/api";
import { isAdmin } from "@/lib/auth";
import {
  formatCategoryLabel,
  formatParentChain,
  sortCategoriesByTreeOrder,
} from "@/lib/categories";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { FormEvent, useEffect, useState } from "react";

function parseMetadataValues(raw: string): string[] {
  return raw
    .split(",")
    .map((value) => value.trim())
    .filter(Boolean);
}

type CategoryMetadataFormProps = {
  category: CategoryTree;
  fieldDefinitions: MetadataFieldDefinition[];
  onAdded: () => Promise<void>;
  onError: (message: string) => void;
};

function CategoryMetadataForm({
  category,
  fieldDefinitions,
  onAdded,
  onError,
}: CategoryMetadataFormProps) {
  const [fieldId, setFieldId] = useState("");
  const [valuesInput, setValuesInput] = useState("");
  const [saving, setSaving] = useState(false);

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    const values = parseMetadataValues(valuesInput);
    if (!fieldId) {
      onError("Select a metadata field");
      return;
    }
    if (values.length === 0) {
      onError("Enter at least one value (comma-separated)");
      return;
    }

    setSaving(true);
    onError("");
    try {
      await addCategoryMetadataValues(category.id, fieldId, values);
      setFieldId("");
      setValuesInput("");
      await onAdded();
    } catch (err) {
      onError(err instanceof Error ? err.message : "Failed to add metadata values");
    } finally {
      setSaving(false);
    }
  }

  if (fieldDefinitions.length === 0) {
    return (
      <p className="mt-3 text-xs text-amber-700">
        Create a metadata field above before assigning values to this category.
      </p>
    );
  }

  return (
    <form
      onSubmit={handleSubmit}
      className="mt-3 flex flex-wrap items-end gap-2 rounded-lg border border-slate-100 bg-slate-50 p-3"
    >
      <div>
        <label className="mb-1 block text-xs font-medium text-slate-600">Field</label>
        <select
          value={fieldId}
          onChange={(e) => setFieldId(e.target.value)}
          className="rounded-lg border border-slate-300 bg-white px-2 py-1.5 text-sm"
        >
          <option value="">Select field</option>
          {fieldDefinitions.map((field) => (
            <option key={field.id} value={field.id}>
              {field.name}
            </option>
          ))}
        </select>
      </div>
      <div className="min-w-[12rem] flex-1">
        <label className="mb-1 block text-xs font-medium text-slate-600">
          Allowed values
        </label>
        <input
          value={valuesInput}
          onChange={(e) => setValuesInput(e.target.value)}
          placeholder="e.g. S, M, L"
          className="w-full rounded-lg border border-slate-300 bg-white px-2 py-1.5 text-sm"
        />
      </div>
      <button
        type="submit"
        disabled={saving}
        className="rounded-lg bg-primary px-3 py-1.5 text-sm font-medium text-white hover:bg-primary-dark disabled:opacity-60"
      >
        {saving ? "Adding..." : "Add values"}
      </button>
    </form>
  );
}

export default function AdminCategoriesPage() {
  const router = useRouter();
  const [categories, setCategories] = useState<CategoryTree[]>([]);
  const [fieldDefinitions, setFieldDefinitions] = useState<MetadataFieldDefinition[]>([]);
  const [name, setName] = useState("");
  const [parentId, setParentId] = useState("");
  const [fieldName, setFieldName] = useState("");
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(true);
  const [creatingField, setCreatingField] = useState(false);

  async function load() {
    const [categoryData, fieldData] = await Promise.all([
      getAdminCategories(),
      getMetadataFieldDefinitions(),
    ]);
    setCategories(categoryData);
    setFieldDefinitions(fieldData);
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

  async function handleCreateField(event: FormEvent) {
    event.preventDefault();
    if (!fieldName.trim()) return;

    setCreatingField(true);
    setError("");
    setMessage("");
    try {
      await addMetadataField(fieldName);
      setMessage(`Metadata field "${fieldName.trim()}" created`);
      setFieldName("");
      await load();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to create metadata field");
    } finally {
      setCreatingField(false);
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

  const sortedCategories = sortCategoriesByTreeOrder(categories);

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
            {sortedCategories.map((category) => (
              <option key={category.id} value={category.id}>
                {formatCategoryLabel(category)}
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

      <section className="rounded-xl border border-slate-200 bg-white p-4">
        <h2 className="text-lg font-semibold">Metadata fields</h2>
        <p className="mt-1 text-sm text-slate-600">
          Define field names globally (e.g. size, color), then assign allowed values on leaf
          categories below. Sellers pick from those values when adding product variations.
        </p>

        <form
          onSubmit={handleCreateField}
          className="mt-4 flex flex-wrap items-end gap-3"
        >
          <div>
            <label className="mb-1 block text-sm font-medium">Field name</label>
            <input
              required
              value={fieldName}
              onChange={(e) => setFieldName(e.target.value)}
              placeholder="e.g. size"
              className="rounded-lg border border-slate-300 px-3 py-2"
            />
          </div>
          <button
            type="submit"
            disabled={creatingField}
            className="rounded-lg bg-primary px-4 py-2 font-semibold text-white hover:bg-primary-dark disabled:opacity-60"
          >
            {creatingField ? "Creating..." : "Create field"}
          </button>
        </form>

        {fieldDefinitions.length > 0 ? (
          <div className="mt-4 flex flex-wrap gap-2">
            {fieldDefinitions.map((field) => (
              <span
                key={field.id}
                className="rounded-full bg-slate-100 px-3 py-1 text-sm capitalize text-slate-700"
              >
                {field.name}
              </span>
            ))}
          </div>
        ) : (
          <p className="mt-4 text-sm text-slate-500">No metadata fields yet.</p>
        )}
      </section>

      {message && <p className="text-green-700">{message}</p>}
      {error && <p className="text-red-600">{error}</p>}

      <div className="space-y-3">
        {sortedCategories.map((cat) => (
          <div key={cat.id} className="rounded-xl border border-slate-200 bg-white p-4">
            <div className="flex items-center justify-between">
              <div>
                <p className="font-semibold">{formatCategoryLabel(cat)}</p>
                <p className="text-xs text-slate-500">{formatParentChain(cat)}</p>
              </div>
              <button
                onClick={() => handleRename(cat.id, cat.name)}
                className="text-sm text-primary"
              >
                Rename
              </button>
            </div>
            {cat.children.length > 0 && (
              <p className="mt-2 text-sm text-slate-600">
                Children: {cat.children.map((child) => child.name).join(", ")}
              </p>
            )}
            {cat.metadataFields.length > 0 ? (
              <div className="mt-2 space-y-1 text-sm text-slate-600">
                {cat.metadataFields.map((field) => (
                  <p key={field.fieldId}>
                    <span className="font-medium capitalize">{field.name}</span>:{" "}
                    {field.values.join(", ")}
                  </p>
                ))}
              </div>
            ) : cat.children.length === 0 ? (
              <p className="mt-2 text-sm text-slate-500">No metadata values assigned yet.</p>
            ) : (
              <p className="mt-2 text-sm text-slate-500">
                Metadata values are configured on leaf categories under this parent.
              </p>
            )}
            {cat.children.length === 0 ? (
              <CategoryMetadataForm
                category={cat}
                fieldDefinitions={fieldDefinitions}
                onAdded={async () => {
                  setMessage(`Metadata values added for ${cat.name}`);
                  await load();
                }}
                onError={setError}
              />
            ) : (
              <p className="mt-3 text-xs text-slate-500">
                Assign metadata on leaf subcategories. Parent categories aggregate child metadata
                for customer filters.
              </p>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}
