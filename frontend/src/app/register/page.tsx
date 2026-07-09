"use client";

import { registerCustomer } from "@/lib/api";
import Link from "next/link";
import { FormEvent, useState } from "react";

export default function RegisterPage() {
  const [form, setForm] = useState({
    email: "",
    password: "",
    confirmPassword: "",
    firstName: "",
    lastName: "",
    contact: "",
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
      const response = await registerCustomer(form);
      setMessage(response.message);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Registration failed");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="mx-auto max-w-lg rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
      <h1 className="text-2xl font-bold">Create Customer Account</h1>
      <p className="mt-1 text-sm text-slate-600">
        Register to shop on Zenvy. You will receive an activation email.
      </p>

      <form onSubmit={handleSubmit} className="mt-6 grid gap-4 md:grid-cols-2">
        {[
          ["firstName", "First name"],
          ["lastName", "Last name"],
          ["email", "Email", "email"],
          ["contact", "Contact (10 digits"],
          ["password", "Password", "password"],
          ["confirmPassword", "Confirm password", "password"],
        ].map(([key, label, type = "text"]) => (
          <div key={key} className={key === "email" ? "md:col-span-2" : ""}>
            <label className="mb-1 block text-sm font-medium">{label}</label>
            <input
              type={type}
              required
              value={form[key as keyof typeof form]}
              onChange={(e) =>
                setForm((prev) => ({ ...prev, [key]: e.target.value }))
              }
              className="w-full rounded-lg border border-slate-300 px-3 py-2"
            />
          </div>
        ))}

        {error && (
          <p className="md:col-span-2 text-sm text-red-600">{error}</p>
        )}
        {message && (
          <p className="md:col-span-2 text-sm text-green-700">{message}</p>
        )}

        <button
          type="submit"
          disabled={loading}
          className="md:col-span-2 rounded-lg bg-primary px-4 py-2.5 font-semibold text-white hover:bg-primary-dark"
        >
          {loading ? "Creating account..." : "Register"}
        </button>
      </form>

      <p className="mt-4 text-sm text-slate-600">
        Already registered?{" "}
        <Link href="/login" className="font-medium text-primary">
          Login
        </Link>
        {" · "}
        <Link href="/register/seller" className="font-medium text-primary">
          Register as seller
        </Link>
      </p>
    </div>
  );
}
