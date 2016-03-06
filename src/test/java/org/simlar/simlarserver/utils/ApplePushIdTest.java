/*
 * Copyright (C) 2015 The Simlar Authors.
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

package org.simlar.simlarserver.utils;

import org.junit.Test;

import static org.junit.Assert.*;

@SuppressWarnings("SpellCheckingInspection")
public final class ApplePushIdTest {
    @Test
    public void testApplePushIdCheckSuccess() {
        assertTrue(ApplePushId.check("7fd224670ab46d041e08101cd2bc3a5646c252a1dd5bfcb02f667203338f89a9"));
        assertTrue(ApplePushId.check("7fd224670ab46d041e08101cd2bc3a5646c252aDdd5bfcb02f6A7203338f89a9"));
        assertTrue(ApplePushId.check("7fd224670ab46d041e08101cd2bc3a5646c252a1dd5bfcb02f667203338FFFFF"));
        assertTrue(ApplePushId.check("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"));
    }

    @Test
    public void testApplePushIdCheckFail() {
        assertFalse(ApplePushId.check(null));
        assertFalse(ApplePushId.check(""));

        assertFalse(ApplePushId.check("*"));
        assertFalse(ApplePushId.check("7fd224670ab46d041e08101cd2bc3a5646c252a1dd5bfcb02f667203338f89a?"));
        assertFalse(ApplePushId.check("7fd224670ab46d041e08101cd2bc3a5646c252a1dd5bfcb02f667203338f89a"));
        assertFalse(ApplePushId.check("7fd224670ab46d041e08101cd2bc3a5646c252a1dd5bfcb02f667203338f89a9a"));
        assertFalse(ApplePushId.check("7fd2-4670ab46d041e08101cd2bc3a5646c252a1dd5bfcb02f667203338f89a9"));
        assertFalse(ApplePushId.check("7fd2g4670ab46d041e08101cd2bc3a5646c252a1dd5bfcb02f667203338f89a9"));
        assertFalse(ApplePushId.check("7fd2G4670ab46d041e08101cd2bc3a5646c252a1dd5bfcb02f667203338f89a9"));
        assertFalse(ApplePushId.check("7fd224670ab46d041e08101cd2bc3a5646c252a1dd5bfcb02f667203338XXXXX"));
    }

    @Test
    public void testApplePushIdCreate() {
        assertNull(ApplePushId.create(""));
        assertNotNull(ApplePushId.create("7fd224670ab46d041e08101cd2bc3a5646c252a1dd5bfcb02f667203338f89a9"));
    }
}
