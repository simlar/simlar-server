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
import org.simlar.simlarserver.SimlarServer;
import org.simlar.simlarserver.helper.SimlarIds;
import org.simlar.simlarserver.utils.SimlarId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SimlarServer.class)
public final class DelayCalculatorServiceTest {
    @Autowired
    private DelayCalculatorService delayCalculatorService;

    private static void assertCalculateDelay(final Duration min, final int count, final Duration max) {
        final Duration resultDelay = DelayCalculatorService.calculateDelay(count);
        final String result = "assertCalculateDelay(" + count + ") = " + resultDelay;
        assertTrue(min + " <= " + result, resultDelay.compareTo(min) >= 0);
        assertTrue(result + " <= " + max, resultDelay.compareTo(max) <= 0);
    }

    @Test
    public void testCalculateDelay() {
        assertCalculateDelay(Duration.ZERO,                   0,                  Duration.ZERO);
        assertCalculateDelay(Duration.ZERO,                   100,                Duration.ZERO);
        assertCalculateDelay(Duration.ZERO,                   1000,               Duration.ZERO);
        assertCalculateDelay(Duration.ZERO,                   2000,               Duration.ofSeconds(1));
        assertCalculateDelay(Duration.ZERO,                   5000,               Duration.ofSeconds(1));
        assertCalculateDelay(Duration.ofSeconds(1),           6000,               Duration.ofSeconds(2));
        assertCalculateDelay(Duration.ofSeconds(3),           8000,               Duration.ofSeconds(6));
        assertCalculateDelay(Duration.ofSeconds(5),           10000,              Duration.ofSeconds(8));
        assertCalculateDelay(Duration.ofDays(1),              100000,             Duration.ofDays(10));
        assertCalculateDelay(Duration.ofDays(5 * 364),        100000000,          DelayCalculatorService.MAXIMUM);
        assertCalculateDelay(DelayCalculatorService.MAXIMUM,  Integer.MAX_VALUE,  DelayCalculatorService.MAXIMUM);
        assertCalculateDelay(DelayCalculatorService.MAXIMUM,  -1,                 DelayCalculatorService.MAXIMUM);
        assertCalculateDelay(DelayCalculatorService.MAXIMUM,  -1000,              DelayCalculatorService.MAXIMUM);
        assertCalculateDelay(DelayCalculatorService.MAXIMUM,  Integer.MIN_VALUE,  DelayCalculatorService.MAXIMUM);
    }

    @Test
    public void testCalculateTotalRequestedContactsIncrement() {
        final SimlarId simlarId = SimlarId.create("*0007*");
        final Instant now = Instant.now();

        assertEquals(0, delayCalculatorService.calculateTotalRequestedContacts(simlarId, SimlarIds.createContacts(0), now));
        assertEquals(1, delayCalculatorService.calculateTotalRequestedContacts(simlarId, SimlarIds.createContacts(1), now));
        assertEquals(1, delayCalculatorService.calculateTotalRequestedContacts(simlarId, SimlarIds.createContacts(0), now));
        assertEquals(3, delayCalculatorService.calculateTotalRequestedContacts(simlarId, SimlarIds.createContacts(2), now));
        assertEquals(6, delayCalculatorService.calculateTotalRequestedContacts(simlarId, SimlarIds.createContacts(3), now));
        assertEquals(8, delayCalculatorService.calculateTotalRequestedContacts(simlarId, SimlarIds.createContacts(2), now));
    }

    @Test
    public void testCalculateTotalRequestedContactsDecreaseAfterADay() {
        final SimlarId simlarId = SimlarId.create("*0008*");
        final Instant now = Instant.now();
        final Instant dayAfter = Instant.now().plus(Duration.ofDays(1)).plus(Duration.ofSeconds(2));

        assertEquals(8, delayCalculatorService.calculateTotalRequestedContacts(simlarId, SimlarIds.createContacts(8), now));
        assertEquals(4, delayCalculatorService.calculateTotalRequestedContacts(simlarId, SimlarIds.createContacts(4), dayAfter));
    }

