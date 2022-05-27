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
package com.github.games647.fastlogin.core.MockObjects;

import com.github.games647.fastlogin.core.AsyncScheduler;
import com.github.games647.fastlogin.core.CommonUtil;
import com.github.games647.fastlogin.core.RateLimiter;
import com.github.games647.fastlogin.core.StoredProfile;
import com.github.games647.fastlogin.core.TickingRateLimiter;
import com.github.games647.fastlogin.core.hooks.bedrock.BedrockService;
import com.github.games647.fastlogin.core.shared.FastLoginCore;
import com.github.games647.fastlogin.core.shared.PlatformPlugin;
import com.github.games647.fastlogin.core.storage.SQLiteStorage;
import com.google.common.base.Ticker;
import com.zaxxer.hikari.HikariConfig;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.UUID;

public class MockPlugin implements PlatformPlugin<MockCommandSender> {

    //for testing shared code from core, where the platform (ex. Bukkit or Bungee) doesn't matter

    private static Logger log = LoggerFactory.getLogger("FastLoginTest");
    private FastLoginCore<MockPlayer, MockCommandSender, MockPlugin> core;
    private Configuration config;

    public MockPlugin() throws IOException {
        core = Mockito.mock(FastLoginCore.class);
        Mockito.when(core.getPlugin()).thenReturn(this);
        Mockito.when(core.getPendingLogin()).thenReturn(CommonUtil.buildCache(5, -1));


        // load default config
        File configfile = new File(Objects.requireNonNull(getClass().getResource("/config.yml")).getPath());
        YamlConfiguration provider = (YamlConfiguration) ConfigurationProvider.getProvider(YamlConfiguration.class);
        config = provider.load(configfile);
        Mockito.when(core.getConfig()).thenReturn(config);

        // rate limiter
        RateLimiter rateLimiter = new TickingRateLimiter(Ticker.systemTicker(), 600, 10);
        Mockito.when(core.getRateLimiter()).thenReturn(rateLimiter);

        // storage
        HikariConfig databaseConfig = new HikariConfig();
        databaseConfig.setDriverClassName("org.sqlite.JDBC");
        databaseConfig.setConnectionTimeout(config.getInt("timeout", 30) * 1_000L);
        databaseConfig.setMaxLifetime(config.getInt("lifetime", 30) * 1_000L);

        String databasePath = "{pluginDir}/FastLogin_" + UUID.randomUUID() + ".db";
        SQLiteStorage storage = new SQLiteStorage(core, databasePath, databaseConfig);
        Mockito.when(core.getStorage()).thenReturn(storage);

        try {
            storage.createTables();
        } catch (Exception ex) {
            log.warn("Failed to setup database. Disabling plugin...", ex);
        }
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Path getPluginFolder() {
        return Paths.get(System.getProperty("java.io.tmpdir"));
    }

    public Logger getLog() {
        return log;
    }

    @Override
    public void sendMessage(MockCommandSender receiver, String message) {
    }

    @Override
    public AsyncScheduler getScheduler() {
        return null;
    }

    @Override
    public boolean isPluginInstalled(String name) {
        return false;
    }

    @Override
    public BedrockService<?> getBedrockService() {
        return null;
    }

    public FastLoginCore<MockPlayer, MockCommandSender, MockPlugin> getCore() {
        return core;
    }

    public Configuration getConfig() {
        return config;
    }

}
