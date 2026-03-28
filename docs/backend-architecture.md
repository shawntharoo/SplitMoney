# SplitMoney Backend Architecture

## Goal

Build a production backend for the existing KMP app using:

- GraphQL
- Node.js with TypeScript
- PostgreSQL
- AWS

The app already has working local flows backed by SQLDelight. The backend should become the source of truth, while the mobile app can keep local persistence for caching and offline support.

## Recommended Stack

- Runtime: `Node.js 22`
- Language: `TypeScript`
- API layer: `GraphQL`
- Framework: `NestJS`
- GraphQL server: `Apollo`
- Database: `PostgreSQL`
- ORM: `Prisma`
- Auth: `JWT` access tokens + refresh token rotation
- Validation: `class-validator` or `zod`
- Background jobs: start without one, add `BullMQ` only if needed later

## Why GraphQL Fits This App

This app has nested, relational screens:

- Groups -> expenses
- Expenses -> participants
- Groups -> balances and settlements
- Users -> memberships and usage constraints

GraphQL is a good fit because:

- mobile screens can fetch exactly the shape they need
- group detail and expense detail are naturally nested views
- future sync and pagination are easier to evolve without endpoint sprawl

The main risk with GraphQL is over-fetching through badly designed resolvers. We should avoid that by using clear query boundaries, batching, and auth checks at every resolver.

## High-Level Architecture

```text
iOS / Android (KMP app)
    ->
Shared networking layer
    ->
GraphQL API (Node + NestJS)
    ->
Application services
    ->
Prisma
    ->
PostgreSQL
```

Supporting AWS services:

- `App Runner` or `ECS Fargate` for the API
- `RDS PostgreSQL` for the database
- `Secrets Manager` for DB credentials and JWT secrets
- `CloudWatch` for logs and alarms
- `Route 53` for DNS
- `ACM` for TLS certificates

## AWS Recommendation

Start simple:

- API: `AWS App Runner`
- Database: `AWS RDS PostgreSQL`
- Secrets: `AWS Secrets Manager`
- Static docs/assets if needed: `S3`

Why this is the best first production setup:

- easier than full ECS at the start
- still production-grade
- cleaner path to autoscaling
- lower ops overhead while the product is still evolving

Use `ECS Fargate` later if you need:

- more control over networking
- sidecars
- more advanced deployment behavior
- multi-service backend expansion

## Domain Model

Core entities:

- `User`
- `Group`
- `GroupMember`
- `Expense`
- `ExpenseParticipant`

Recommended additional backend entities:

- `RefreshToken`
- `UserDevice`
- `AuditLog` later if needed

## PostgreSQL Schema

### users

- `id uuid pk`
- `username varchar unique not null`
- `email varchar unique null`
- `password_hash varchar null`
- `display_name varchar null`
- `created_at timestamptz not null`
- `updated_at timestamptz not null`
- `deleted_at timestamptz null`

Notes:

- `username` should be case-insensitive unique
- use soft delete only if product requirements need recovery
- otherwise hard delete is simpler, but this app likely benefits from soft-delete support later

### groups

- `id uuid pk`
- `name varchar not null`
- `creator_user_id uuid not null fk users.id`
- `created_at timestamptz not null`
- `updated_at timestamptz not null`
- `deleted_at timestamptz null`

### group_members

- `id uuid pk`
- `group_id uuid not null fk groups.id`
- `user_id uuid not null fk users.id`
- `joined_at timestamptz not null`

Constraints:

- unique `(group_id, user_id)`

### expenses

- `id uuid pk`
- `group_id uuid not null fk groups.id`
- `title varchar not null`
- `amount numeric(12,2) not null`
- `payer_user_id uuid not null fk users.id`
- `created_by_user_id uuid not null fk users.id`
- `created_at timestamptz not null`
- `updated_at timestamptz not null`
- `deleted_at timestamptz null`

### expense_participants

- `id uuid pk`
- `expense_id uuid not null fk expenses.id`
- `user_id uuid not null fk users.id`
- `share_amount numeric(12,2) null`
- `created_at timestamptz not null`

Constraints:

- unique `(expense_id, user_id)`

Notes:

- `share_amount` can be nullable at first if you are doing equal split
- later this lets you support unequal splits without redesigning the schema

### refresh_tokens

- `id uuid pk`
- `user_id uuid not null fk users.id`
- `token_hash varchar not null`
- `expires_at timestamptz not null`
- `revoked_at timestamptz null`
- `created_at timestamptz not null`
- `device_name varchar null`

## Business Rules

These should live in application services, not only in GraphQL resolvers.

### Users

- usernames must be unique
- user deletion is allowed only when the user:
  - is not the creator of any active group
  - is not included in any active expense

### Groups

- a group has one creator
- a group can have many members
- deleting a group deletes:
  - its expenses
  - its expense participants
  - its group member rows

This is consistent with the current app behavior.

### Expenses

- each expense belongs to exactly one group
- each expense has one payer
- participants must belong to the group
- payer should usually be included as a participant unless you explicitly support non-participant payer flows

### Balances / Settlements

Do not store settlements as a permanent table at first.

Instead:

