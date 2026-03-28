import { Field, InputType } from '@nestjs/graphql';
import { IsEmail, IsOptional, IsString, MaxLength, MinLength } from 'class-validator';

@InputType()
export class CreateUserInput {
  @Field()
  @IsString()
  @MinLength(3)
  @MaxLength(32)
  username!: string;

  @Field({ nullable: true })
  @IsOptional()
  @IsEmail()
  email?: string;

  @Field({ nullable: true })
  @IsOptional()
  @IsString()
  @MaxLength(64)
  displayName?: string;
}
