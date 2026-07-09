import type { Metadata } from "next";
import { Navbar } from "@/components/Navbar";
import "./globals.css";

export const metadata: Metadata = {
  title: "Zenvy Shop",
  description: "Full-stack marketplace storefront",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body>
        <Navbar />
        <main className="mx-auto min-h-[calc(100vh-73px)] max-w-6xl px-4 py-8">
          {children}
        </main>
      </body>
    </html>
  );
}
