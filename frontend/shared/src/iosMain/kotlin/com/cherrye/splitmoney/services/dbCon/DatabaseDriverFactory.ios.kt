package com.cherrye.splitmoney.services.dbCon

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.cherrye.splitmoney.splitMoney

actual class DatabaseDriverFactory {
    actual fun create(): SqlDriver = NativeSqliteDriver(splitMoney.Schema, "splitMoney")
}