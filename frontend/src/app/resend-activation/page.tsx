"use client";

import { resendActivation } from "@/lib/api";
import Link from "next/link";
import { FormEvent, useState } from "react";

export default function ResendActivationPage() {
  const [email, setEmail] = useState("");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setLoading(true);
    setError("");
    setMessage("");
    try {
      const res = await resendActivation(email);
      setMessage(res.message);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Request failed");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="mx-auto max-w-md rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
      <h1 className="text-2xl font-bold">Resend Activation</h1>
      <p className="mt-1 text-sm text-slate-600">
        Did not receive your activation email? Request a new one.
      </p>
      <form onSubmit={handleSubmit} className="mt-6 space-y-4">
        <div>
          <label className="mb-1 block text-sm font-medium">Email</label>
          <input
            type="email"
            required
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            className="w-full rounded-lg border border-slate-300 px-3 py-2"
          />
        </div>
        {message && <p className="text-sm text-green-700">{message}</p>}
        {error && <p className="text-sm text-red-600">{error}</p>}
        <button
          type="submit"
          disabled={loading}
          className="w-full rounded-lg bg-primary px-4 py-2.5 font-semibold text-white hover:bg-primary-dark"
        >
          {loading ? "Sending..." : "Resend activation email"}
        </button>
      </form>
      <p className="mt-4 text-sm">
        <Link href="/login" className="text-primary">
          Back to login
        </Link>
      </p>
    </div>
  );
}
