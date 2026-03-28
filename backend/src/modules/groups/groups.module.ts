import { Module } from '@nestjs/common';
import { UsersModule } from '../users/users.module';
import { GroupsResolver } from './groups.resolver';
import { GroupsService } from './groups.service';

@Module({
  imports: [UsersModule],
  providers: [GroupsResolver, GroupsService],
  exports: [GroupsService],
})
export class GroupsModule {}
