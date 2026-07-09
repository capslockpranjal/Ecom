"use client";

import { getSellerReturns, isLoggedIn, ReturnRequest, updateReturnStatus } from "@/lib/api";
import { isSeller } from "@/lib/auth";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";

export default function SellerReturnsPage() {
  const router = useRouter();
  const [returns, setReturns] = useState<ReturnRequest[]>([]);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  async function load() {
    const data = await getSellerReturns();
    setReturns(data);
  }

  useEffect(() => {
    if (!isLoggedIn()) {
      router.push("/login");
      return;
    }
    if (!isSeller()) {
      router.push("/");
      return;
    }

    load()
      .catch((err) => setError(err instanceof Error ? err.message : "Failed to load"))
      .finally(() => setLoading(false));
  }, [router]);

  async function handleStatus(returnId: string, status: "APPROVED" | "REJECTED") {
    try {
      await updateReturnStatus(returnId, status);
      await load();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Update failed");
    }
  }

  if (loading) return <p>Loading returns...</p>;

  return (
    <div className="space-y-6">
      <h1 className="text-3xl font-bold">Return Requests</h1>
      {error && <p className="text-red-600">{error}</p>}

      {returns.length === 0 ? (
        <p className="text-slate-600">No return requests.</p>
      ) : (
        <div className="overflow-hidden rounded-xl border border-slate-200 bg-white">
          <table className="w-full text-left text-sm">
            <thead className="border-b border-slate-200 bg-slate-50">
              <tr>
                <th className="px-4 py-3">Product</th>
                <th className="px-4 py-3">Qty</th>
                <th className="px-4 py-3">Status</th>
                <th className="px-4 py-3">Action</th>
              </tr>
            </thead>
            <tbody>
              {returns.map((item) => (
                <tr key={item.id} className="border-b border-slate-100">
                  <td className="px-4 py-3">
                    <p className="font-medium">{item.productName}</p>
                    {item.reason && (
                      <p className="text-xs text-slate-500">{item.reason}</p>
                    )}
                  </td>
                  <td className="px-4 py-3">{item.quantity}</td>
                  <td className="px-4 py-3">{item.status}</td>
                  <td className="px-4 py-3">
                    {item.status === "REQUESTED" && (
                      <div className="flex gap-2">
                        <button
                          onClick={() => handleStatus(item.id, "APPROVED")}
                          className="text-green-700 hover:underline"
                        >
                          Approve
                        </button>
                        <button
                          onClick={() => handleStatus(item.id, "REJECTED")}
                          className="text-red-600 hover:underline"
                        >
                          Reject
                        </button>
                      </div>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
