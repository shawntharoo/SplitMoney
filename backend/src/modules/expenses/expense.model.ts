import { Field, Float, ID, ObjectType } from '@nestjs/graphql';
import { GroupModel } from '../groups/group.model';
import { UserModel } from '../users/user.model';
import { ExpenseParticipantModel } from './expense-participant.model';

@ObjectType()
export class ExpenseModel {
  @Field(() => ID)
  id!: string;

  @Field()
  title!: string;

  @Field(() => Float)
  amount!: number;

  @Field(() => GroupModel)
  group!: GroupModel;

  @Field(() => UserModel)
  payer!: UserModel;

  @Field(() => UserModel)
  createdBy!: UserModel;

  @Field(() => [ExpenseParticipantModel])
  participants!: ExpenseParticipantModel[];

  @Field()
  createdAt!: Date;

  @Field()
  updatedAt!: Date;
}
