"use client";

import { registerCustomer } from "@/lib/api";
import Link from "next/link";
import { FormEvent, useState } from "react";
import { AuthCard } from "@/components/ui/AuthCard";
import { Button } from "@/components/ui/Button";

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
    <AuthCard
      title="Join Zenvy"
      subtitle="Create your account and start discovering curated products."
      footer={
        <p className="text-center text-sm text-zen-800/70">
          Already registered?{" "}
          <Link href="/login" className="font-medium text-primary hover:underline">
            Sign in
          </Link>
          {" · "}
          <Link href="/register/seller" className="font-medium text-primary hover:underline">
            Register as seller
          </Link>
        </p>
      }
    >
      <form onSubmit={handleSubmit} className="grid gap-4 md:grid-cols-2">
        {[
          ["firstName", "First name"],
          ["lastName", "Last name"],
          ["email", "Email", "email"],
          ["contact", "Contact (10 digits)"],
          ["password", "Password", "password"],
          ["confirmPassword", "Confirm password", "password"],
        ].map(([key, label, type = "text"]) => (
          <div key={key} className={key === "email" ? "md:col-span-2" : ""}>
            <label className="zenvy-label">{label}</label>
            <input
              type={type}
              required
              value={form[key as keyof typeof form]}
              onChange={(e) =>
                setForm((prev) => ({ ...prev, [key]: e.target.value }))
              }
              className="zenvy-input"
            />
          </div>
        ))}

        {error && (
          <p className="md:col-span-2 rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">
            {error}
          </p>
        )}
        {message && (
          <p className="md:col-span-2 rounded-lg bg-primary/10 px-3 py-2 text-sm text-primary-dark">
            {message}
          </p>
        )}

        <Button type="submit" disabled={loading} className="md:col-span-2 w-full">
          {loading ? "Creating account..." : "Create account"}
        </Button>
      </form>
    </AuthCard>
  );
}
