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

package org.simlar.simlarserver.data;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public final class DeviceTypeTest {
    @Test
    public void testFromInt() {
        assertNull(DeviceType.fromInt(-1));
        assertNull(DeviceType.fromInt(0));
        assertEquals(DeviceType.ANDROID, DeviceType.fromInt(1));
        assertEquals(DeviceType.IOS, DeviceType.fromInt(2));
        assertEquals(DeviceType.IOS_DEVELOPMENT, DeviceType.fromInt(3));
        assertEquals(DeviceType.IOS_VOIP, DeviceType.fromInt(4));
        assertEquals(DeviceType.IOS_VOIP_DEVELOPMENT, DeviceType.fromInt(5));
        assertNull(DeviceType.fromInt(6));
    }

    @Test
    public void testIsIos() {
        assertFalse(DeviceType.ANDROID.isIos());
        assertTrue(DeviceType.IOS.isIos());
        assertTrue(DeviceType.IOS_DEVELOPMENT.isIos());
        assertTrue(DeviceType.IOS_VOIP.isIos());
        assertTrue(DeviceType.IOS_VOIP_DEVELOPMENT.isIos());
    }
}
