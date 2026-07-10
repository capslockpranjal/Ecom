export const API_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

export type ApiResponse<T> = {
  success: boolean;
  message: string;
  data: T;
  timestamp?: string;
};

export type PagedResponse<T> = {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
};

export type SpringPage<T> = {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  last: boolean;
};

export type AuthTokens = {
  accessToken: string;
  refreshToken: string;
};

export type Category = {
  id: string;
  name: string;
  metadataFields?: MetadataField[];
  brands?: string[];
  minPrice?: number;
  maxPrice?: number;
};

export type MetadataField = {
  fieldId: string;
  name: string;
  values: string[];
};

export type FilteringData = {
  metadata: MetadataField[];
  brands: string[];
  minPrice: number;
  maxPrice: number;
};

export type ProductVariation = {
  id: string;
  quantityAvailable: number;
  price: number;
  isActive: boolean;
  metadata: Record<string, string>;
  primaryImage?: string;
  secondaryImages?: string[];
};

export type ProductListItem = {
  id: string;
  name: string;
  description: string;
  brand: string;
  isCancellable: boolean;
  isReturnable: boolean;
  category: { id: string; name: string };
  primaryImages: string[];
};

export type SellerProduct = {
  id: string;
  name: string;
  description: string;
  brand: string;
  isActive: boolean;
  isCancellable: boolean;
  isReturnable: boolean;
  categoryId: string;
  categoryName: string;
};

export type ProductDetail = {
  id: string;
  name: string;
  description: string;
  brand: string;
  isCancellable: boolean;
  isReturnable: boolean;
  category: { id: string; name: string };
  variations: ProductVariation[];
};

export type CartItem = {
  id: string;
  variationId: string;
  productId: string;
  productName: string;
  brand: string;
  metadata: Record<string, string>;
  unitPrice: number;
  quantity: number;
  lineTotal: number;
  quantityAvailable: number;
  primaryImage?: string;
};

export type Cart = {
  id: string;
  items: CartItem[];
  subtotal: number;
  itemCount: number;
};

export type Address = {
  id: string;
  city: string;
  state: string;
  country: string;
  addressLine: string;
  zipCode: string;
  label: string;
};

export type CustomerProfile = {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  contact: string;
  profileImage?: string;
};

export type OrderItem = {
  id: string;
  productVariationId: string;
  productId: string;
  productName: string;
  brand: string;
  metadata: Record<string, string>;
  quantity: number;
  unitPrice: number;
  lineTotal: number;
  isCancellable?: boolean;
  isReturnable?: boolean;
};

export type SellerOrder = {
  id: string;
  sellerId: string;
  sellerCompanyName: string;
  status: string;
  subtotal: number;
  items: OrderItem[];
};

export type Order = {
  id: string;
  status: string;
  paymentStatus: string;
  paymentMethod: string;
  totalAmount: number;
  addressLine: string;
  city: string;
  state: string;
  country: string;
  zipCode: string;
  label: string;
  createdAt: string;
  cancellable?: boolean;
  sellerOrders: SellerOrder[];
};

export type OrderSummary = {
  id: string;
  status: string;
  paymentStatus: string;
  paymentMethod: string;
  totalAmount: number;
  createdAt: string;
  sellerOrderCount: number;
};

export type PaymentSession = {
  orderId: string;
  razorpayOrderId: string;
  razorpayKeyId: string;
  amount: number;
  currency: string;
  paymentStatus: string;
  paymentMethod: string;
  requiresPayment: boolean;
  message: string;
};

export type ReturnRequest = {
  id: string;
  orderItemId: string;
  orderId: string;
  sellerOrderId: string;
  productName: string;
  brand: string;
  metadata: Record<string, string>;
  quantity: number;
  reason?: string;
  status: string;
  sellerCompanyName: string;
  createdAt: string;
};

export type ProductFilters = {
  brand?: string;
  minPrice?: number;
  maxPrice?: number;
  metadata?: Record<string, string>;
};

export type SellerOrderDetail = {
  sellerOrderId: string;
  orderId: string;
  sellerOrder: SellerOrder;
  paymentStatus: string;
  paymentMethod: string;
  addressLine: string;
  city: string;
  state: string;
  country: string;
  zipCode: string;
  label: string;
  orderCreatedAt: string;
};

export type AdminCustomer = {
  id: string;
  fullName: string;
  email: string;
  isActive: boolean;
};

