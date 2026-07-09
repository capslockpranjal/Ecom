"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { clearTokens, isLoggedIn } from "@/lib/api";
import { getRoles, isAdmin, isCustomer, isSeller } from "@/lib/auth";
import { useEffect, useState } from "react";

export function Navbar() {
  const pathname = usePathname();
  const [loggedIn, setLoggedIn] = useState(false);
  const [roles, setRoles] = useState<string[]>([]);

  useEffect(() => {
    setLoggedIn(isLoggedIn());
    setRoles(getRoles());
  }, [pathname]);

  function logout() {
    clearTokens();
    setLoggedIn(false);
    window.location.href = "/login";
  }

  return (
    <header className="border-b border-slate-200 bg-white">
      <div className="mx-auto flex max-w-6xl items-center justify-between px-4 py-4">
        <Link href="/" className="text-xl font-bold text-primary">
          Zenvy
        </Link>
        <nav className="flex flex-wrap items-center justify-end gap-3 text-sm font-medium">
          {loggedIn ? (
            <>
              {isCustomer() && (
                <>
                  <Link href="/shop" className="hover:text-primary">
                    Shop
                  </Link>
                  <Link href="/cart" className="hover:text-primary">
                    Cart
                  </Link>
                  <Link href="/orders" className="hover:text-primary">
                    Orders
                  </Link>
                  <Link href="/returns" className="hover:text-primary">
                    Returns
                  </Link>
                  <Link href="/profile" className="hover:text-primary">
                    Profile
                  </Link>
                  <Link href="/addresses" className="hover:text-primary">
                    Addresses
                  </Link>
                </>
              )}
              {isSeller() && (
                <>
                  <Link href="/seller/products" className="hover:text-primary">
                    Products
                  </Link>
                  <Link href="/seller/orders" className="hover:text-primary">
                    Orders
                  </Link>
                  <Link href="/seller/returns" className="hover:text-primary">
                    Returns
                  </Link>
                </>
              )}
              {isAdmin() && (
                <>
                  <Link href="/admin/customers" className="hover:text-primary">
                    Customers
                  </Link>
                  <Link href="/admin/sellers" className="hover:text-primary">
                    Sellers
                  </Link>
                  <Link href="/admin/products" className="hover:text-primary">
                    Products
                  </Link>
                  <Link href="/admin/categories" className="hover:text-primary">
                    Categories
                  </Link>
                  <Link href="/admin/orders" className="hover:text-primary">
                    Orders
                  </Link>
                </>
              )}
              {roles.length > 0 && (
                <span className="hidden text-xs text-slate-500 sm:inline">
                  {roles.map((r) => r.replace("ROLE_", "")).join(" · ")}
                </span>
              )}
              <button
                onClick={logout}
                className="rounded-lg border border-slate-300 px-3 py-1.5 hover:bg-slate-50"
              >
                Logout
              </button>
            </>
          ) : (
            <>
              <Link href="/login" className="hover:text-primary">
                Login
              </Link>
              <Link
                href="/register"
                className="rounded-lg bg-primary px-3 py-1.5 text-white hover:bg-primary-dark"
              >
                Register
              </Link>
            </>
          )}
        </nav>
      </div>
    </header>
  );
}
