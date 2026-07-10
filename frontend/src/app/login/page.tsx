"use client";

import { login } from "@/lib/api";
import { getDefaultRoute } from "@/lib/auth";
import Link from "next/link";
import { FormEvent, useState } from "react";
import { useRouter } from "next/navigation";
import { AuthCard } from "@/components/ui/AuthCard";
import { Button } from "@/components/ui/Button";

export default function LoginPage() {
  const router = useRouter();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setLoading(true);
    setError("");

    try {
      await login(email, password);
      router.push(getDefaultRoute());
    } catch (err) {
      setError(err instanceof Error ? err.message : "Login failed");
    } finally {
      setLoading(false);
    }
  }

  return (
    <AuthCard
      title="Sign in"
      subtitle="Welcome back to your calm shopping space."
      footer={
        <div className="space-y-2 text-center text-sm text-zen-800/70">
          <p>
            New here?{" "}
            <Link href="/register" className="font-medium text-primary hover:underline">
              Customer account
            </Link>
            {" · "}
            <Link href="/register/seller" className="font-medium text-primary hover:underline">
              Seller account
            </Link>
          </p>
          <p>
            <Link href="/forgot-password" className="text-primary hover:underline">
              Forgot password?
            </Link>
            {" · "}
            <Link href="/resend-activation" className="text-primary hover:underline">
              Resend activation
            </Link>
          </p>
        </div>
      }
    >
      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label className="zenvy-label">Email</label>
          <input
            type="email"
            required
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            className="zenvy-input"
            placeholder="you@example.com"
          />
        </div>
        <div>
          <label className="zenvy-label">Password</label>
          <input
            type="password"
            required
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className="zenvy-input"
            placeholder="••••••••"
          />
        </div>
        {error && (
          <p className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">{error}</p>
        )}
        <Button type="submit" disabled={loading} className="w-full">
          {loading ? "Signing in..." : "Sign in"}
        </Button>
      </form>
    </AuthCard>
  );
}