export type AdminSeller = {
  id: string;
  fullName: string;
  email: string;
  isActive: boolean;
  companyName: string;
  companyAddress: string;
  companyContact: string;
};

export type AdminProduct = {
  id: string;
  name: string;
  description: string;
  brand: string;
  isCancellable: boolean;
  isReturnable: boolean;
  isActive: boolean;
  sellerId: string;
  category: { id: string; name: string };
  primaryImages: string[];
};

export type CategoryTree = {
  id: string;
  name: string;
  parentChain: { id: string; name: string }[];
  children: { id: string; name: string }[];
  metadataFields: MetadataField[];
};

function getAccessToken(): string | null {
  if (typeof window === "undefined") return null;
  return localStorage.getItem("accessToken");
}

function getRefreshToken(): string | null {
  if (typeof window === "undefined") return null;
  return localStorage.getItem("refreshToken");
}

export function setTokens(tokens: AuthTokens) {
  localStorage.setItem("accessToken", tokens.accessToken);
  localStorage.setItem("refreshToken", tokens.refreshToken);
}

export function clearTokens() {
  localStorage.removeItem("accessToken");
  localStorage.removeItem("refreshToken");
}

export function isLoggedIn(): boolean {
  return !!getAccessToken();
}

let refreshPromise: Promise<boolean> | null = null;

async function refreshAccessToken(): Promise<boolean> {
  const refreshToken = getRefreshToken();
  if (!refreshToken) return false;

  if (!refreshPromise) {
    refreshPromise = (async () => {
      try {
        const response = await fetch(`${API_URL}/auth/refresh`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ refreshToken }),
        });
        if (!response.ok) return false;
        const body: ApiResponse<AuthTokens> = await response.json();
        setTokens(body.data);
        return true;
      } catch {
        return false;
      } finally {
        refreshPromise = null;
      }
    })();
  }

  return refreshPromise;
}

