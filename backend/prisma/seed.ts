import { PrismaClient } from '@prisma/client';

const prisma = new PrismaClient();

async function main() {
  const alice = await prisma.user.upsert({
    where: { username: 'alice' },
    update: {},
    create: {
      username: 'alice',
      displayName: 'Alice',
    },
  });

  const bob = await prisma.user.upsert({
    where: { username: 'bob' },
    update: {},
    create: {
      username: 'bob',
      displayName: 'Bob',
    },
  });

  const group = await prisma.group.upsert({
    where: { id: '11111111-1111-1111-1111-111111111111' },
    update: {},
    create: {
      id: '11111111-1111-1111-1111-111111111111',
      name: 'Weekend Trip',
      creatorUserId: alice.id,
      members: {
        create: [
          { userId: alice.id },
          { userId: bob.id },
        ],
      },
    },
    include: {
      members: true,
    },
  });

  const expense = await prisma.expense.upsert({
    where: { id: '22222222-2222-2222-2222-222222222222' },
    update: {},
    create: {
      id: '22222222-2222-2222-2222-222222222222',
      groupId: group.id,
      title: 'Dinner',
      amount: '84.00',
      payerUserId: alice.id,
      createdByUserId: alice.id,
    },
  });

  await prisma.expenseParticipant.createMany({
    data: [
      { expenseId: expense.id, userId: alice.id, shareAmount: '42.00' },
      { expenseId: expense.id, userId: bob.id, shareAmount: '42.00' },
    ],
    skipDuplicates: true,
  });
}

main()
  .finally(async () => {
    await prisma.$disconnect();
  })
  .catch(async (error) => {
    console.error(error);
    await prisma.$disconnect();
    process.exit(1);
  });
