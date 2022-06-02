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
package com.github.games647.fastlogin.core.shared;

import com.github.games647.fastlogin.core.FastLoginCoreTest;
import com.github.games647.fastlogin.core.MockObjects.MockLoginSource;
import com.github.games647.fastlogin.core.StoredProfile;
import com.github.games647.fastlogin.core.shared.event.FastLoginPreLoginEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

public class JoinManagementTest extends FastLoginCoreTest {

    private JoinManagement jm;

    @Override
    @BeforeEach
    public void setUp() throws IOException {
        super.setUp();
        JoinManagement jmInstance = new JoinManagement(plugin.getCore(), null, null) {
            @Override
            public FastLoginPreLoginEvent callFastLoginPreLoginEvent(String username, LoginSource source, StoredProfile profile) {
                return null;
            }

            @Override
            public void requestPremiumLogin(LoginSource source, StoredProfile profile, String username, boolean registered) {

            }

            @Override
            public void startCrackedSession(LoginSource source, StoredProfile profile, String username) {

            }
        };

        this.jm = Mockito.spy(jmInstance);
    }

    @Test
    public void joinCrackedWithDefaultConfig() {
        String username = "newPlayer";
        MockLoginSource source = new MockLoginSource();

        jm.onLogin(username, source);
        verify(jm).startCrackedSession(eq(source), any(), eq(username));
    }

    @Test
    public void joinCrackedWithPremiumName() throws UnknownHostException {
        String username = "knownPremium1";
        byte[] ip = {0, 0, 1, 1};
        InetSocketAddress address = new InetSocketAddress(InetAddress.getByAddress(ip), 25567);
        MockLoginSource source = new MockLoginSource(address);

        plugin.getConfig().set("nameChangeCheck", true);
        plugin.getConfig().set("autoRegister", true);

        StoredProfile profile = plugin.getCore().getStorage().loadProfile(username);

        jm.onLogin(username, source);
        verify(jm).requestPremiumLogin(source, profile, username, true);
    }
}
