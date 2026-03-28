import { Field, ID, ObjectType } from '@nestjs/graphql';
import { GroupMemberModel } from './group-member.model';
import { UserModel } from '../users/user.model';

@ObjectType()
export class GroupModel {
  @Field(() => ID)
  id!: string;

  @Field()
  name!: string;

  @Field(() => UserModel)
  creator!: UserModel;

  @Field(() => [GroupMemberModel])
  members!: GroupMemberModel[];

  @Field()
  createdAt!: Date;

  @Field()
  updatedAt!: Date;
}
