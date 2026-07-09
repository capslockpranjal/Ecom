"use client";

import {
  changePassword,
  getProfile,
  isLoggedIn,
  updateProfile,
  CustomerProfile,
} from "@/lib/api";
import { isCustomer } from "@/lib/auth";
import { useRouter } from "next/navigation";
import { FormEvent, useEffect, useState } from "react";

export default function ProfilePage() {
  const router = useRouter();
  const [profile, setProfile] = useState<CustomerProfile | null>(null);
  const [form, setForm] = useState({ firstName: "", lastName: "", contact: "" });
  const [passwordForm, setPasswordForm] = useState({
    oldPassword: "",
    newPassword: "",
    confirmPassword: "",
  });
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (!isLoggedIn()) {
      router.push("/login");
      return;
    }
    if (!isCustomer()) {
      router.push("/");
      return;
    }

    getProfile()
      .then((data) => {
        setProfile(data);
        setForm({
          firstName: data.firstName,
          lastName: data.lastName,
          contact: data.contact,
        });
      })
      .catch((err) => setError(err instanceof Error ? err.message : "Failed to load profile"))
      .finally(() => setLoading(false));
  }, [router]);

  async function handleProfileSubmit(event: FormEvent) {
    event.preventDefault();
    setSaving(true);
    setError("");
    setMessage("");
    try {
      const updated = await updateProfile(form);
      setProfile(updated);
      setMessage("Profile updated");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Update failed");
    } finally {
      setSaving(false);
    }
  }

  async function handlePasswordSubmit(event: FormEvent) {
    event.preventDefault();
    setSaving(true);
    setError("");
    setMessage("");
    try {
      const res = await changePassword(
        passwordForm.oldPassword,
        passwordForm.newPassword,
        passwordForm.confirmPassword
      );
      setMessage(res.message);
      setPasswordForm({ oldPassword: "", newPassword: "", confirmPassword: "" });
    } catch (err) {
      setError(err instanceof Error ? err.message : "Password change failed");
    } finally {
      setSaving(false);
    }
  }

  if (loading) return <p>Loading profile...</p>;
  if (!profile) return <p className="text-red-600">{error || "Profile not found"}</p>;

  return (
    <div className="mx-auto max-w-2xl space-y-6">
      <h1 className="text-3xl font-bold">My Profile</h1>

      <div className="rounded-xl border border-slate-200 bg-white p-5">
        <p className="text-sm text-slate-600">Email: {profile.email}</p>
        <form onSubmit={handleProfileSubmit} className="mt-4 grid gap-4 sm:grid-cols-2">
          {(["firstName", "lastName", "contact"] as const).map((key) => (
            <div key={key}>
              <label className="mb-1 block text-sm font-medium capitalize">{key}</label>
              <input
                required={key !== "contact"}
                value={form[key]}
                onChange={(e) => setForm((prev) => ({ ...prev, [key]: e.target.value }))}
                className="w-full rounded-lg border border-slate-300 px-3 py-2"
              />
            </div>
          ))}
          <button
            type="submit"
            disabled={saving}
            className="rounded-lg bg-primary px-4 py-2 font-semibold text-white hover:bg-primary-dark sm:col-span-2"
          >
            Save profile
          </button>
        </form>
      </div>

      <div className="rounded-xl border border-slate-200 bg-white p-5">
        <h2 className="text-lg font-semibold">Change password</h2>
        <form onSubmit={handlePasswordSubmit} className="mt-4 space-y-4">
          {(["oldPassword", "newPassword", "confirmPassword"] as const).map((key) => (
            <div key={key}>
              <label className="mb-1 block text-sm font-medium">
                {key === "oldPassword" ? "Current password" : key === "newPassword" ? "New password" : "Confirm password"}
              </label>
              <input
                type="password"
                required
                value={passwordForm[key]}
                onChange={(e) =>
                  setPasswordForm((prev) => ({ ...prev, [key]: e.target.value }))
                }
                className="w-full rounded-lg border border-slate-300 px-3 py-2"
              />
            </div>
          ))}
          <button
            type="submit"
            disabled={saving}
            className="rounded-lg border border-slate-300 px-4 py-2 font-medium hover:bg-slate-50"
          >
            Update password
          </button>
        </form>
      </div>

      {message && <p className="text-green-700">{message}</p>}
      {error && <p className="text-red-600">{error}</p>}
    </div>
  );
}