- store raw expense data
- compute balances and settlement recommendations dynamically

Reason:

- settlements are derived data
- recomputing avoids drift and update bugs
- easier to keep correct while product rules evolve

## GraphQL Schema Shape

Recommended top-level structure:

```graphql
type Query {
  me: Me!
  groups: [Group!]!
  group(id: ID!): GroupDetails!
  expense(id: ID!): ExpenseDetails!
  users: [User!]!
}

type Mutation {
  signUp(input: SignUpInput!): AuthPayload!
  signIn(input: SignInInput!): AuthPayload!
  refreshToken(input: RefreshTokenInput!): AuthPayload!
  signOut: Boolean!

  createUser(input: CreateUserInput!): User!
  deleteUser(id: ID!): DeleteUserResult!

  createGroup(input: CreateGroupInput!): Group!
  deleteGroup(id: ID!): Boolean!
  addGroupMember(input: AddGroupMemberInput!): Group!
  removeGroupMember(input: RemoveGroupMemberInput!): Group!

  createExpense(input: CreateExpenseInput!): Expense!
  deleteExpense(id: ID!): Boolean!
  addExpenseParticipant(input: AddExpenseParticipantInput!): Expense!
  removeExpenseParticipant(input: RemoveExpenseParticipantInput!): Expense!
}
```

Suggested object types:

```graphql
type User {
  id: ID!
  username: String!
  displayName: String
}

type Group {
  id: ID!
  name: String!
  creator: User!
  members: [User!]!
}

type Expense {
  id: ID!
  title: String!
  amount: Decimal!
  payer: User!
  participants: [User!]!
  groupId: ID!
}

type GroupDetails {
  group: Group!
  expenses: [Expense!]!
  balances: [UserNetBalance!]!
  settlements: [Settlement!]!
}

type ExpenseDetails {
  expense: Expense!
  balances: [ExpenseParticipantBalance!]!
  settlements: [Settlement!]!
}

type UserNetBalance {
  user: User!
  netAmount: Decimal!
}

type ExpenseParticipantBalance {
  user: User!
  owes: Decimal!
  gets: Decimal!
}

type Settlement {
  from: User!
  to: User!
  amount: Decimal!
}
```

## Resolver Strategy

Keep resolvers thin.

Good pattern:

- resolver handles auth and input mapping
- service handles business rules
- repository/Prisma handles persistence

Suggested module structure:

```text
backend/src
  app.module.ts
  common/
  auth/
  users/
  groups/
  expenses/
  balances/
  prisma/
```

Inside each domain:

```text
users/
  users.module.ts
  users.resolver.ts
  users.service.ts
  users.repository.ts
  dto/
```

## Auth Plan

For store release, the app should move away from username-only identity.

Recommended first version:

- email + password sign-up
- JWT access token
- refresh token stored securely

Recommended later additions:

- Sign in with Apple
- Google sign-in

For mobile token storage:

- iOS: Keychain
- Android: EncryptedSharedPreferences or Keystore-backed secure storage

## Mobile Sync Strategy

The backend should become the source of truth.

Recommended mobile approach:

- fetch from GraphQL
- write server responses into SQLDelight local tables
- render UI from local DB or repository layer
- queue mutations for retry later if you want offline support

Phased rollout:

1. Replace local-only CRUD with online CRUD
2. Store server data locally
3. Add offline read support
4. Add offline mutation queue only if needed

Do not build full offline sync complexity on day one unless it is a product requirement.

## Security

Required before production:

- JWT secret rotation plan
- password hashing with `argon2`
- request rate limiting
- GraphQL depth/complexity limits
- input validation
- audit logging for auth events
- CORS restrictions
- HTTPS only

Also:

- do not expose internal numeric IDs if you can avoid it
- prefer UUIDs for external entities

## Observability

Minimum production observability:

- structured logs
- request IDs
- GraphQL error logging
- DB slow query monitoring
- CloudWatch alarms on:
  - 5xx rate
  - container restarts
  - DB CPU / storage / connection pressure

## Deployment Environments

Create at least:

- `dev`
- `staging`
- `prod`

Each environment should have:

- separate database
- separate secrets
- separate API URL

Never share production DB with development or staging builds.

## Immediate Build Plan

### Phase 1

- scaffold `backend/` with Node + TypeScript + NestJS
- add Prisma
- define PostgreSQL schema
- implement auth module

### Phase 2

- implement GraphQL queries and mutations for:
  - users
  - groups
  - expenses
  - participants

### Phase 3

- connect mobile app networking layer to backend
- keep SQLDelight as local cache

### Phase 4

- add AWS deployment
- add monitoring
- prepare release environments

## Recommended First Deliverables

The next coding step should create:

1. `backend/` Node project
2. Prisma schema matching this document
3. GraphQL schema modules for auth, users, groups, expenses
4. Local Docker PostgreSQL for development
5. environment config for AWS-ready deployment

## Notes For This Project

The current app behavior already assumes:

- group deletion cascades into expenses
- user deletion is blocked by active usage
- expense details and settlements are derived views

That means the backend should preserve those same rules so the mobile app does not end up with different logic locally and remotely.
