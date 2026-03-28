import { Field, InputType } from '@nestjs/graphql';
import { ArrayMinSize, IsArray, IsUUID, MaxLength, MinLength } from 'class-validator';

@InputType()
export class CreateGroupInput {
  @Field()
  @MinLength(2)
  @MaxLength(60)
  name!: string;

  @Field(() => [String])
  @IsArray()
  @ArrayMinSize(1)
  @IsUUID('4', { each: true })
  memberUserIds!: string[];
}
