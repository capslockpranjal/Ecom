"use client";

import { getReturns, isLoggedIn, ReturnRequest } from "@/lib/api";
import { isCustomer } from "@/lib/auth";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";

export default function ReturnsPage() {
  const router = useRouter();
  const [returns, setReturns] = useState<ReturnRequest[]>([]);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!isLoggedIn()) {
      router.push("/login");
      return;
    }
    if (!isCustomer()) {
      router.push("/");
      return;
    }

    getReturns()
      .then(setReturns)
      .catch((err) => setError(err instanceof Error ? err.message : "Failed to load returns"))
      .finally(() => setLoading(false));
  }, [router]);

  if (loading) return <p>Loading returns...</p>;

  return (
    <div className="space-y-6">
      <h1 className="text-3xl font-bold">My Returns</h1>
      {error && <p className="text-red-600">{error}</p>}

      {returns.length === 0 ? (
        <p className="text-slate-600">No return requests yet.</p>
      ) : (
        <div className="space-y-3">
          {returns.map((item) => (
            <div
              key={item.id}
              className="rounded-xl border border-slate-200 bg-white p-4"
            >
              <div className="flex flex-wrap items-start justify-between gap-3">
                <div>
                  <p className="font-semibold">{item.productName}</p>
                  <p className="text-sm text-slate-600">
                    {item.sellerCompanyName} · Qty {item.quantity}
                  </p>
                  <p className="text-sm text-slate-600">
                    {Object.entries(item.metadata)
                      .map(([k, v]) => `${k}: ${v}`)
                      .join(", ")}
                  </p>
                  {item.reason && (
                    <p className="mt-1 text-sm text-slate-500">Reason: {item.reason}</p>
                  )}
                </div>
                <div className="text-right text-sm">
                  <p className="font-medium">{item.status}</p>
                  <p className="text-slate-600">
                    {new Date(item.createdAt).toLocaleString()}
                  </p>
                  <Link href={`/orders/${item.orderId}`} className="text-primary">
                    View order
                  </Link>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
