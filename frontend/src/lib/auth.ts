export function getAccessToken(): string | null {
  if (typeof window === "undefined") return null;
  return localStorage.getItem("accessToken");
}

export function getRoles(): string[] {
  const token = getAccessToken();
  if (!token) return [];

  try {
    const payload = token.split(".")[1];
    const decoded = JSON.parse(atob(payload.replace(/-/g, "+").replace(/_/g, "/")));
    return Array.isArray(decoded.roles) ? decoded.roles : [];
  } catch {
    return [];
  }
}

export function hasRole(role: string): boolean {
  return getRoles().includes(role);
}

export function isCustomer(): boolean {
  return hasRole("ROLE_CUSTOMER");
}

export function isSeller(): boolean {
  return hasRole("ROLE_SELLER");
}

export function isAdmin(): boolean {
  return hasRole("ROLE_ADMIN");
}

export function getDefaultRoute(): string {
  if (isAdmin()) return "/admin/orders";
  if (isSeller()) return "/seller/orders";
  if (isCustomer()) return "/shop";
  return "/login";
}
