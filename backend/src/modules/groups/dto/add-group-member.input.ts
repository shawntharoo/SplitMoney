import { Field, InputType } from '@nestjs/graphql';
import { IsUUID } from 'class-validator';

@InputType()
export class AddGroupMemberInput {
  @Field()
  @IsUUID('4')
  groupId!: string;

  @Field()
  @IsUUID('4')
  userId!: string;
}
