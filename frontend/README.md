# Transport Optimizer - Development Project

## Project Structure

```
/app
  /(auth)
    /login
    /signup
  /dashboard
    /passenger
    /driver
    /owner
  /api
    /routes
    /trips
    /auth
/components
  /ui (Shadcn components)
  /dashboard
  /maps
/lib
  supabase.ts
  utils.ts
/types
  index.ts
```

## Environment Setup

1. Copy `.env.local` and add your API keys
2. Run `npm install` to install dependencies
3. Run `npm run dev` to start development server

## Tech Stack
- **Frontend**: Next.js 14+ with TypeScript
- **Styling**: Tailwind CSS + Shadcn UI
- **Backend**: Supabase (PostgreSQL)
- **Maps**: Google Maps API
- **AI**: Google Gemini API

## Current Status
✅ Project initialized
✅ UI components installed
✅ Supabase client configured
⏳ Database schema pending
⏳ Authentication pending
