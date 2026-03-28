import { Module } from '@nestjs/common';
import { ConfigModule } from '@nestjs/config';
import { ApolloDriver, ApolloDriverConfig } from '@nestjs/apollo';
import { GraphQLModule } from '@nestjs/graphql';
import { join } from 'path';
import { Request } from 'express';
import appConfig from 'src/common/config/app.config';
import { GraphqlContext } from 'src/common/graphql/graphql-context';
import { PrismaModule } from 'src/prisma/prisma.module';
import { AppResolver } from './app.resolver';
import { AuthModule } from '../auth/auth.module';
import { UsersModule } from '../users/users.module';
import { GroupsModule } from '../groups/groups.module';
import { ExpensesModule } from '../expenses/expenses.module';
import { BalanceModule } from '../balance/balance.module';

@Module({
  imports: [
    ConfigModule.forRoot({
      isGlobal: true,
      load: [appConfig],
    }),
    GraphQLModule.forRoot<ApolloDriverConfig>({
      driver: ApolloDriver,
      autoSchemaFile: join(process.cwd(), 'src/schema.gql'),
      sortSchema: true,
      playground: true,
      context: ({ req }: { req: Request }): GraphqlContext => ({
        req,
        viewer: {
          userId: typeof req.headers['x-user-id'] === 'string' ? req.headers['x-user-id'] : undefined,
        },
      }),
    }),
    PrismaModule,
    AuthModule,
    UsersModule,
    GroupsModule,
    ExpensesModule,
    BalanceModule,
  ],
  providers: [AppResolver],
})
export class AppModule {}
