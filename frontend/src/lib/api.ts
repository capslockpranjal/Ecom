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

export type AuthTokens = {
  accessToken: string;
  refreshToken: string;
};

export type Category = {
  id: string;
  name: string;
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

function getAccessToken(): string | null {
  if (typeof window === "undefined") return null;
  return localStorage.getItem("accessToken");
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

async function apiFetch<T>(
  path: string,
  options: RequestInit = {},
  auth = true
): Promise<T> {
  const headers = new Headers(options.headers || {});
  if (!headers.has("Content-Type") && options.body) {
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

export async function login(email: string, password: string) {
  const response = await apiFetch<ApiResponse<AuthTokens>>(
    "/auth/login",
    {
      method: "POST",
      body: JSON.stringify({ email, password }),
    },
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
    {
      method: "POST",
      body: JSON.stringify(payload),
    },
    false
  );
}

export async function getCategories(categoryId?: string) {
  const query = categoryId ? `?categoryId=${categoryId}` : "";
  const response = await apiFetch<ApiResponse<Category[]>>(
    `/categories/customer${query}`
  );
  return response.data;
}

export async function getProducts(categoryId: string) {
  const response = await apiFetch<ApiResponse<{ content: ProductListItem[] }>>(
    `/customer/products?categoryId=${categoryId}&max=50&offset=0`
  );
  return response.data.content;
}

export async function getProduct(productId: string) {
  const response = await apiFetch<ApiResponse<ProductDetail>>(
    `/customer/products/${productId}`
  );
  return response.data;
}

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
  const response = await apiFetch<ApiResponse<Cart>>(
    `/customer/cart/items/${itemId}`,
    {
      method: "PATCH",
      body: JSON.stringify({ quantity }),
    }
  );
  return response.data;
}

export async function removeCartItem(itemId: string) {
  const response = await apiFetch<ApiResponse<Cart>>(
    `/customer/cart/items/${itemId}`,
    { method: "DELETE" }
  );
  return response.data;
}

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

export async function checkout(addressId: string, paymentMethod: "COD" | "ONLINE" = "COD") {
  const response = await apiFetch<ApiResponse<Order>>("/customer/orders/checkout", {
    method: "POST",
    body: JSON.stringify({ addressId, paymentMethod }),
  });
  return response.data;
}

export async function cancelOrder(orderId: string) {
  return apiFetch<ApiResponse<string>>(`/customer/orders/${orderId}/cancel`, {
    method: "POST",
  });
}

export async function confirmPayment(orderId: string) {
  const response = await apiFetch<ApiResponse<Order>>(
    `/customer/orders/${orderId}/confirm-payment`,
    { method: "POST" }
  );
  return response.data;
}

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

export async function getOrders() {
  const response = await apiFetch<PagedResponse<OrderSummary>>(
    "/customer/orders?pageOffset=0&pageSize=20"
  );
  return response.content;
}

export async function getOrder(orderId: string) {
  const response = await apiFetch<ApiResponse<Order>>(
    `/customer/orders/${orderId}`
  );
  return response.data;
}

export function imageUrl(path?: string) {
  if (!path) return null;
  if (path.startsWith("http")) return path;
  return `${API_URL}${path}`;
}
