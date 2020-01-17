/*
 * Copyright (C) 2019 The Simlar Authors.
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

package org.simlar.simlarserver.services.pushnotification.google.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public final class GooglePushNotificationResponseTest {

    @Test
    public void testDeserializationPrettyPrint() throws JsonProcessingException {

        final String str = "{\n" +
                "  \"name\": \"projects/simlar-org/messages/0:1572168901680225%09814fb0002e7a5e\"\n" +
                "}\n";

        assertNotNull(new ObjectMapper().readValue(str, GooglePushNotificationResponse.class));
    }

    @Test
    public void testDeserialization3() throws JsonProcessingException {
        assertEquals("newName", new ObjectMapper().readValue("{\"name\":\"newName\"}", GooglePushNotificationResponse.class).getName());
    }

    @Test
    public void testSerialization() throws JsonProcessingException {
        assertEquals("{\"name\":\"newName\"}", new ObjectMapper().writeValueAsString(new GooglePushNotificationResponse("newName")));
    }
}
