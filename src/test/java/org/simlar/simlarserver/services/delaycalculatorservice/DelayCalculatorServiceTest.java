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
import org.junit.runner.RunWith;
import org.simlar.simlarserver.Application;
import org.simlar.simlarserver.utils.SimlarId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
public final class DelayCalculatorServiceTest {
    @SuppressWarnings("CanBeFinal")
    @Autowired
    private DelayCalculatorService delayCalculatorService;

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

    private static Collection<SimlarId> createContacts(final int amount) {
        if (amount <= 0) {
            return Collections.emptyList();
        }

        final Collection<SimlarId> simlarIds = new ArrayList<>();
        for (int i = 0; i < amount; ++i) {
            simlarIds.add(SimlarId.create(String.format("*%d*", i + 1)));
        }

        return simlarIds;
    }

    private void createContactsTest(final List<String> expected, final int amount) {
        assertEquals(expected, createContacts(amount).stream().map(SimlarId::get).collect(Collectors.toList()));
    }

    @Test
    public void createContactsTest() {
        createContactsTest(Collections.emptyList(), -1);
        createContactsTest(Collections.emptyList(), 0);

        createContactsTest(Collections.singletonList("*1*"), 1);
        createContactsTest(Arrays.asList("*1*", "*2*"), 2);
        createContactsTest(Arrays.asList("*1*", "*2*", "*3*", "*4*", "*5*", "*6*", "*7*", "*8*", "*9*", "*10*"), 10);
    }

    @Test
    public void calculateTotalRequestedContactsIncrement() {
        final SimlarId simlarId = SimlarId.create("*0001*");
        final Date now = new Date();

        assertEquals(0, delayCalculatorService.calculateTotalRequestedContacts(simlarId, createContacts(0), now));
        assertEquals(1, delayCalculatorService.calculateTotalRequestedContacts(simlarId, createContacts(1), now));
        assertEquals(1, delayCalculatorService.calculateTotalRequestedContacts(simlarId, createContacts(0), now));
        assertEquals(3, delayCalculatorService.calculateTotalRequestedContacts(simlarId, createContacts(2), now));
        assertEquals(6, delayCalculatorService.calculateTotalRequestedContacts(simlarId, createContacts(3), now));
        assertEquals(8, delayCalculatorService.calculateTotalRequestedContacts(simlarId, createContacts(2), now));
    }

    @Test
    public void calculateTotalRequestedContactsDecreaseAfterADay() {
        final SimlarId simlarId = SimlarId.create("*0002*");
        final Date now = new Date();
        final Date dayAfter = new Date(now.getTime() + 1000 * 60 * 60 * 24 + 1000);

        assertEquals(8, delayCalculatorService.calculateTotalRequestedContacts(simlarId, createContacts(8), now));
        assertEquals(4, delayCalculatorService.calculateTotalRequestedContacts(simlarId, createContacts(4), dayAfter));
    }

    @Test
    public void calculateTotalRequestedContactsSameContactsNoIncrement() {
        final SimlarId simlarId = SimlarId.create("*0003*");
        final SimlarId a = SimlarId.create("*0002*");
        final SimlarId b = SimlarId.create("*0003*");
        final SimlarId c = SimlarId.create("*0004*");
        final Date now = new Date();

        assertEquals(3, delayCalculatorService.calculateTotalRequestedContacts(simlarId, Arrays.asList(a, b, c), now));
        assertEquals(3, delayCalculatorService.calculateTotalRequestedContacts(simlarId, Arrays.asList(b, a, c), now));
        assertEquals(3, delayCalculatorService.calculateTotalRequestedContacts(simlarId, Arrays.asList(c, b, a), now));
        assertEquals(3, delayCalculatorService.calculateTotalRequestedContacts(simlarId, Arrays.asList(a, b, c, a), now));
    }

    @Test
    public void calculateRequestDelay() {
        final SimlarId simlarId = SimlarId.create("*0004*");

        assertEquals(0, delayCalculatorService.calculateRequestDelay(simlarId, createContacts(2000)));
        assertEquals(0, delayCalculatorService.calculateRequestDelay(simlarId, createContacts(2001)));
        assertEquals(1, delayCalculatorService.calculateRequestDelay(simlarId, createContacts(2000)));
        assertEquals(3, delayCalculatorService.calculateRequestDelay(simlarId, createContacts(2001)));
        assertEquals(93, delayCalculatorService.calculateRequestDelay(simlarId, createContacts(10000)));
        assertEquals(93, delayCalculatorService.calculateRequestDelay(simlarId, createContacts(10000)));
        assertEquals(546, delayCalculatorService.calculateRequestDelay(simlarId, createContacts(10001)));
        assertEquals(238440, delayCalculatorService.calculateRequestDelay(simlarId, createContacts(100000)));
    }
}
