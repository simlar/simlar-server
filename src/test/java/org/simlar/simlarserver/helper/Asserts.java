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

package org.simlar.simlarserver.helper;

import org.simlar.simlarserver.database.models.SmsProviderLog;

import java.time.Duration;
import java.time.temporal.Temporal;

import static java.lang.Math.abs;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("UtilityClass")
public final class Asserts {
    private Asserts() {
        throw new AssertionError("This class was not meant to be instantiated");
    }

    private static void assertAlmostEquals(final String message, final Temporal expected, final Temporal actual) {

        if (expected == null || actual == null) {
            assertEquals(message, expected, actual);
            return;
        }

        assertTrue(message, abs(Duration.between(expected, actual).getSeconds()) <= 1);
    }

    public static void assertAlmostEquals(final String message, final SmsProviderLog expected, final SmsProviderLog actual) {
        if (expected == null || actual == null) {
            assertSame(message, expected, actual);
            return;
        }

        assertEquals(message, expected.getTelephoneNumber(), actual.getTelephoneNumber());
        assertAlmostEquals(message, expected.getTimestamp(), actual.getTimestamp());
        final String sessionId = actual.getSessionId();
        if (expected.getSessionId() == null) {
            assertNull(message, sessionId);
        } else {
            assertNotNull(message, sessionId);
            assertNotEquals(message, "", sessionId);
        }
        assertAlmostEquals(message, expected.getCallbackTimestamp(), actual.getCallbackTimestamp());
        assertEquals(message, expected.getStatus(), actual.getStatus());
        assertEquals(message, expected.getError(), actual.getError());
        assertEquals(message, expected.getMessage(), actual.getMessage());
    }
}
