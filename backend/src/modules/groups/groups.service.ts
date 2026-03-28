import { BadRequestException, Injectable, NotFoundException } from '@nestjs/common';
import { Group } from '@prisma/client';
import { PrismaService } from 'src/prisma/prisma.service';
import { UsersService } from '../users/users.service';
import { AddGroupMemberInput } from './dto/add-group-member.input';
import { CreateGroupInput } from './dto/create-group.input';

const groupInclude = {
  creator: true,
  members: {
    include: {
      user: true,
    },
  },
} as const;

@Injectable()
export class GroupsService {
  constructor(
    private readonly prisma: PrismaService,
    private readonly usersService: UsersService,
  ) {}

  async listGroups(): Promise<(Group & { creator: unknown; members: unknown[] })[]> {
    return this.prisma.group.findMany({
      where: { deletedAt: null },
      include: groupInclude,
      orderBy: { createdAt: 'asc' },
    });
  }

  async getGroupById(id: string) {
    const group = await this.prisma.group.findFirst({
      where: {
        id,
        deletedAt: null,
      },
      include: groupInclude,
    });

    if (!group) {
      throw new NotFoundException(`Group ${id} was not found.`);
    }

    return group;
  }

  async createGroup(input: CreateGroupInput, creatorUserId?: string) {
    if (!creatorUserId) {
      throw new BadRequestException('x-user-id header is required to create a group.');
    }

    await this.usersService.getUserOrThrow(creatorUserId);

    const uniqueMemberIds = Array.from(new Set([creatorUserId, ...input.memberUserIds]));
    await Promise.all(uniqueMemberIds.map((memberId) => this.usersService.getUserOrThrow(memberId)));

    return this.prisma.group.create({
      data: {
        name: input.name.trim(),
        creatorUserId,
        members: {
          create: uniqueMemberIds.map((userId) => ({ userId })),
        },
      },
      include: groupInclude,
    });
  }

  async addMember(input: AddGroupMemberInput) {
    await this.getGroupById(input.groupId);
    await this.usersService.getUserOrThrow(input.userId);

    await this.prisma.groupMember.upsert({
      where: {
        groupId_userId: {
          groupId: input.groupId,
          userId: input.userId,
        },
      },
      update: {},
      create: {
        groupId: input.groupId,
        userId: input.userId,
      },
    });

    return this.getGroupById(input.groupId);
  }
}