    @SuppressWarnings("StandardVariableNames")
    @Test
    public void testCalculateTotalRequestedContactsSameContactsNoIncrement() {
        final SimlarId simlarId = SimlarId.create("*0009*");
        final SimlarId a = SimlarId.create("*0002*");
        final SimlarId b = SimlarId.create("*0003*");
        final SimlarId c = SimlarId.create("*0004*");
        final Instant now = Instant.now();

        assertEquals(3, delayCalculatorService.calculateTotalRequestedContacts(simlarId, Arrays.asList(a, b, c), now));
        assertEquals(3, delayCalculatorService.calculateTotalRequestedContacts(simlarId, Arrays.asList(b, a, c), now));
        assertEquals(3, delayCalculatorService.calculateTotalRequestedContacts(simlarId, Arrays.asList(c, b, a), now));
        assertEquals(3, delayCalculatorService.calculateTotalRequestedContacts(simlarId, Arrays.asList(a, b, c, a), now));
    }

    @Test
    public void testCalculateRequestDelay() {
        final SimlarId simlarId = SimlarId.create("*0004*");

        assertEquals(Duration.ZERO,              delayCalculatorService.calculateRequestDelay(simlarId, SimlarIds.createContacts(2000)));
        assertEquals(Duration.ZERO,              delayCalculatorService.calculateRequestDelay(simlarId, SimlarIds.createContacts(2001)));
        assertEquals(Duration.ofSeconds(1),      delayCalculatorService.calculateRequestDelay(simlarId, SimlarIds.createContacts(2000)));
        assertEquals(Duration.ofSeconds(3),      delayCalculatorService.calculateRequestDelay(simlarId, SimlarIds.createContacts(2001)));
        assertEquals(Duration.ofSeconds(93),     delayCalculatorService.calculateRequestDelay(simlarId, SimlarIds.createContacts(10000)));
        assertEquals(Duration.ofSeconds(93),     delayCalculatorService.calculateRequestDelay(simlarId, SimlarIds.createContacts(10000)));
        assertEquals(Duration.ofSeconds(546),    delayCalculatorService.calculateRequestDelay(simlarId, SimlarIds.createContacts(10001)));
        assertEquals(Duration.ofSeconds(238440), delayCalculatorService.calculateRequestDelay(simlarId, SimlarIds.createContacts(100000)));
    }

    @Test
    public void testCalculateRequestDelayOneContactPermission() {
        /// This test simulates a user removing contacts permission and granting it again.
        final SimlarId simlarId = SimlarId.create("*0005*");
        final Instant now = Instant.now();

        assertEquals(0, delayCalculatorService.calculateTotalRequestedContacts(simlarId, SimlarIds.createContacts(0), now));
        assertEquals(100, delayCalculatorService.calculateTotalRequestedContacts(simlarId, SimlarIds.createContacts(100), now));
        assertEquals(101, delayCalculatorService.calculateTotalRequestedContacts(simlarId, SimlarIds.createContacts(1), now));
        assertEquals(101, delayCalculatorService.calculateTotalRequestedContacts(simlarId, SimlarIds.createContacts(100), now));
    }

    @Test
    public void testCalculateRequestDelayOneContactInTelephoneBook() {
        final SimlarId simlarId = SimlarId.create("*0006*");
        final Instant now = Instant.now();

        assertEquals(1, delayCalculatorService.calculateTotalRequestedContacts(simlarId, SimlarIds.createContacts(1), now));
        assertEquals(1, delayCalculatorService.calculateTotalRequestedContacts(simlarId, SimlarIds.createContacts(1), now));
        assertEquals(1, delayCalculatorService.calculateTotalRequestedContacts(simlarId, SimlarIds.createContacts(1), now));
        assertEquals(1, delayCalculatorService.calculateTotalRequestedContacts(simlarId, SimlarIds.createContacts(1), now));
    }
}
