"use client";

import {
  addAddress,
  Address,
  checkout,
  getAddresses,
  isLoggedIn,
} from "@/lib/api";
import { useRouter } from "next/navigation";
import { FormEvent, useEffect, useState } from "react";

export default function CheckoutPage() {
  const router = useRouter();
  const [addresses, setAddresses] = useState<Address[]>([]);
  const [selectedAddressId, setSelectedAddressId] = useState("");
  const [showForm, setShowForm] = useState(false);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);
  const [placing, setPlacing] = useState(false);
  const [paymentMethod, setPaymentMethod] = useState<"COD" | "ONLINE">("COD");
  const [form, setForm] = useState({
    label: "Home",
    addressLine: "",
    city: "",
    state: "",
    country: "India",
    zipCode: "",
  });

  useEffect(() => {
    if (!isLoggedIn()) {
      router.push("/login");
      return;
    }

    async function load() {
      try {
        const data = await getAddresses();
        setAddresses(data);
        if (data[0]) setSelectedAddressId(data[0].id);
      } catch (err) {
        setError(err instanceof Error ? err.message : "Failed to load addresses");
      } finally {
        setLoading(false);
      }
    }

    load();
  }, [router]);

  async function handleAddAddress(event: FormEvent) {
    event.preventDefault();
    try {
      await addAddress(form);
      const data = await getAddresses();
      setAddresses(data);
      if (data[0]) setSelectedAddressId(data[data.length - 1].id);
      setShowForm(false);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to add address");
    }
  }

  async function handleCheckout() {
    if (!selectedAddressId) {
      setError("Please select or add a delivery address");
      return;
    }

    setPlacing(true);
    setError("");
    try {
      const order = await checkout(selectedAddressId, paymentMethod);
      router.push(`/orders/${order.id}`);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Checkout failed");
    } finally {
      setPlacing(false);
    }
  }

  if (loading) return <p>Loading checkout...</p>;

  return (
    <div className="mx-auto max-w-2xl space-y-6">
      <h1 className="text-3xl font-bold">Checkout</h1>

      <div className="rounded-xl border border-slate-200 bg-white p-5">
        <h2 className="text-lg font-semibold">Payment Method</h2>
        <div className="mt-3 flex gap-3">
          <label className="flex cursor-pointer items-center gap-2 rounded-lg border border-slate-200 px-4 py-3">
            <input
              type="radio"
              name="payment"
              checked={paymentMethod === "COD"}
              onChange={() => setPaymentMethod("COD")}
            />
            Cash on Delivery
          </label>
          <label className="flex cursor-pointer items-center gap-2 rounded-lg border border-slate-200 px-4 py-3">
            <input
              type="radio"
              name="payment"
              checked={paymentMethod === "ONLINE"}
              onChange={() => setPaymentMethod("ONLINE")}
            />
            Online Payment
          </label>
        </div>
        {paymentMethod === "ONLINE" && (
          <p className="mt-2 text-sm text-slate-600">
            You will confirm payment on the next screen (simulated gateway).
          </p>
        )}
      </div>

      <div className="rounded-xl border border-slate-200 bg-white p-5">
        <div className="flex items-center justify-between">
          <h2 className="text-lg font-semibold">Delivery Address</h2>
          <button
            onClick={() => setShowForm((value) => !value)}
            className="text-sm font-medium text-primary"
          >
            {showForm ? "Cancel" : "Add new address"}
          </button>
        </div>

        {addresses.length > 0 && (
          <div className="mt-4 space-y-3">
            {addresses.map((address) => (
              <label
                key={address.id}
                className={`block cursor-pointer rounded-lg border p-4 ${
                  selectedAddressId === address.id
                    ? "border-primary bg-teal-50"
                    : "border-slate-200"
                }`}
              >
                <input
                  type="radio"
                  name="address"
                  checked={selectedAddressId === address.id}
                  onChange={() => setSelectedAddressId(address.id)}
                  className="mr-2"
                />
                <span className="font-medium">{address.label}</span>
                <p className="mt-1 text-sm text-slate-600">
                  {address.addressLine}, {address.city}, {address.state}{" "}
                  {address.zipCode}, {address.country}
                </p>
              </label>
            ))}
          </div>
        )}

        {showForm && (
          <form onSubmit={handleAddAddress} className="mt-4 grid gap-3">
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
                  onChange={(e) =>
                    setForm((prev) => ({ ...prev, [key]: e.target.value }))
                  }
                  className="w-full rounded-lg border border-slate-300 px-3 py-2"
                />
              </div>
            ))}
            <button
              type="submit"
              className="rounded-lg border border-slate-300 px-4 py-2 font-medium hover:bg-slate-50"
            >
              Save address
            </button>
          </form>
        )}
      </div>

      {error && <p className="text-red-600">{error}</p>}

      <button
        onClick={handleCheckout}
        disabled={placing}
        className="w-full rounded-lg bg-primary px-4 py-3 font-semibold text-white hover:bg-primary-dark"
      >
        {placing ? "Placing order..." : paymentMethod === "COD" ? "Place Order (COD)" : "Place Order & Pay Online"}
      </button>
    </div>
  );
}
