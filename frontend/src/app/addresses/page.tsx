"use client";

import {
  addAddress,
  Address,
  deleteAddress,
  getAddresses,
  isLoggedIn,
  updateAddress,
} from "@/lib/api";
import { isCustomer } from "@/lib/auth";
import { useRouter } from "next/navigation";
import { FormEvent, useEffect, useState } from "react";

const emptyForm = {
  label: "Home",
  addressLine: "",
  city: "",
  state: "",
  country: "India",
  zipCode: "",
};

export default function AddressesPage() {
  const router = useRouter();
  const [addresses, setAddresses] = useState<Address[]>([]);
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [showForm, setShowForm] = useState(false);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  async function load() {
    const data = await getAddresses();
    setAddresses(data);
  }

  useEffect(() => {
    if (!isLoggedIn()) {
      router.push("/login");
      return;
    }
    if (!isCustomer()) {
      router.push("/");
      return;
    }
    load()
      .catch((err) => setError(err instanceof Error ? err.message : "Failed to load"))
      .finally(() => setLoading(false));
  }, [router]);

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setError("");
    try {
      if (editingId) {
        await updateAddress(editingId, form);
      } else {
        await addAddress(form);
      }
      await load();
      setForm(emptyForm);
      setEditingId(null);
      setShowForm(false);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Save failed");
    }
  }

  function startEdit(address: Address) {
    setEditingId(address.id);
    setForm({
      label: address.label,
      addressLine: address.addressLine,
      city: address.city,
      state: address.state,
      country: address.country,
      zipCode: address.zipCode,
    });
    setShowForm(true);
  }

  async function handleDelete(id: string) {
    if (!confirm("Delete this address?")) return;
    try {
      await deleteAddress(id);
      await load();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Delete failed");
    }
  }

  if (loading) return <p>Loading addresses...</p>;

  return (
    <div className="mx-auto max-w-2xl space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-bold">My Addresses</h1>
        <button
          onClick={() => {
            setShowForm(true);
            setEditingId(null);
            setForm(emptyForm);
          }}
          className="rounded-lg bg-primary px-4 py-2 text-sm font-semibold text-white hover:bg-primary-dark"
        >
          Add address
        </button>
      </div>

      {error && <p className="text-red-600">{error}</p>}

      {showForm && (
        <form onSubmit={handleSubmit} className="rounded-xl border border-slate-200 bg-white p-5">
          <h2 className="font-semibold">{editingId ? "Edit address" : "New address"}</h2>
          <div className="mt-4 grid gap-3">
            {Object.entries({
              label: "Label",
              addressLine: "Address line",
              city: "City",
              state: "State",
              country: "Country",
              zipCode: "ZIP code",
            }).map(([key, label]) => (
              <div key={key}>
                <label className="mb-1 block text-sm font-medium">{label}</label>
                <input
                  required
                  value={form[key as keyof typeof form]}
                  onChange={(e) => setForm((prev) => ({ ...prev, [key]: e.target.value }))}
                  className="w-full rounded-lg border border-slate-300 px-3 py-2"
                />
              </div>
            ))}
          </div>
          <div className="mt-4 flex gap-2">
            <button type="submit" className="rounded-lg bg-primary px-4 py-2 text-white">
              Save
            </button>
            <button
              type="button"
              onClick={() => setShowForm(false)}
              className="rounded-lg border border-slate-300 px-4 py-2"
            >
              Cancel
            </button>
          </div>
        </form>
      )}

      {addresses.length === 0 ? (
        <p className="text-slate-600">No saved addresses yet.</p>
      ) : (
        <div className="space-y-3">
          {addresses.map((address) => (
            <div
              key={address.id}
              className="flex items-start justify-between rounded-xl border border-slate-200 bg-white p-4"
            >
              <div>
                <p className="font-medium">{address.label}</p>
                <p className="text-sm text-slate-600">
                  {address.addressLine}, {address.city}, {address.state} {address.zipCode},{" "}
                  {address.country}
                </p>
              </div>
              <div className="flex gap-2 text-sm">
                <button onClick={() => startEdit(address)} className="text-primary">
                  Edit
                </button>
                <button onClick={() => handleDelete(address.id)} className="text-red-600">
                  Delete
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
