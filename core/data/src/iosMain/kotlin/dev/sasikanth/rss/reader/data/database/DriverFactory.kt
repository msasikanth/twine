/*
 * Copyright 2023 Sasikanth Miriyampalli
 *
 * Licensed under the GPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 */
package dev.sasikanth.rss.reader.data.database

import app.cash.sqldelight.db.AfterVersion
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import app.cash.sqldelight.driver.native.wrapConnection
import co.touchlab.kermit.Logger
import co.touchlab.sqliter.DatabaseConfiguration
import co.touchlab.sqliter.JournalMode
import dev.sasikanth.rss.reader.data.utils.Constants.APP_GROUP
import dev.sasikanth.rss.reader.di.scopes.AppScope
import kotlinx.cinterop.ExperimentalForeignApi
import me.tatarka.inject.annotations.Inject
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

@Inject
@AppScope
@OptIn(ExperimentalForeignApi::class)
actual class DriverFactory(
  private val codeMigrations: Array<AfterVersion>,
) {

  actual fun createDriver(): SqlDriver {
    migrateAppDatabaseToAppGroup()

    val extendedConfig =
      DatabaseConfiguration.Extended(
        foreignKeyConstraints = true,
        basePath = appGroupDatabasePath()
      )

    return NativeSqliteDriver(
      DatabaseConfiguration(
        name = DB_NAME,
        version = ReaderDatabase.Schema.version.toInt(),
        journalMode = JournalMode.WAL,
        create = { connection -> wrapConnection(connection) { ReaderDatabase.Schema.create(it) } },
        upgrade = { connection, oldVersion, newVersion ->
          wrapConnection(connection) {
            ReaderDatabase.Schema.migrate(
              driver = it,
              oldVersion = oldVersion.toLong(),
              newVersion = newVersion.toLong(),
              callbacks = codeMigrations
            )
          }
        },
        extendedConfig = extendedConfig,
      )
    )
  }

  private fun migrateAppDatabaseToAppGroup() {
    val fileManager = NSFileManager.defaultManager()
    val appDatabasePath = "${appDatabasePath()}/$DB_NAME"

    if (!fileManager.fileExistsAtPath(appDatabasePath)) {
      Logger.d {
        "Skipping DB file migration, no app database file found at location or it's already migrated"
      }
      return
    }

    val groupDatabasePath = "${appGroupDatabasePath()}/$DB_NAME"
    if (fileManager.fileExistsAtPath(groupDatabasePath)) {
      Logger.d { "Skipping DB file migration, app group database file already exists" }
      return
    }

    val appDatabaseWalPath = NSURL.fileURLWithPath("$appDatabasePath-wal")
    val appDatabaseShmPath = NSURL.fileURLWithPath("$appDatabasePath-shm")

    val groupDatabaseWalPath =
      NSURL.fileURLWithPath(groupDatabasePath).URLByAppendingPathComponent("$groupDatabasePath-wal")
        ?: return
    val groupDatabaseSgnPath =
      NSURL.fileURLWithPath(groupDatabasePath).URLByAppendingPathComponent("$groupDatabasePath-shm")
        ?: return

    fileManager.moveItemAtURL(
      srcURL = NSURL.fileURLWithPath(appDatabasePath),
      toURL = NSURL.fileURLWithPath(groupDatabasePath),
      error = null,
    )
    fileManager.moveItemAtURL(
      srcURL = appDatabaseWalPath,
      toURL = groupDatabaseWalPath,
      error = null,
    )
    fileManager.moveItemAtURL(
      appDatabaseShmPath,
      toURL = groupDatabaseSgnPath,
      error = null,
    )
  }

  private fun appDatabasePath(): String {
    val paths =
      NSSearchPathForDirectoriesInDomains(NSApplicationSupportDirectory, NSUserDomainMask, true)
    val supportDirectory = paths[0] as String

    return "$supportDirectory/$DATABASE_PATH"
  }

  private fun appGroupDatabasePath(): String {
    val fileManager = NSFileManager.defaultManager()
    val appGroupPath = fileManager.containerURLForSecurityApplicationGroupIdentifier(APP_GROUP)

    requireNotNull(appGroupPath)

    val dbPath = "${appGroupPath.path()}/$DATABASE_PATH"
    if (!fileManager.fileExistsAtPath(dbPath)) {
      fileManager.createDirectoryAtPath(
        path = dbPath,
        withIntermediateDirectories = true,
        attributes = null,
        error = null,
      )
    }

    return dbPath
  }
}

private const val DATABASE_PATH = "databases"