async function apiFetch<T>(
  path: string,
  options: RequestInit = {},
  auth = true,
  retry = true
): Promise<T> {
  const headers = new Headers(options.headers || {});
  if (!headers.has("Content-Type") && options.body && !(options.body instanceof FormData)) {
    headers.set("Content-Type", "application/json");
  }

  if (auth) {
    const token = getAccessToken();
    if (token) {
      headers.set("Authorization", `Bearer ${token}`);
    }
  }

  const response = await fetch(`${API_URL}${path}`, {
    ...options,
    headers,
  });

  if (response.status === 401 && auth && retry) {
    const refreshed = await refreshAccessToken();
    if (refreshed) {
      return apiFetch<T>(path, options, auth, false);
    }
    clearTokens();
  }

  if (!response.ok) {
    let message = "Request failed";
    try {
      const error = await response.json();
      message = error.message || message;
    } catch {
      // ignore
    }
    throw new Error(message);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return response.json();
}

// ── Auth ──────────────────────────────────────────────────────────────────────

export async function login(email: string, password: string) {
  const response = await apiFetch<ApiResponse<AuthTokens>>(
    "/auth/login",
    { method: "POST", body: JSON.stringify({ email, password }) },
    false
  );
  setTokens(response.data);
  return response;
}

export async function registerCustomer(payload: {
  email: string;
  password: string;
  confirmPassword: string;
  firstName: string;
  lastName: string;
  contact: string;
}) {
  return apiFetch<ApiResponse<string>>(
    "/auth/register/customer",
    { method: "POST", body: JSON.stringify(payload) },
    false
  );
}

export async function registerSeller(payload: Record<string, string>) {
  return apiFetch<ApiResponse<string>>(
    "/auth/register/seller",
    { method: "POST", body: JSON.stringify(payload) },
    false
  );
}

export async function activateAccount(token: string) {
  return apiFetch<ApiResponse<string>>(
    `/auth/activate?token=${encodeURIComponent(token)}`,
    { method: "PUT" },
    false
  );
}

export async function forgotPassword(email: string) {
  return apiFetch<ApiResponse<string>>(
    "/auth/forgot-password",
    { method: "POST", body: JSON.stringify({ email }) },
    false
  );
}

export async function resetPassword(token: string, password: string, confirmPassword: string) {
  return apiFetch<ApiResponse<string>>(
    "/auth/reset-password",
    { method: "PUT", body: JSON.stringify({ token, password, confirmPassword }) },
    false
  );
}

export async function resendActivation(email: string) {
  return apiFetch<ApiResponse<string>>(
    "/auth/resend-activation",
    { method: "POST", body: JSON.stringify({ email }) },
    false
  );
}

// ── Customer profile ──────────────────────────────────────────────────────────

export async function getProfile() {
  const response = await apiFetch<ApiResponse<CustomerProfile>>("/customer/profile");
  return response.data;
}

export async function updateProfile(payload: {
  firstName?: string;
  lastName?: string;
  contact?: string;
}) {
  const response = await apiFetch<ApiResponse<CustomerProfile>>("/customer/profile", {
    method: "PATCH",
    body: JSON.stringify(payload),
  });
  return response.data;
}

export async function changePassword(oldPassword: string, newPassword: string, confirmPassword: string) {
  return apiFetch<ApiResponse<string>>("/customer/password", {
    method: "PATCH",
    body: JSON.stringify({ oldPassword, newPassword, confirmPassword }),
  });
}

// ── Categories & products ─────────────────────────────────────────────────────

export async function getCategories(categoryId?: string) {
  const query = categoryId ? `?categoryId=${categoryId}` : "";
  const response = await apiFetch<ApiResponse<Category[]>>(`/categories/customer${query}`);
  return response.data;
}

export async function getCategoryFilters(categoryId: string) {
  const response = await apiFetch<ApiResponse<FilteringData>>(
    `/categories/customer/${categoryId}/filters`
  );
  return response.data;
}

export async function getProducts(
  categoryId: string,
  opts: {
    max?: number;
    offset?: number;
    sort?: string;
    order?: string;
    filters?: ProductFilters;
  } = {}
) {
  const params = new URLSearchParams({ categoryId });
  if (opts.max !== undefined) params.set("max", String(opts.max));
  if (opts.offset !== undefined) params.set("offset", String(opts.offset));
  if (opts.sort) params.set("sort", opts.sort);
  if (opts.order) params.set("order", opts.order);
  if (opts.filters?.brand) params.set("brand", opts.filters.brand);
  if (opts.filters?.minPrice !== undefined) params.set("minPrice", String(opts.filters.minPrice));
  if (opts.filters?.maxPrice !== undefined) params.set("maxPrice", String(opts.filters.maxPrice));
  if (opts.filters?.metadata) {
    Object.entries(opts.filters.metadata).forEach(([key, value]) => {
      if (value) params.set(`metadata.${key}`, value);
    });
  }

  const response = await apiFetch<ApiResponse<SpringPage<ProductListItem>>>(
    `/customer/products?${params}`
  );
  return response.data;
}

export async function getProduct(productId: string) {
  const response = await apiFetch<ApiResponse<ProductDetail>>(`/customer/products/${productId}`);
  return response.data;
}

// ── Cart ──────────────────────────────────────────────────────────────────────

export async function getCart() {
  const response = await apiFetch<ApiResponse<Cart>>("/customer/cart");
  return response.data;
}

export async function addToCart(variationId: string, quantity: number) {
  const response = await apiFetch<ApiResponse<Cart>>("/customer/cart/items", {
    method: "POST",
    body: JSON.stringify({ variationId, quantity }),
  });
  return response.data;
}

export async function updateCartItem(itemId: string, quantity: number) {
  const response = await apiFetch<ApiResponse<Cart>>(`/customer/cart/items/${itemId}`, {
    method: "PATCH",
    body: JSON.stringify({ quantity }),
  });
  return response.data;
}

export async function removeCartItem(itemId: string) {
  const response = await apiFetch<ApiResponse<Cart>>(`/customer/cart/items/${itemId}`, {
    method: "DELETE",
  });
  return response.data;
}

// ── Addresses ─────────────────────────────────────────────────────────────────

export async function getAddresses() {
  const response = await apiFetch<ApiResponse<Address[]>>("/customer/address");
  return response.data;
}

export async function addAddress(payload: Omit<Address, "id">) {
  return apiFetch<ApiResponse<string>>("/customer/address", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export async function updateAddress(id: string, payload: Partial<Omit<Address, "id">>) {
  const response = await apiFetch<ApiResponse<Address>>(`/customer/address/${id}`, {
    method: "PATCH",
    body: JSON.stringify(payload),
  });
  return response.data;
}

export async function deleteAddress(id: string) {
  return apiFetch<ApiResponse<string>>(`/customer/address/${id}`, { method: "DELETE" });
}

// ── Orders ────────────────────────────────────────────────────────────────────

export async function checkout(addressId: string, paymentMethod: "COD" | "ONLINE" = "COD") {
  const response = await apiFetch<ApiResponse<Order>>("/customer/orders/checkout", {
    method: "POST",
    body: JSON.stringify({ addressId, paymentMethod }),
  });
  return response.data;
}

export async function cancelOrder(orderId: string) {
  return apiFetch<ApiResponse<string>>(`/customer/orders/${orderId}/cancel`, { method: "POST" });
}

export async function getPaymentSession(orderId: string) {
  const response = await apiFetch<ApiResponse<PaymentSession>>(
    `/customer/orders/${orderId}/payment-session`
  );
  return response.data;
}

export async function verifyPayment(
  orderId: string,
  payload: {
    razorpayOrderId: string;
    razorpayPaymentId: string;
    razorpaySignature: string;
  }
) {
  const response = await apiFetch<ApiResponse<Order>>(
    `/customer/orders/${orderId}/verify-payment`,
    { method: "POST", body: JSON.stringify(payload) }
  );
  return response.data;
}

export async function createReturn(orderItemId: string, reason?: string) {
  const response = await apiFetch<ApiResponse<ReturnRequest>>("/customer/returns", {
    method: "POST",
    body: JSON.stringify({ orderItemId, reason }),
  });
  return response.data;
}

export async function getReturns() {
  const response = await apiFetch<PagedResponse<ReturnRequest>>(
    "/customer/returns?pageOffset=0&pageSize=50"
  );
  return response.content;
}

export async function getSellerReturns() {
  const response = await apiFetch<PagedResponse<ReturnRequest>>(
    "/seller/returns?pageOffset=0&pageSize=50"
  );
  return response.content;
}

export async function updateReturnStatus(returnId: string, status: "APPROVED" | "REJECTED") {
  const response = await apiFetch<ApiResponse<ReturnRequest>>(
    `/seller/returns/${returnId}/status`,
    { method: "PATCH", body: JSON.stringify({ status }) }
  );
  return response.data;
}

export async function getOrders(pageOffset = 0, pageSize = 20) {
  const response = await apiFetch<PagedResponse<OrderSummary>>(
    `/customer/orders?pageOffset=${pageOffset}&pageSize=${pageSize}`
  );
  return response;
}

export async function getOrder(orderId: string) {
  const response = await apiFetch<ApiResponse<Order>>(`/customer/orders/${orderId}`);
  return response.data;
}

// ── Seller products ───────────────────────────────────────────────────────────

export async function getSellerCategories() {
  const response = await apiFetch<ApiResponse<SpringPage<CategoryTree>>>("/categories/seller?max=100");
  return response.data.content;
}

export async function getSellerProducts(opts: { productId?: string; max?: number; offset?: number } = {}) {
  const params = new URLSearchParams();
  if (opts.productId) params.set("productId", opts.productId);
  if (opts.max !== undefined) params.set("max", String(opts.max));
  if (opts.offset !== undefined) params.set("offset", String(opts.offset));

  const response = await apiFetch<ApiResponse<SellerProduct[] | SpringPage<SellerProduct>>>(
    `/products?${params}`
  );
  const data = response.data;
  return Array.isArray(data) ? data : data.content;
}

export async function getSellerProductVariations(productId: string) {
  const response = await apiFetch<ApiResponse<ProductVariation[]>>(
    `/products/${productId}/variations`
  );
  return response.data;
}

export async function createProduct(payload: {
  name: string;
  categoryId: string;
  brand: string;
  description?: string;
  isCancellable?: boolean;
  isReturnable?: boolean;
}) {
  const response = await apiFetch<ApiResponse<string>>("/products", {
    method: "POST",
    body: JSON.stringify(payload),
  });
  return response.data;
}

export async function updateProduct(
  productId: string,
  payload: { name?: string; description?: string; isCancellable?: boolean; isReturnable?: boolean }
) {
  return apiFetch<ApiResponse<string>>(`/products/${productId}`, {
    method: "PUT",
    body: JSON.stringify(payload),
  });
}

export async function deleteProduct(productId: string) {
  return apiFetch<ApiResponse<string>>(`/products/${productId}`, { method: "DELETE" });
}

export async function addVariationWithImage(
  data: {
    productId: string;
    quantityAvailable: number;
    price: number;
    metadata: Record<string, string>;
  },
  primaryImage: File
) {
  const form = new FormData();
  form.append("data", new Blob([JSON.stringify(data)], { type: "application/json" }));
  form.append("primaryImage", primaryImage);

  const token = getAccessToken();
  const response = await fetch(`${API_URL}/products/variation`, {
    method: "POST",
    headers: token ? { Authorization: `Bearer ${token}` } : {},
    body: form,
  });

  if (!response.ok) {
    const error = await response.json().catch(() => ({}));
    throw new Error(error.message || "Failed to add variation");
  }
  return response.json() as Promise<ApiResponse<string>>;
}

// ── Seller orders ─────────────────────────────────────────────────────────────

export async function getSellerOrders() {
  const response = await apiFetch<PagedResponse<OrderSummary>>(
    "/seller/orders?pageOffset=0&pageSize=20"
  );
  return response.content;
}

export async function getSellerOrder(sellerOrderId: string) {
  const response = await apiFetch<ApiResponse<SellerOrderDetail>>(
    `/seller/orders/${sellerOrderId}`
  );
  return response.data;
}

export async function updateSellerOrderStatus(
  sellerOrderId: string,
  status: "CONFIRMED" | "SHIPPED" | "DELIVERED" | "CANCELLED"
) {
  return apiFetch<ApiResponse<string>>(`/seller/orders/${sellerOrderId}/status`, {
    method: "PATCH",
    body: JSON.stringify({ status }),
  });
}

// ── Admin ─────────────────────────────────────────────────────────────────────

export async function getAdminCustomers(pageOffset = 0, pageSize = 20, email?: string) {
  const params = new URLSearchParams({
    pageOffset: String(pageOffset),
    pageSize: String(pageSize),
  });
  if (email) params.set("email", email);
  return apiFetch<PagedResponse<AdminCustomer>>(`/admin/customers?${params}`);
}

export async function getAdminSellers(pageOffset = 0, pageSize = 20, email?: string) {
  const params = new URLSearchParams({
    pageOffset: String(pageOffset),
    pageSize: String(pageSize),
  });
  if (email) params.set("email", email);
  return apiFetch<PagedResponse<AdminSeller>>(`/admin/sellers?${params}`);
}

export async function activateCustomer(id: string) {
  return apiFetch<ApiResponse<string>>(`/admin/customers/${id}/activate`, { method: "PATCH" });
}

export async function deactivateCustomer(id: string) {
  return apiFetch<ApiResponse<string>>(`/admin/customers/${id}/deactivate`, { method: "PATCH" });
}

export async function activateSeller(id: string) {
  return apiFetch<ApiResponse<string>>(`/admin/sellers/${id}/activate`, { method: "PATCH" });
}

export async function deactivateSeller(id: string) {
  return apiFetch<ApiResponse<string>>(`/admin/sellers/${id}/deactivate`, { method: "PATCH" });
}

export async function getAdminProducts(opts: { max?: number; offset?: number } = {}) {
  const params = new URLSearchParams();
  if (opts.max !== undefined) params.set("max", String(opts.max));
  if (opts.offset !== undefined) params.set("offset", String(opts.offset));
  const response = await apiFetch<ApiResponse<SpringPage<AdminProduct>>>(`/admin/products?${params}`);
  return response.data;
}

export async function activateProduct(id: string) {
  return apiFetch<ApiResponse<string>>(`/admin/products/${id}/activate`, { method: "PUT" });
}

export async function deactivateProduct(id: string) {
  return apiFetch<ApiResponse<string>>(`/admin/products/${id}/deactivate`, { method: "PUT" });
}

export async function getAdminOrders() {
  const response = await apiFetch<PagedResponse<OrderSummary>>(
    "/admin/orders?pageOffset=0&pageSize=20"
  );
  return response.content;
}

export async function getAdminOrder(orderId: string) {
  const response = await apiFetch<ApiResponse<Order>>(`/admin/orders/${orderId}`);
  return response.data;
}

export async function getAdminCategories() {
  const response = await apiFetch<ApiResponse<SpringPage<CategoryTree>>>("/categories?max=100");
  return response.data.content;
}

export async function createCategory(name: string, parentId?: string) {
  const response = await apiFetch<ApiResponse<string>>("/categories", {
    method: "POST",
    body: JSON.stringify({ name, parentId: parentId || null }),
  });
  return response.data;
}

export async function updateCategoryName(categoryId: string, name: string) {
  return apiFetch<ApiResponse<string>>(`/categories/${categoryId}`, {
    method: "PUT",
    body: JSON.stringify({ name }),
  });
}

export function imageUrl(path?: string) {
  if (!path) return null;
  if (path.startsWith("http")) return path;
  return `${API_URL}${path}`;
}
