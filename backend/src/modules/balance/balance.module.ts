import { Module } from '@nestjs/common';
import { BalanceResolver } from './balance.resolver';
import { BalanceService } from './balance.service';

@Module({
  providers: [BalanceResolver, BalanceService],
})
export class BalanceModule {}
