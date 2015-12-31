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

package org.simlar.simlarserver;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class SimlarIdTest {
    @Test
    public void testSimlarIdCheckSuccess() {
        assertTrue(SimlarId.check("*0001*"));
        assertTrue(SimlarId.check("*4912345678*"));
        assertTrue(SimlarId.check("*491234567890123*"));
    }

    @Test
    public void testSimlarIdCheckFail() {
        assertFalse(SimlarId.check(null));
        assertFalse(SimlarId.check(""));

        assertFalse(SimlarId.check("*"));
        assertFalse(SimlarId.check("**"));
        assertFalse(SimlarId.check("0001"));
        assertFalse(SimlarId.check("0001*"));
        assertFalse(SimlarId.check("*0001"));
        assertFalse(SimlarId.check("*00*01*"));
        assertFalse(SimlarId.check("*00a01*"));
        assertFalse(SimlarId.check("A*0001*"));
        assertFalse(SimlarId.check("*0001*b"));
        assertFalse(SimlarId.check("*0001* "));
        assertFalse(SimlarId.check(" *0001*"));
        assertFalse(SimlarId.check("*0001 *"));
        assertFalse(SimlarId.check(" *0001* "));
    }

    @Test
    public void testSimlarIdCreate() {
        assertNull(SimlarId.create("*"));
        assertNotNull(SimlarId.create("*0007*"));
    }
}
