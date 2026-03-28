import { createParamDecorator, ExecutionContext } from '@nestjs/common';
import { GqlExecutionContext } from '@nestjs/graphql';
import { GraphqlContext } from './graphql-context';

export const CurrentUserId = createParamDecorator(
  (_data: unknown, context: ExecutionContext): string | undefined => {
    const gqlContext = GqlExecutionContext.create(context).getContext<GraphqlContext>();
    return gqlContext.viewer.userId;
  },
);
