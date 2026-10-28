# SkillBridge NG — Frontend

React + Vite + Tailwind (v4) frontend for the SkillBridge NG Spring Boot backend.

## Setup
1. Backend running at `http://localhost:8081` (CORS allows `http://localhost:5173`).
2. `npm install`
3. `.env` (see `.env.example`): `VITE_API_BASE_URL=http://localhost:8081`
4. `npm run dev` → http://localhost:5173

> Tailwind v4 is configured through `@tailwindcss/vite` — do **not** run `npx tailwindcss init -p`.

## Structure
- `src/api/` — one module per backend area; `axios.js` holds the instance, token attach and one-shot refresh (tokens rotate).
- `src/context/AuthContext.jsx` — session restore, login, register, logout.
- `src/components/` — shared UI, `ResourceManager` (generic CRUD), `ProfileView`, `JobForm`.
- `src/pages/` — routes.

## Known limits (backend)
No endpoint for: user listing, editing name/bio/location/phone, file upload, payments, wallet, AI, admin.
Those show "Coming soon" (or take URLs / user IDs) and never fake data.
