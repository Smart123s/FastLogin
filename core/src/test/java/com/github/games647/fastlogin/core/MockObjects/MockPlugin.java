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
import com.github.games647.fastlogin.core.RateLimiter;
import com.github.games647.fastlogin.core.TickingRateLimiter;
import com.github.games647.fastlogin.core.hooks.bedrock.BedrockService;
import com.github.games647.fastlogin.core.shared.FastLoginCore;
import com.github.games647.fastlogin.core.shared.PlatformPlugin;
import com.google.common.base.Ticker;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

public class MockPlugin implements PlatformPlugin<MockCommandSender> {

    //for testing shared code from core, where the platform (ex. Bukkit or Bungee) doesn't matter

    private static final Logger log = LoggerFactory.getLogger("FastLoginTest");
    private final FastLoginCore<MockPlayer, MockCommandSender, MockPlugin> core;
    private final Configuration config;

    public MockPlugin() throws IOException {
        core = Mockito.spy(new FastLoginCore<>(this));

        // load default config
        File configfile = new File(Objects.requireNonNull(getClass().getResource("/config.yml")).getPath());
        YamlConfiguration provider = (YamlConfiguration) ConfigurationProvider.getProvider(YamlConfiguration.class);
        config = provider.load(configfile);

        // rate limiter
        RateLimiter rateLimiter = new TickingRateLimiter(Ticker.systemTicker(), 600, 10);
        Mockito.when(core.getRateLimiter()).thenReturn(rateLimiter);
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Path getPluginFolder() {
        return null;
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
