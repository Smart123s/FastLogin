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
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;

import java.sql.SQLException;
import java.util.concurrent.ThreadFactory;

public abstract class SQLStorage {

    protected final FastLoginCore<?, ?, ?> core;

    @Getter
    protected final HikariDataSource dataSource;

    private final MigrationManager migrationManager;

    @Getter
    private final SQLAuthStorage authStorage;

    public SQLStorage(FastLoginCore<?, ?, ?> core, HikariConfig config) {
        this.core = core;
        config.setPoolName(core.getPlugin().getName());

        ThreadFactory platformThreadFactory = core.getPlugin().getThreadFactory();
        if (platformThreadFactory != null) {
            config.setThreadFactory(platformThreadFactory);
        }

        this.dataSource = new HikariDataSource(config);

        this.migrationManager = new MigrationManager(core, this);
        this.authStorage = new SQLAuthStorage(core, this);
    }

    public void createTables() throws SQLException {
        migrationManager.createTables();
        migrationManager.migrateTable(authStorage);
    }

    protected abstract String getTableExistsStatement();

    public void close() {
        dataSource.close();
    }
}
