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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class HashTest {
    @Test
    public void testMd5() {
        assertEquals("", Hash.md5(null));
        assertEquals("d41d8cd98f00b204e9800998ecf8427e", Hash.md5(""));
        assertEquals("111483f4a3a6166cc4a1cc4fac633f72", Hash.md5("*0001*:sip.simlar.org:xldfdsf3er4ferf"));
        assertEquals("f5dde2f01afd1bde593063832c84e5e1", Hash.md5("*0002*:sip.simlar.org:fgklgor4223"));
        assertEquals("9d58ebf2d3c31b4c0b3d3411e5a7e237", Hash.md5("*0001*@sip.simlar.org:sip.simlar.org:dfk4kgo34k"));
        assertEquals("9f7917d320a27ac068b56019aa11ba6a", Hash.md5("*0002*@sip.simlar.org:sip.simlar.org:sdp45p6hpplk"));
        assertEquals("1ee89de73dccb07194b19a25fdfad653", Hash.md5("*0001*::sp4mv02fvu"));
    }
}
