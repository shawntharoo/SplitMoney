import { Field, Float, ID, ObjectType } from '@nestjs/graphql';
import { UserModel } from '../users/user.model';

@ObjectType()
export class ExpenseParticipantModel {
  @Field(() => ID)
  id!: string;

  @Field(() => UserModel)
  user!: UserModel;

  @Field(() => Float, { nullable: true })
  shareAmount?: number | null;
}
