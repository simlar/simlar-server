/*
 * Copyright (C) 2017 The Simlar Authors.
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class LibPhoneNumberTest {
    @Test
    public void testValidPhoneNumbers() {
        assertTrue(LibPhoneNumber.isValid("+15005550001"));
        assertTrue(LibPhoneNumber.isValid("+15005550006"));
        assertTrue(LibPhoneNumber.isValid("+491631234567"));
    }

    @Test
    public void testInvalidPhoneNumbers() {
        assertFalse(LibPhoneNumber.isValid("+49163123456"));
        assertFalse(LibPhoneNumber.isValid("+12345"));
        assertFalse(LibPhoneNumber.isValid("+1"));
        assertFalse(LibPhoneNumber.isValid("491631234567"));
        assertFalse(LibPhoneNumber.isValid("A"));
        assertFalse(LibPhoneNumber.isValid(""));
        assertFalse(LibPhoneNumber.isValid(null));
    }
}
