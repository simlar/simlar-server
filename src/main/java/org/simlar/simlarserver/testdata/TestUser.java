/*
 * Copyright (C) 2016 The Simlar Authors.
 *
 * This file is part of Simlar. (https://www.simlar.org)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 */

package org.simlar.simlarserver.testdata;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class TestUser {
    public static final String SIMLAR_ID_NOT_REGISTERED = "*0003*";
    public static final List<TestUser> USERS = Collections.unmodifiableList(Arrays.asList(
            new TestUser("*0001*", "x1fg6hk78", "5c3d66f5a3928cca2821d711a2c016bb"),
            new TestUser("*0002*", "fdfho21j3", "6e5a610112a88a001e14fb4bce3fd8af")
    ));

    private final String simlarId;
    private final String password;
    private final String passwordHash;

    private TestUser(final String simlarId, final String password, final String passwordHash) {
        this.simlarId     = simlarId;
        this.password     = password;
        this.passwordHash = passwordHash;
    }

    public static TestUser get(final int i) {
        return USERS.get(i);
    }

    public String getSimlarId() {
        return simlarId;
    }

    public String getPassword() {
        return password;
    }

    public String getPasswordHash() {
        return passwordHash;
    }
}
