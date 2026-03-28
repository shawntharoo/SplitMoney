import { Args, Mutation, Query, Resolver } from '@nestjs/graphql';
import { CurrentUserId } from 'src/common/graphql/current-user.decorator';
import { CreateExpenseInput } from './dto/create-expense.input';
import { ExpenseModel } from './expense.model';
import { ExpensesService } from './expenses.service';

@Resolver(() => ExpenseModel)
export class ExpensesResolver {
  constructor(private readonly expensesService: ExpensesService) {}

  @Query(() => ExpenseModel)
  expense(@Args('id') id: string) {
    return this.expensesService.getExpenseById(id);
  }

  @Mutation(() => ExpenseModel)
  createExpense(
    @Args('input') input: CreateExpenseInput,
    @CurrentUserId() creatorUserId?: string,
  ) {
    return this.expensesService.createExpense(input, creatorUserId);
  }

  @Mutation(() => Boolean)
  deleteExpense(@Args('id') id: string) {
    return this.expensesService.deleteExpense(id);
  }
}
