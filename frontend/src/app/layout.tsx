import type { Metadata } from "next";
import { Cormorant_Garamond, DM_Sans } from "next/font/google";
import { Navbar } from "@/components/Navbar";
import "./globals.css";

const displayFont = Cormorant_Garamond({
  subsets: ["latin"],
  weight: ["500", "600", "700"],
  variable: "--font-display",
});

const sansFont = DM_Sans({
  subsets: ["latin"],
  weight: ["400", "500", "600", "700"],
  variable: "--font-sans",
});

export const metadata: Metadata = {
  title: "Zenvy — Curated Marketplace",
  description: "Discover thoughtfully curated products in a calm, mindful shopping experience.",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en" className={`${displayFont.variable} ${sansFont.variable}`}>
      <body className="min-h-screen">
        <div className="pointer-events-none fixed inset-0 bg-zen-radial" aria-hidden />
        <Navbar />
        <main className="relative mx-auto min-h-[calc(100vh-73px)] max-w-6xl px-4 py-8 md:px-6 md:py-10">
          {children}
        </main>
      </body>
    </html>
  );
}
