import { Args, Mutation, Query, Resolver } from '@nestjs/graphql';
import { CurrentUserId } from 'src/common/graphql/current-user.decorator';
import { CreateUserInput } from './dto/create-user.input';
import { UserModel } from './user.model';
import { UsersService } from './users.service';

@Resolver(() => UserModel)
export class UsersResolver {
  constructor(private readonly usersService: UsersService) {}

  @Query(() => UserModel, { nullable: true })
  me(@CurrentUserId() userId?: string) {
    return this.usersService.getViewer(userId);
  }

  @Query(() => [UserModel])
  users() {
    return this.usersService.listUsers();
  }

  @Mutation(() => UserModel)
  createUser(@Args('input') input: CreateUserInput) {
    return this.usersService.createUser(input);
  }
}
