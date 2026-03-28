import { Injectable } from '@nestjs/common';
import { PrismaService } from 'src/prisma/prisma.service';

@Injectable()
export class BalanceService {
  constructor(private readonly prisma: PrismaService) {}

  async getGroupBalances(groupId: string) {
    const group = await this.prisma.group.findUniqueOrThrow({
      where: { id: groupId },
      include: {
        members: {
          include: {
            user: true,
          },
        },
        expenses: {
          where: { deletedAt: null },
          include: {
            participants: true,
          },
        },
      },
    });

    const balances = new Map<string, { paid: number; owes: number }>();
    for (const member of group.members) {
      balances.set(member.userId, { paid: 0, owes: 0 });
    }

    for (const expense of group.expenses) {
      const payer = balances.get(expense.payerUserId);
      if (payer) {
        payer.paid += Number(expense.amount);
      }

      for (const participant of expense.participants) {
        const entry = balances.get(participant.userId);
        if (entry) {
          entry.owes += Number(participant.shareAmount ?? 0);
        }
      }
    }

    return group.members.map((member) => {
      const summary = balances.get(member.userId) ?? { paid: 0, owes: 0 };
      return {
        userId: member.userId,
        user: member.user,
        paid: Number(summary.paid.toFixed(2)),
        owes: Number(summary.owes.toFixed(2)),
        net: Number((summary.paid - summary.owes).toFixed(2)),
      };
    });
  }
}
