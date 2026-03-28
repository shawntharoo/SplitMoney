import { Args, Query, Resolver } from '@nestjs/graphql';
import { BalanceEntryModel } from './balance.model';
import { BalanceService } from './balance.service';

@Resolver(() => BalanceEntryModel)
export class BalanceResolver {
  constructor(private readonly balanceService: BalanceService) {}

  @Query(() => [BalanceEntryModel])
  groupBalances(@Args('groupId') groupId: string) {
    return this.balanceService.getGroupBalances(groupId);
  }
}
