import { BadRequestException, Injectable, NotFoundException } from '@nestjs/common';
import { Prisma } from '@prisma/client';
import { PrismaService } from 'src/prisma/prisma.service';
import { GroupsService } from '../groups/groups.service';
import { UsersService } from '../users/users.service';
import { CreateExpenseInput } from './dto/create-expense.input';

const expenseInclude = {
  group: {
    include: {
      creator: true,
      members: {
        include: {
          user: true,
        },
      },
    },
  },
  payer: true,
  createdBy: true,
  participants: {
    include: {
      user: true,
    },
  },
} as const;

@Injectable()
export class ExpensesService {
  constructor(
    private readonly prisma: PrismaService,
    private readonly groupsService: GroupsService,
    private readonly usersService: UsersService,
  ) {}

  async getExpenseById(id: string) {
    const expense = await this.prisma.expense.findFirst({
      where: {
        id,
        deletedAt: null,
      },
      include: expenseInclude,
    });

    if (!expense) {
      throw new NotFoundException(`Expense ${id} was not found.`);
    }

    return this.mapExpense(expense);
  }

  async createExpense(input: CreateExpenseInput, creatorUserId?: string) {
    if (!creatorUserId) {
      throw new BadRequestException('x-user-id header is required to create an expense.');
    }

    await this.usersService.getUserOrThrow(creatorUserId);
    await this.usersService.getUserOrThrow(input.payerUserId);
    const group = await this.groupsService.getGroupById(input.groupId);

    const groupMemberIds = new Set(group.members.map((member) => member.userId));
    const participantIds = input.participants.map((participant) => participant.userId);

    if (!groupMemberIds.has(input.payerUserId)) {
      throw new BadRequestException('Payer must be a member of the group.');
    }

    for (const participantId of participantIds) {
      if (!groupMemberIds.has(participantId)) {
        throw new BadRequestException('All participants must be members of the group.');
      }
    }

    const providedShares = input.participants.filter((participant) => participant.shareAmount !== undefined);
    if (providedShares.length > 0) {
      const totalShare = providedShares.reduce((sum, participant) => sum + (participant.shareAmount ?? 0), 0);
      if (Math.abs(totalShare - input.amount) > 0.01) {
        throw new BadRequestException('Participant shares must add up to the total expense amount.');
      }
    }

    const equalSplit = Number((input.amount / input.participants.length).toFixed(2));

    const expense = await this.prisma.expense.create({
      data: {
        groupId: input.groupId,
        title: input.title.trim(),
        amount: new Prisma.Decimal(input.amount.toFixed(2)),
        payerUserId: input.payerUserId,
        createdByUserId: creatorUserId,
        participants: {
          create: input.participants.map((participant) => ({
            userId: participant.userId,
            shareAmount: new Prisma.Decimal(
              (participant.shareAmount ?? equalSplit).toFixed(2),
            ),
          })),
        },
      },
      include: expenseInclude,
    });

    return this.mapExpense(expense);
  }

  async deleteExpense(id: string): Promise<boolean> {
    await this.getExpenseById(id);

    await this.prisma.expense.update({
      where: { id },
      data: {
        deletedAt: new Date(),
      },
    });

    return true;
  }

  private mapExpense<T extends { amount: Prisma.Decimal; participants: Array<{ shareAmount: Prisma.Decimal | null }> }>(
    expense: T,
  ) {
    return {
      ...expense,
      amount: expense.amount.toNumber(),
      participants: expense.participants.map((participant) => ({
        ...participant,
        shareAmount: participant.shareAmount?.toNumber() ?? null,
      })),
    };
  }
}
