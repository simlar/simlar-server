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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

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

    private List<SimlarId> parsePipeSeparatedSimlarIdsNotNull(final String str) {
        final List<SimlarId> simlarIds = SimlarId.parsePipeSeparatedSimlarIds(str);
        assertNotNull(simlarIds);
        return simlarIds;
    }

    @Test
    public void testParsePipeSeparatedSimlarIdsEmpty() {
        assertEquals(0, parsePipeSeparatedSimlarIdsNotNull(null).size());
        assertEquals(0, parsePipeSeparatedSimlarIdsNotNull("").size());
        assertEquals(0, parsePipeSeparatedSimlarIdsNotNull("*").size());
        assertEquals(0, parsePipeSeparatedSimlarIdsNotNull("|||").size());
        assertEquals(0, parsePipeSeparatedSimlarIdsNotNull("*  *|pp|*").size());
        //assertEquals(Collections.EMPTY_LIST, parsePipeSeparatedSimlarIdsNotNull("*1*|pp|*"));
    }

    private List<SimlarId> createSimlarIds(final List<String> simlarIds) {
        final List<SimlarId> retVal = new ArrayList<>();
        for (final String simlarIdStr : simlarIds) {
            retVal.add(SimlarId.create(simlarIdStr));
        }
        return retVal;
    }

    private void compareSimlarIds(final List<String> expected, final String str) {
        assertEquals("failed to parse: " + str, createSimlarIds(expected), parsePipeSeparatedSimlarIdsNotNull(str));
    }

    @SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
    @Test
    public void testParsePipeSeparatedSimlarIds() {
        final String s1 = "*0001*";
        final String s2 = "*0002*";
        compareSimlarIds(Arrays.asList(s1), s1);
        compareSimlarIds(Arrays.asList(s1), s1 + "|" + s1);
        compareSimlarIds(Arrays.asList(s1, s2), s1 + "|" + s2);
        compareSimlarIds(Arrays.asList(s1, s2), "sdfvbd|"  + s1 + " |" + s2);
        compareSimlarIds(Arrays.asList(s1, s2), "  "  + s1 + " |" + s2);
        compareSimlarIds(Arrays.asList(s1, s2), s1 + " | " + s2 + " ");
        compareSimlarIds(Arrays.asList(s1, s2), s1 + " | " + s2 + " | sdfas");
        compareSimlarIds(Arrays.asList(s1, s2), s1 + "| |" + s2 + " | sdfas");
        compareSimlarIds(Arrays.asList(s2, s1), s2 + "|" + s1);
    }
}
