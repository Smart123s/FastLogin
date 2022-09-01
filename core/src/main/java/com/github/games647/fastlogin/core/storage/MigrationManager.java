/*
 * SPDX-License-Identifier: MIT
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2015-2022 games647 and contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.github.games647.fastlogin.core.storage;

import com.github.games647.fastlogin.core.shared.FastLoginCore;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MigrationManager {

    protected static final String MIGRATION_TABLE = "migrations";
    protected static final String CREATE_TABLE_STMT = "CREATE TABLE IF NOT EXISTS `" + MIGRATION_TABLE + "` ("
            + "`ID` INTEGER PRIMARY KEY AUTO_INCREMENT, "
            + "`Table` VARCHAR(32), "
            + "`Version` INTEGER NOT NULL, "
            + "`Date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP "
            + ')';

    protected static final String LOAD_TABLE_VERSION = "SELECT MAX(Version) FROM `" + MIGRATION_TABLE
            + "` WHERE `Table`=? LIMIT 1";

    protected static final String INSERT_MIGRATION = "INSERT INTO `" + MIGRATION_TABLE
            + "` (`Table`, `Version`) " + "VALUES (?, ?) ";

    protected final FastLoginCore<?, ?, ?> core;

    protected final SQLStorage storage;

    protected MigrationManager(FastLoginCore<?, ?, ?> core, SQLStorage storage) {
        this.core = core;
        this.storage = storage;
    }

    protected void createTables() throws SQLException {
        try (Connection con = storage.getDataSource().getConnection();
                Statement createStmt = con.createStatement()) {

            // if (dataSource.getDriverClassName().contains("sqlite")) {
            // throws: the return value of "fastlogin.hikari.HikariDataSource.getDriverClassName()" is null
            if (storage instanceof SQLiteStorage) {
                createStmt.executeUpdate(CREATE_TABLE_STMT.replace("AUTO_INCREMENT", "AUTOINCREMENT"));
            } else {
                createStmt.executeUpdate(CREATE_TABLE_STMT);
            }

        }
    }

    /**
     * Check the current version of a table stored in the connected database
     * @param table the table to check
     * @return the current version
     */
    protected int getCurrentTableVersion(MigratableStorage table) {
        try (Connection con = storage.getDataSource().getConnection();
                PreparedStatement loadStmt = con.prepareStatement(LOAD_TABLE_VERSION)) {
            loadStmt.setString(1, table.getTableName());

            try (ResultSet resultSet = loadStmt.executeQuery()) {
                if (resultSet.next()) {
                    int version = resultSet.getInt(1);

                    // special case: table premium was created before the migration manager
                    if (version == 0 && "premium".equals(table.getTableName()) && table.tableExists()) {
                        version = 1;
                    }

                    return version;
                }
            }
        } catch (SQLException sqlEx) {
            core.getPlugin().getLog().error("Failed to query version of table {}", table, sqlEx);
        }

        return -1;
    }

    protected void migrateTable(MigratableStorage table) {
        int initialVersion = getCurrentTableVersion(table);

        for (int i = initialVersion; i < table.getRequiredVersion(); i++) {
            core.getPlugin().getLog().info("Starting database migration of table {} to version {}",
                    table.getTableName(), i + 1);
            try (Connection con = storage.getDataSource().getConnection();
                    Statement migrateStmt = con.createStatement();
                    PreparedStatement saveStmt = con.prepareStatement(INSERT_MIGRATION);
                ) {
                migrateStmt.executeUpdate(table.getMigrationStatement(i));

                // add entry to migrations table
                saveStmt.setString(1, table.getTableName());
                saveStmt.setInt(2, i + 1);
                saveStmt.executeUpdate();
            } catch (SQLException sqlEx) {
                core.getPlugin().getLog().error("Failed to migrate table {} to version {}",
                        table.getTableName(), i + 1, sqlEx);
                return;
            }
            core.getPlugin().getLog().info("Table {} has been successfully migrated to version {}",
                    table.getTableName(), i + 1);
        }
    }

}
