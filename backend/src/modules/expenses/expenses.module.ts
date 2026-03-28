import { Module } from '@nestjs/common';
import { GroupsModule } from '../groups/groups.module';
import { UsersModule } from '../users/users.module';
import { ExpensesResolver } from './expenses.resolver';
import { ExpensesService } from './expenses.service';

@Module({
  imports: [GroupsModule, UsersModule],
  providers: [ExpensesResolver, ExpensesService],
  exports: [ExpensesService],
})
export class ExpensesModule {}
