import { ConflictException, Injectable, NotFoundException } from '@nestjs/common';
import { User } from '@prisma/client';
import { PrismaService } from 'src/prisma/prisma.service';
import { CreateUserInput } from './dto/create-user.input';

@Injectable()
export class UsersService {
  constructor(private readonly prisma: PrismaService) {}

  async createUser(input: CreateUserInput): Promise<User> {
    const existingUser = await this.prisma.user.findFirst({
      where: {
        OR: [
          { username: input.username },
          input.email ? { email: input.email } : undefined,
        ].filter(Boolean) as { username?: string; email?: string }[],
      },
    });

    if (existingUser) {
      throw new ConflictException('User with that username or email already exists.');
    }

    return this.prisma.user.create({
      data: {
        username: input.username.trim(),
        email: input.email?.trim(),
        displayName: input.displayName?.trim(),
      },
    });
  }

  async getViewer(userId?: string): Promise<User | null> {
    if (!userId) {
      return null;
    }

    return this.prisma.user.findUnique({
      where: { id: userId },
    });
  }

  async listUsers(): Promise<User[]> {
    return this.prisma.user.findMany({
      where: { deletedAt: null },
      orderBy: { createdAt: 'asc' },
    });
  }

  async getUserOrThrow(id: string): Promise<User> {
    const user = await this.prisma.user.findUnique({
      where: { id },
    });

    if (!user || user.deletedAt) {
      throw new NotFoundException(`User ${id} was not found.`);
    }

    return user;
  }
}
