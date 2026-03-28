import { Field, ID, ObjectType } from '@nestjs/graphql';

@ObjectType()
export class UserModel {
  @Field(() => ID)
  id!: string;

  @Field()
  username!: string;

  @Field({ nullable: true })
  email?: string | null;

  @Field({ nullable: true })
  displayName?: string | null;
}
