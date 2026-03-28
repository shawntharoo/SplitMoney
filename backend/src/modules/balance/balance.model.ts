import { Field, Float, ID, ObjectType } from '@nestjs/graphql';
import { UserModel } from '../users/user.model';

@ObjectType()
export class BalanceEntryModel {
  @Field(() => ID)
  userId!: string;

  @Field(() => UserModel)
  user!: UserModel;

  @Field(() => Float)
  paid!: number;

  @Field(() => Float)
  owes!: number;

  @Field(() => Float)
  net!: number;
}
