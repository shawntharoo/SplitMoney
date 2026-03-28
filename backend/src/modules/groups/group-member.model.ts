import { Field, ID, ObjectType } from '@nestjs/graphql';
import { UserModel } from '../users/user.model';

@ObjectType()
export class GroupMemberModel {
  @Field(() => ID)
  id!: string;

  @Field()
  joinedAt!: Date;

  @Field(() => UserModel)
  user!: UserModel;
}
