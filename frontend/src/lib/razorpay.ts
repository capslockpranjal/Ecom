"use client";

import { getPaymentSession, verifyPayment } from "@/lib/api";

declare global {
  interface Window {
    Razorpay?: new (options: Record<string, unknown>) => {
      open: () => void;
    };
  }
}

function loadRazorpayScript(): Promise<void> {
  if (typeof window === "undefined") return Promise.resolve();
  if (window.Razorpay) return Promise.resolve();

  return new Promise((resolve, reject) => {
    const script = document.createElement("script");
    script.src = "https://checkout.razorpay.com/v1/checkout.js";
    script.onload = () => resolve();
    script.onerror = () => reject(new Error("Failed to load Razorpay"));
    document.body.appendChild(script);
  });
}

export async function openRazorpayCheckout(orderId: string): Promise<void> {
  await loadRazorpayScript();

  const session = await getPaymentSession(orderId);

  return new Promise((resolve, reject) => {
    if (!window.Razorpay) {
      reject(new Error("Razorpay is not available"));
      return;
    }

    const razorpay = new window.Razorpay({
      key: session.razorpayKeyId,
      amount: session.amount,
      currency: session.currency,
      name: "Zenvy",
      description: `Order #${orderId.slice(0, 8)}`,
      order_id: session.razorpayOrderId,
      handler: async (response: {
        razorpay_payment_id: string;
        razorpay_order_id: string;
        razorpay_signature: string;
      }) => {
        try {
          await verifyPayment(orderId, {
            razorpayOrderId: response.razorpay_order_id,
            razorpayPaymentId: response.razorpay_payment_id,
            razorpaySignature: response.razorpay_signature,
          });
          resolve();
        } catch (err) {
          reject(err);
        }
      },
      modal: {
        ondismiss: () => reject(new Error("Payment cancelled")),
      },
    });

    razorpay.open();
  });
}
