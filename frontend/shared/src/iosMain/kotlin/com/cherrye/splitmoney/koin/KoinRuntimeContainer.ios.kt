package com.cherrye.splitmoney.koin

import com.cherrye.splitmoney.repository.impl.SqlDelightExpenseParticipantsRepository
import com.cherrye.splitmoney.repository.impl.SqlDelightExpenseRepository
import com.cherrye.splitmoney.repository.impl.SqlDelightGroupMembersRepository
import com.cherrye.splitmoney.repository.impl.SqlDelightGroupRepository
import com.cherrye.splitmoney.repository.impl.SqlDelightUserRepository
import com.cherrye.splitmoney.repository.interfaces.ExpenseParticipantsRepository
import com.cherrye.splitmoney.repository.interfaces.ExpenseRepository
import com.cherrye.splitmoney.repository.interfaces.GroupMembersRepository
import com.cherrye.splitmoney.repository.interfaces.GroupRepository
import com.cherrye.splitmoney.repository.interfaces.UserRepository
import com.cherrye.splitmoney.services.dbCon.DatabaseDriverFactory
import com.cherrye.splitmoney.services.navigation.NavigationCoreServices
import com.cherrye.splitmoney.splitMoney
import org.koin.core.component.KoinComponent
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

actual class KoinRuntimeContainer {
    actual companion object : KoinComponent {
        actual fun registerRuntimeService(defaultGen: Module, platformModule: Module
        ) {
            startKoin {
                modules(

                )
            }
        }

        actual fun registerSwiftNativeServices (
            defaultGen: Module,
            navigationService: NativeInjectionFactory<NavigationCoreServices>
        ) {
            startKoin {
                      modules(
                          defaultGen,
                          module {
                              single { DatabaseDriverFactory() }
                              single { splitMoney(get<DatabaseDriverFactory>().create()) }
                              single { navigationService() }
                              single<UserRepository> { SqlDelightUserRepository(get()) }
                              single<GroupRepository> { SqlDelightGroupRepository(get()) }
                              single<GroupMembersRepository> { SqlDelightGroupMembersRepository(get()) }
                              single<ExpenseRepository> { SqlDelightExpenseRepository(get()) }
                              single<ExpenseParticipantsRepository> { SqlDelightExpenseParticipantsRepository(get()) }
                          }
                      )
            }
        }
    }
}