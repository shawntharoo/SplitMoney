package com.cherrye.splitmoney.android

import android.app.Application
import com.cherrye.splitmoney.android.services.navigation.NavigationService
import com.cherrye.splitmoney.koin.KoinRuntimeContainer
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
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.koin.ksp.generated.defaultModule

class MainApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        KoinRuntimeContainer.registerRuntimeService(
            defaultModule,
            module {
                single {
                    val driver = DatabaseDriverFactory(androidContext()).create()
                    splitMoney(driver)
                }

                singleOf(::NavigationService) { bind<NavigationCoreServices>() }
                single<UserRepository> { SqlDelightUserRepository(get()) }
                single<GroupRepository> { SqlDelightGroupRepository(get()) }
                single<GroupMembersRepository> { SqlDelightGroupMembersRepository(get()) }
                single<ExpenseRepository> { SqlDelightExpenseRepository(get()) }
                single<ExpenseParticipantsRepository> { SqlDelightExpenseParticipantsRepository(get()) }
            }
        )
    }
}