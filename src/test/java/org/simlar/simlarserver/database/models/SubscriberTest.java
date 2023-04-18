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

package org.simlar.simlarserver.database.models;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public final class SubscriberTest {
    @Test
    public void testCreateHashHa1() {
        assertEquals("1a4e8a6d2bdc2864d4c4164ba47391cb", Subscriber.createHashHa1(null, null, null));
        assertEquals("3d2c2bbe810e8510bd7f051d80f5a85e", Subscriber.createHashHa1("*2342*", "", "53cur3"));
        assertEquals("c7e7a2b130cb4398a3682559a498c025", Subscriber.createHashHa1("*2342*", "", "53cur4"));
        assertEquals("5abdc6343ca3b039fc5ae1c1c68b22ab", Subscriber.createHashHa1("*2343*", "", "53cur4"));
    }

    @Test
    public void testCreateHashHa1b() {
        assertEquals("dd336acb901359d7f13bb823dbc95607", Subscriber.createHashHa1b(null, null, null));
        assertEquals("c832b701c3840182da6bf33b3c588aa4", Subscriber.createHashHa1b("*2342*", "", "53cur3"));
        assertEquals("b66abb6dc2a30f94380e0be4d87dcd38", Subscriber.createHashHa1b("*2342*", "", "53cur4"));
        assertEquals("facc6b39f73e54ea33d8141804c1f951", Subscriber.createHashHa1b("*2343*", "", "53cur4"));
    }
}
