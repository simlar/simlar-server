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

package org.simlar.simlarserver.utils;

import org.junit.Test;

import java.time.Duration;

import static org.junit.Assert.assertEquals;

public final class ComparablesTest {
    private static <T extends Comparable<? super T>> void assertMaxFirst(final T max, final T min) {
        assertEquals(max, Comparables.max(max, min));
        assertEquals(max, Comparables.max(min, max));
    }

    @Test
    public void testMaxWithDuration() {
        assertMaxFirst(null, null);
        assertMaxFirst(Duration.ofDays(1), null);
        assertMaxFirst(Duration.ofDays(1), Duration.ofMinutes(2));
        assertMaxFirst(Duration.ofSeconds(61), Duration.ofMinutes(1));
    }
}
