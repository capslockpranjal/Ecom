/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/pages/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/components/**/*.{js,ts,jsx,tsx,mdx}",
    "./src/app/**/*.{js,ts,jsx,tsx,mdx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: {
          DEFAULT: "#4a6b5d",
          dark: "#3a5549",
          light: "#6b8f7e",
        },
        zen: {
          50: "#f7f8f5",
          100: "#eceee8",
          200: "#d8ddd2",
          300: "#b8c4b4",
          800: "#2c3d34",
          900: "#1a2620",
        },
        gold: {
          DEFAULT: "#b8954a",
          light: "#e8d9b0",
          dark: "#8f7035",
        },
      },
      fontFamily: {
        display: ["var(--font-display)", "Georgia", "serif"],
        sans: ["var(--font-sans)", "system-ui", "sans-serif"],
      },
      boxShadow: {
        zen: "0 4px 24px -4px rgba(44, 61, 52, 0.08)",
        "zen-lg": "0 12px 40px -8px rgba(44, 61, 52, 0.12)",
      },
      backgroundImage: {
        "zen-gradient":
          "linear-gradient(135deg, #3d5348 0%, #4a6b5d 45%, #5c7a6b 100%)",
        "zen-radial":
          "radial-gradient(ellipse at 30% 20%, rgba(184, 149, 74, 0.15) 0%, transparent 50%)",
      },
    },
  },
  plugins: [],
};
