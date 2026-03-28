# SplitMoney

This repository is now organized as a small monorepo with a clear separation between client and server code.

## Structure

- `frontend/` - Kotlin Multiplatform mobile app
  - `androidApp/` - Android application
  - `iosApp/` - iOS application
  - `shared/` - shared KMM code, models, persistence, and view models
- `backend/` - Node.js GraphQL API with NestJS, Prisma, and PostgreSQL
- `docs/` - architecture and planning notes

## Working directories

Frontend commands should be run from `frontend/`.

Examples:

```bash
cd frontend
./gradlew build
```

Backend commands should be run from `backend/`.

Examples:

```bash
cd backend
npm install
npm run start:dev
```

## Why this layout

- keeps mobile and backend concerns separate
- makes deployment boundaries clearer
- makes it easier to add CI pipelines per app
- avoids the feeling that one app is nested inside the other
