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

package org.simlar.simlarserver.services.delaycalculatorservice;

import org.junit.Test;

import java.text.NumberFormat;

import static org.junit.Assert.assertTrue;

public final class DelayCalculatorServiceTest {
    private String formatInt(final int i) {
        return NumberFormat.getIntegerInstance().format(i);
    }

    private void calculateDelayTest(final int minSeconds, final int count, final int maxSeconds) {
        final int resultDelay = DelayCalculatorService.calculateDelay(count);
        final String result = "calculateDelay(" + formatInt(count) + ") = " + formatInt(resultDelay);
        assertTrue(formatInt(minSeconds) + " <= " + result, minSeconds <= resultDelay);
        assertTrue(result + " <= " + formatInt(maxSeconds), resultDelay <= maxSeconds);
    }

    @Test
    public void calculateDelay() {
        calculateDelayTest(0,                       0,                   0);
        calculateDelayTest(0,                       100,                 0);
        calculateDelayTest(0,                       1000,                0);
        calculateDelayTest(0,                       2000,                1);
        calculateDelayTest(0,                       5000,                1);
        calculateDelayTest(1,                       6000,                2);
        calculateDelayTest(3,                       8000,                6);
        calculateDelayTest(5,                       10000,               8);
        calculateDelayTest(24 * 60 * 60,            100000,              24 * 60 * 60 * 10);
        calculateDelayTest(5 * 364 * 24 * 60 * 60,  100000000,           Integer.MAX_VALUE);
        calculateDelayTest(Integer.MAX_VALUE,       Integer.MAX_VALUE,   Integer.MAX_VALUE);
        calculateDelayTest(Integer.MAX_VALUE,       -1,                  Integer.MAX_VALUE);
        calculateDelayTest(Integer.MAX_VALUE,       -1000,               Integer.MAX_VALUE);
        calculateDelayTest(Integer.MAX_VALUE,       Integer.MIN_VALUE,   Integer.MAX_VALUE);
    }
}
