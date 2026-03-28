import { Field, Float, InputType } from '@nestjs/graphql';
import { ArrayMinSize, IsArray, IsNumber, IsOptional, IsUUID, MaxLength, Min } from 'class-validator';

@InputType()
class ExpenseParticipantShareInput {
  @Field()
  @IsUUID('4')
  userId!: string;

  @Field(() => Float, { nullable: true })
  @IsOptional()
  @IsNumber()
  @Min(0)
  shareAmount?: number;
}

@InputType()
export class CreateExpenseInput {
  @Field()
  @IsUUID('4')
  groupId!: string;

  @Field()
  @MaxLength(120)
  title!: string;

  @Field(() => Float)
  @IsNumber()
  @Min(0.01)
  amount!: number;

  @Field()
  @IsUUID('4')
  payerUserId!: string;

  @Field(() => [ExpenseParticipantShareInput])
  @IsArray()
  @ArrayMinSize(1)
  participants!: ExpenseParticipantShareInput[];
}

export { ExpenseParticipantShareInput };
