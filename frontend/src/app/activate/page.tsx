"use client";

import { activateAccount } from "@/lib/api";
import Link from "next/link";
import { useSearchParams } from "next/navigation";
import { Suspense, useEffect, useState } from "react";

function ActivateContent() {
  const searchParams = useSearchParams();
  const token = searchParams.get("token") || "";
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(!!token);

  useEffect(() => {
    if (!token) return;

    activateAccount(token)
      .then((res) => setMessage(res.message))
      .catch((err) => setError(err instanceof Error ? err.message : "Activation failed"))
      .finally(() => setLoading(false));
  }, [token]);

  if (!token) {
    return (
      <div className="mx-auto max-w-md rounded-xl border border-slate-200 bg-white p-6">
        <h1 className="text-2xl font-bold">Account Activation</h1>
        <p className="mt-2 text-red-600">Missing activation token.</p>
        <Link href="/login" className="mt-4 inline-block text-primary">
          Go to login
        </Link>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-md rounded-xl border border-slate-200 bg-white p-6">
      <h1 className="text-2xl font-bold">Account Activation</h1>
      {loading && <p className="mt-4 text-slate-600">Activating your account...</p>}
      {message && <p className="mt-4 text-green-700">{message}</p>}
      {error && <p className="mt-4 text-red-600">{error}</p>}
      {!loading && (
        <Link href="/login" className="mt-4 inline-block font-medium text-primary">
          Go to login
        </Link>
      )}
    </div>
  );
}

export default function ActivatePage() {
  return (
    <Suspense fallback={<p>Loading...</p>}>
      <ActivateContent />
    </Suspense>
  );
}
