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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
        assertFalse(SimlarId.check("1*0001*2"));
        assertFalse(SimlarId.check("*0001*\n*0002*"));
        assertFalse(SimlarId.check("*0001*|*0002*"));
    }

    @Test
    public void testSimlarIdCreate() {
        assertNull(SimlarId.create("*"));
        assertNotNull(SimlarId.create("*0007*"));
    }

    @Test
    public void testCreateWithTelephoneNumber() {
        assertNull(SimlarId.createWithTelephoneNumber(null));
        assertNull(SimlarId.createWithTelephoneNumber(""));
        assertNull(SimlarId.createWithTelephoneNumber("*"));
        assertNull(SimlarId.createWithTelephoneNumber("+"));
        assertNull(SimlarId.createWithTelephoneNumber("+4d"));
        assertNull(SimlarId.createWithTelephoneNumber("491631234567"));

        assertEquals("*49163123456*", SimlarIdHelper.createSimlarId("+49163123456"));
        assertEquals("*491631234567*", SimlarIdHelper.createSimlarId("+491631234567"));
    }

    private static List<SimlarId> parsePipeSeparatedSimlarIdsNotNull(final String str) {
        final List<SimlarId> simlarIds = SimlarId.parsePipeSeparatedSimlarIds(str);
        assertNotNull(simlarIds);
        return simlarIds;
    }

    private static void assertParsePipeSeparatedSimlarIds(final Collection<String> expected, final String str) {
        assertEquals("failed to parse: " + str,
                expected.stream().map(SimlarId::create).collect(Collectors.toList()),
                parsePipeSeparatedSimlarIdsNotNull(str));
    }

    @Test
    public void testParsePipeSeparatedSimlarIdsEmpty() {
        assertParsePipeSeparatedSimlarIds(Collections.emptyList(), null);
        assertParsePipeSeparatedSimlarIds(Collections.emptyList(), "");
        assertParsePipeSeparatedSimlarIds(Collections.emptyList(), "*");
        assertParsePipeSeparatedSimlarIds(Collections.emptyList(), "|||");
        assertParsePipeSeparatedSimlarIds(Collections.emptyList(), "*  *|pp|*");
    }

    @Test
    @SuppressWarnings("TooBroadScope")
    public void testParsePipeSeparatedSimlarIds() {
        final String s1 = "*0001*";
        final String s2 = "*0002*";

        assertParsePipeSeparatedSimlarIds(Collections.singletonList(s1), s1);
        assertParsePipeSeparatedSimlarIds(Collections.singletonList(s1), s1 + '|' + s1);
        assertParsePipeSeparatedSimlarIds(Arrays.asList(s1, s2), s1 + '|' + s2);
        assertParsePipeSeparatedSimlarIds(Arrays.asList(s1, s2), "sdfvbd|"  + s1 + " |" + s2);
        assertParsePipeSeparatedSimlarIds(Arrays.asList(s1, s2), "  "  + s1 + " |" + s2);
        assertParsePipeSeparatedSimlarIds(Arrays.asList(s1, s2), s1 + " | " + s2 + ' ');
        assertParsePipeSeparatedSimlarIds(Arrays.asList(s1, s2), s1 + " | " + s2 + " | sdfas");
        assertParsePipeSeparatedSimlarIds(Arrays.asList(s1, s2), s1 + "| |" + s2 + " | sdfas");
        assertParsePipeSeparatedSimlarIds(Arrays.asList(s2, s1), s2 + '|' + s1);
    }

    private static void assertSortAndUnifySimlarIds(final List<SimlarId> expected, final Collection<SimlarId> input) {
        assertEquals(new TreeSet<>(expected), SimlarId.sortAndUnifySimlarIds(input));
    }

    @SuppressWarnings("StandardVariableNames")
    @Test
    public void sortAndUnifySimlarIds() {
        final SimlarId a  = SimlarId.create("*0001*");
        final SimlarId b  = SimlarId.create("*0002*");
        final SimlarId b2 = SimlarId.create("*0002*");
        final SimlarId c  = SimlarId.create("*0003*");
        final SimlarId d  = SimlarId.create("*0004*");
        final SimlarId e  = SimlarId.create("*0005*");
        final SimlarId f  = SimlarId.create("*0006*");

        assertSortAndUnifySimlarIds(Arrays.asList(a, b), Arrays.asList(a, b));
        assertSortAndUnifySimlarIds(Arrays.asList(a, b), Arrays.asList(b, a));
        assertSortAndUnifySimlarIds(Arrays.asList(a, b, c, d, e, f), Arrays.asList(a, b, c, d, e, f));
        assertSortAndUnifySimlarIds(Arrays.asList(a, b, c, d, e, f), Arrays.asList(f, e, d, c, b, a));
        assertSortAndUnifySimlarIds(Arrays.asList(a, b, c, d, e, f), Arrays.asList(f, b, c, d, e, a));
        assertSortAndUnifySimlarIds(Arrays.asList(a, b, c, d, e, f), Arrays.asList(a, b, b, c, d, e, f));
        assertSortAndUnifySimlarIds(Arrays.asList(a, b, c, d, e, f), Arrays.asList(a, b, b2, c, d, e, f));
        assertSortAndUnifySimlarIds(Arrays.asList(a, b, c, d, e, f), Arrays.asList(a, b, b, b, b, c, c, c, c, c, d, e, f));
    }

    @SuppressWarnings("StandardVariableNames")
    @Test
    public void testHashSimlarIds() {
        final SimlarId a = SimlarId.create("*0001*");
        final SimlarId b = SimlarId.create("*0002*");
        final SimlarId c = SimlarId.create("*0003*");
        final SimlarId d = SimlarId.create("*0004*");
        final SimlarId e = SimlarId.create("*0005*");
        final SimlarId f = SimlarId.create("*0006*");

        assertEquals("2d9f98aeba31139b7ea8efda9e83ec5a5e7e757e0548a98b92588c664343d30b", SimlarId.hashSimlarIds(new TreeSet<>(Arrays.asList(a, b, c, d, e, f))));
        assertEquals("846bf6deae539996486ca444d5517451a4ccea9cbbb339be9b6b1072227c822e", SimlarId.hashSimlarIds(new TreeSet<>(Arrays.asList(a, b, c, d, e))));
        assertEquals("b7f38684102bc67a793ae5cc8013392c61d40c893d8ddce23d78c68266db2c65", SimlarId.hashSimlarIds(new TreeSet<>(Arrays.asList(a, c, d, e))));
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", SimlarId.hashSimlarIds(Collections.emptySortedSet()));
    }
}
