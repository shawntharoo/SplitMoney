package com.cherrye.splitmoney.services.dbCon

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.cherrye.splitmoney.splitMoney

actual class DatabaseDriverFactory(private val context: Context) {
    actual fun create(): SqlDriver =
        AndroidSqliteDriver(splitMoney.Schema, context, "splitMoney")
}