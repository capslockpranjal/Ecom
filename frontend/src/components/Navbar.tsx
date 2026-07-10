"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { clearTokens, isLoggedIn } from "@/lib/api";
import { getRoles, isAdmin, isCustomer, isSeller } from "@/lib/auth";
import { Logo } from "@/components/Logo";
import { useEffect, useState } from "react";

function NavLink({ href, children }: { href: string; children: React.ReactNode }) {
  const pathname = usePathname();
  const active = pathname === href || pathname.startsWith(`${href}/`);

  return (
    <Link
      href={href}
      className={active ? "zenvy-nav-link zenvy-nav-link-active" : "zenvy-nav-link"}
    >
      {children}
    </Link>
  );
}

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
    <header className="sticky top-0 z-50 border-b border-zen-200/80 bg-white/90 backdrop-blur-md">
      <div className="mx-auto flex max-w-6xl items-center justify-between gap-4 px-4 py-3.5 md:px-6">
        <Logo showTagline />

        <nav className="flex flex-wrap items-center justify-end gap-1 text-sm">
          {loggedIn ? (
            <>
              {isCustomer() && (
                <>
                  <NavLink href="/shop">Shop</NavLink>
                  <NavLink href="/cart">Cart</NavLink>
                  <NavLink href="/orders">Orders</NavLink>
                  <NavLink href="/returns">Returns</NavLink>
                  <NavLink href="/profile">Profile</NavLink>
                  <NavLink href="/addresses">Addresses</NavLink>
                </>
              )}
              {isSeller() && (
                <>
                  <NavLink href="/seller/products">Products</NavLink>
                  <NavLink href="/seller/orders">Orders</NavLink>
                  <NavLink href="/seller/returns">Returns</NavLink>
                </>
              )}
              {isAdmin() && (
                <>
                  <NavLink href="/admin/customers">Customers</NavLink>
                  <NavLink href="/admin/sellers">Sellers</NavLink>
                  <NavLink href="/admin/products">Products</NavLink>
                  <NavLink href="/admin/categories">Categories</NavLink>
                  <NavLink href="/admin/orders">Orders</NavLink>
                </>
              )}
              {roles.length > 0 && (
                <span className="hidden rounded-full bg-zen-100 px-2.5 py-1 text-[11px] font-medium uppercase tracking-wide text-zen-800/60 lg:inline">
                  {roles.map((r) => r.replace("ROLE_", "")).join(" · ")}
                </span>
              )}
              <button
                onClick={logout}
                className="zenvy-nav-link ml-1 border border-zen-200"
              >
                Logout
              </button>
            </>
          ) : (
            <>
              <NavLink href="/login">Login</NavLink>
              <Link
                href="/register"
                className="ml-1 rounded-xl bg-primary px-4 py-2 text-sm font-semibold text-white shadow-zen transition hover:bg-primary-dark"
              >
                Join Zenvy
              </Link>
            </>
          )}
        </nav>
      </div>
    </header>
  );
}
