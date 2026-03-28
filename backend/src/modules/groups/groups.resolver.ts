import { Args, Mutation, Query, Resolver } from '@nestjs/graphql';
import { CurrentUserId } from 'src/common/graphql/current-user.decorator';
import { AddGroupMemberInput } from './dto/add-group-member.input';
import { CreateGroupInput } from './dto/create-group.input';
import { GroupModel } from './group.model';
import { GroupsService } from './groups.service';

@Resolver(() => GroupModel)
export class GroupsResolver {
  constructor(private readonly groupsService: GroupsService) {}

  @Query(() => [GroupModel])
  groups() {
    return this.groupsService.listGroups();
  }

  @Query(() => GroupModel)
  group(@Args('id') id: string) {
    return this.groupsService.getGroupById(id);
  }

  @Mutation(() => GroupModel)
  createGroup(
    @Args('input') input: CreateGroupInput,
    @CurrentUserId() creatorUserId?: string,
  ) {
    return this.groupsService.createGroup(input, creatorUserId);
  }

  @Mutation(() => GroupModel)
  addGroupMember(@Args('input') input: AddGroupMemberInput) {
    return this.groupsService.addMember(input);
  }
}
