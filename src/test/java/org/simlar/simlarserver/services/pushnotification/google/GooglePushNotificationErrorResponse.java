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

package org.simlar.simlarserver.services.pushnotification.google;

import java.util.List;

@SuppressWarnings("UtilityClass")
final class GooglePushNotificationErrorResponse {
    private GooglePushNotificationErrorResponse() {
        throw new AssertionError("This class was not meant to be instantiated");
    }

    @SuppressWarnings("StaticCollection")
    static final List<String> INVALID_TOKENS = List.of(
            "{\n" +
            "  \"error\": {\n" +
            "    \"code\": 400,\n" +
            "    \"message\": \"The registration token is not a valid FCM registration token\",\n" +
            "    \"status\": \"INVALID_ARGUMENT\",\n" +
            "    \"details\": [\n" +
            "      {\n" +
            "        \"@type\": \"type.googleapis.com/google.firebase.fcm.v1.FcmError\",\n" +
            "        \"errorCode\": \"INVALID_ARGUMENT\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"@type\": \"type.googleapis.com/google.rpc.BadRequest\",\n" +
            "        \"fieldViolations\": [\n" +
            "          {\n" +
            "            \"field\": \"message.token\",\n" +
            "            \"description\": \"The registration token is not a valid FCM registration token\"\n" +
            "          }\n" +
            "        ]\n" +
            "      }\n" +
            "    ]\n" +
            "  }\n" +
            "}\n",
            "{\n" +
            "  \"error\": {\n" +
            "    \"code\": 400,\n" +
            "    \"message\": \"The registration token is not a valid FCM registration token\",\n" +
            "    \"status\": \"INVALID_ARGUMENT\",\n" +
            "    \"details\": [\n" +
            "      {\n" +
            "        \"@type\": \"type.googleapis.com/google.firebase.fcm.v1.FcmError\",\n" +
            "        \"errorCode\": \"INVALID_ARGUMENT\"\n" +
            "      }\n" +
            "    ]\n" +
            "  }\n" +
            "}\n");
}
