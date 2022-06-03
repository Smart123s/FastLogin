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

import com.github.games647.craftapi.model.NameHistory;
import com.github.games647.craftapi.model.Profile;
import com.github.games647.craftapi.model.skin.SkinProperty;
import com.github.games647.craftapi.resolver.MojangResolver;
import com.github.games647.craftapi.resolver.ProfileResolver;
import com.github.games647.craftapi.resolver.RateLimitException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public class MockResolver extends MojangResolver implements ProfileResolver {
    @Override
    public ImmutableSet<Profile> findProfiles(String... names) throws IOException, RateLimitException {
        return null;
    }

    @Override
    public Optional<Profile> findProfile(String name) throws IOException, RateLimitException {
        UUID uuid;
        switch (name) {
            case "knwonPremium1":
                uuid = UUID.fromString("6e5480fd-d50e-4f60-83fc-8be8d81ff2d3");
                return Optional.of(new Profile(uuid, "knownPremium1"));
            case "knwonPremium2":
                uuid = UUID.fromString("800308bf-8ef2-472c-8886-23b5be8e522a");
                return Optional.of(new Profile(uuid, "knownPremium2"));
            case "crackPremiumName1":
                uuid = UUID.fromString("983a36a7-13e8-496a-bfbc-22341600a59d");
                return Optional.of(new Profile(uuid, "crackPremiumName1"));
            case "crackPremiumName3":
                uuid = UUID.fromString("5d188bb3-975a-4ca1-8fe8-b61dff3a2b4a");
                return Optional.of(new Profile(uuid, "crackPremiumName3"));
            default:
                return Optional.empty();
        }
    }

    @Override
    public Optional<Profile> findProfile(String name, Instant time) throws IOException, RateLimitException {
        return Optional.empty();
    }

    @Override
    public ImmutableList<NameHistory> findNames(UUID uuid) throws IOException {
        return null;
    }

    @Override
    public Optional<SkinProperty> downloadSkin(UUID uuid) throws IOException, RateLimitException {
        return Optional.empty();
    }
}
