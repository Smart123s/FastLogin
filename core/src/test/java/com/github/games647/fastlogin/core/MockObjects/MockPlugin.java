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

import com.github.games647.craftapi.resolver.MojangResolver;
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
import java.util.concurrent.ConcurrentMap;

import static org.mockito.Mockito.lenient;

public class MockPlugin implements PlatformPlugin<MockCommandSender> {

    //for testing shared code from core, where the platform (ex. Bukkit or Bungee) doesn't matter

    private static Logger log = LoggerFactory.getLogger("FastLoginTest");
    private FastLoginCore<MockPlayer, MockCommandSender, MockPlugin> core;
    private Configuration config;

    public MockPlugin() throws IOException {
        core = Mockito.mock(FastLoginCore.class);
        lenient().when(core.getPlugin()).thenReturn(this);

        // pendingLogin
        ConcurrentMap<String, Object> pendingLogin = CommonUtil.buildCache(5, -1);
        lenient().when(core.getPendingLogin()).thenReturn(pendingLogin);

        // load default config
        File configfile = new File(Objects.requireNonNull(getClass().getResource("/config.yml")).getPath());
        YamlConfiguration provider = (YamlConfiguration) ConfigurationProvider.getProvider(YamlConfiguration.class);
        config = provider.load(configfile);
        lenient().when(core.getConfig()).thenReturn(config);

        // rate limiter
        RateLimiter rateLimiter = new TickingRateLimiter(Ticker.systemTicker(), 600, 10);
        lenient().when(core.getRateLimiter()).thenReturn(rateLimiter);

        // resolver (fake Mojang API)
        MojangResolver resolver = new MockResolver();
        lenient().when(core.getResolver()).thenReturn(resolver);

        // storage
        HikariConfig databaseConfig = new HikariConfig();
        databaseConfig.setDriverClassName("org.sqlite.JDBC");
        databaseConfig.setConnectionTimeout(config.getInt("timeout", 30) * 1_000L);
        databaseConfig.setMaxLifetime(config.getInt("lifetime", 30) * 1_000L);

        String databasePath = "{pluginDir}/FastLogin_" + UUID.randomUUID() + ".db";
        SQLiteStorage storage = new SQLiteStorage(core, databasePath, databaseConfig);
        lenient().when(core.getStorage()).thenReturn(storage);

        try {
            storage.createTables();
        } catch (Exception ex) {
            log.warn("Failed to setup database. Disabling plugin...", ex);
        }

        // add mock players to database
        storage.save(new StoredProfile(null, "knownCracked1", false, "0.0.1.1"));
        storage.save(new StoredProfile(null, "knownCracked2", false, "0.0.1.2"));
        storage.save(new StoredProfile(UUID.fromString("6e5480fd-d50e-4f60-83fc-8be8d81ff2d3"), "knownPremium1", true, "0.0.1.1"));
        storage.save(new StoredProfile(UUID.fromString("800308bf-8ef2-472c-8886-23b5be8e522a"), "knownPremium2", true, "0.0.1.1"));
        storage.save(new StoredProfile(UUID.fromString("f1db843f-8d5a-4c72-82a6-4da597184816"), "changedName1", true, "0.0.2.1"));
        // cracked player with an existing premium accounts name
        storage.save(new StoredProfile(null, "crackPremiumName1", false, "0.0.3.1"));

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
