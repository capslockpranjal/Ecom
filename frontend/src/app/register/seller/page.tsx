"use client";

import { registerSeller } from "@/lib/api";
import Link from "next/link";
import { FormEvent, useState } from "react";

const fields = [
  { key: "email", label: "Email", type: "email" },
  { key: "password", label: "Password", type: "password" },
  { key: "confirmPassword", label: "Confirm password", type: "password" },
  { key: "firstName", label: "First name", type: "text" },
  { key: "lastName", label: "Last name", type: "text" },
  { key: "gst", label: "GST number", type: "text" },
  { key: "companyName", label: "Company name", type: "text" },
  { key: "companyContact", label: "Company contact (10 digits)", type: "tel" },
  { key: "addressLine", label: "Address line", type: "text" },
  { key: "city", label: "City", type: "text" },
  { key: "state", label: "State", type: "text" },
  { key: "country", label: "Country", type: "text" },
  { key: "zipCode", label: "ZIP code", type: "text" },
] as const;

export default function SellerRegisterPage() {
  const [form, setForm] = useState<Record<string, string>>({
    country: "India",
  });
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setLoading(true);
    setError("");
    setMessage("");
    try {
      const res = await registerSeller(form);
      setMessage(res.message);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Registration failed");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="mx-auto max-w-lg rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
      <h1 className="text-2xl font-bold">Seller Registration</h1>
      <p className="mt-1 text-sm text-slate-600">
        Submit your details. An admin must approve your account before you can sell.
      </p>
      <form onSubmit={handleSubmit} className="mt-6 grid gap-4 sm:grid-cols-2">
        {fields.map(({ key, label, type }) => (
          <div key={key} className={key === "addressLine" ? "sm:col-span-2" : ""}>
            <label className="mb-1 block text-sm font-medium">{label}</label>
            <input
              type={type}
              required
              value={form[key] || ""}
              onChange={(e) => setForm((prev) => ({ ...prev, [key]: e.target.value }))}
              className="w-full rounded-lg border border-slate-300 px-3 py-2"
            />
          </div>
        ))}
        {message && <p className="sm:col-span-2 text-sm text-green-700">{message}</p>}
        {error && <p className="sm:col-span-2 text-sm text-red-600">{error}</p>}
        <button
          type="submit"
          disabled={loading}
          className="sm:col-span-2 rounded-lg bg-primary px-4 py-2.5 font-semibold text-white hover:bg-primary-dark"
        >
          {loading ? "Submitting..." : "Register as seller"}
        </button>
      </form>
      <p className="mt-4 text-sm text-slate-600">
        <Link href="/register" className="text-primary">
          Customer registration
        </Link>
        {" · "}
        <Link href="/login" className="text-primary">
          Login
        </Link>
      </p>
    </div>
  );
}
