# Zenvy Frontend

Next.js customer storefront for the Zenvy marketplace.

## Setup

```bash
cp .env.local.example .env.local
npm install
npm run dev
```

Open [http://localhost:3000](http://localhost:3000).

Ensure the backend is running on `http://localhost:8080` with CORS enabled for `http://localhost:3000`.

## Pages

- `/` — landing page
- `/login`, `/register` — customer auth
- `/shop` — browse categories and products
- `/products/[id]` — product detail and add to cart
- `/cart` — manage cart
- `/checkout` — place COD order
- `/orders` — order history
- `/seller/orders` — seller fulfillment dashboard
- `/admin/orders` — admin order oversight
