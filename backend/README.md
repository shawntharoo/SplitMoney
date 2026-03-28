# SplitMoney Backend

Production-oriented backend scaffold for SplitMoney using GraphQL, NestJS, Prisma, PostgreSQL, and AWS-friendly container deployment.

## Stack

- Node.js 20+ locally, Node 20 container image
- NestJS with Apollo GraphQL
- Prisma ORM
- PostgreSQL
- Docker for local/container deploys
- AWS App Runner friendly layout

## Quick start

1. Copy `.env.example` to `.env`.
2. Start PostgreSQL locally or point `DATABASE_URL` at an existing instance.
3. Install dependencies:

```bash
npm install
```

4. Create the database schema:

```bash
npx prisma migrate dev --name init
```

5. Generate the Prisma client and optional seed data:

```bash
npx prisma generate
npm run seed
```

6. Run the API:

```bash
npm run start:dev
```

GraphQL will be available at `http://localhost:4000/graphql`.

## Initial GraphQL operations

- `me`
- `users`
- `groups`
- `group(id: ID!)`
- `expense(id: ID!)`
- `groupBalances(groupId: ID!)`
- `createUser`
- `createGroup`
- `addGroupMember`
- `createExpense`
- `deleteExpense`

Use the `x-user-id` request header for the temporary authenticated user context until real JWT auth is added.

## Suggested AWS deployment

- API container: AWS App Runner
- Database: Amazon RDS PostgreSQL
- Secrets: AWS Secrets Manager
- Logs/alarms: CloudWatch

Recommended production environment variables:

- `DATABASE_URL`
- `JWT_ACCESS_SECRET`
- `JWT_REFRESH_SECRET`
- `CORS_ORIGIN`
- `PORT`

## Next steps

- Add JWT login and refresh token rotation
- Add pagination for group expenses
- Add request-scoped dataloaders to reduce nested query fan-out
- Add tests for business rules and balance calculation
