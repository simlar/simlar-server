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
    static final List<String> INVALID_TOKENS = List.of("""
            {
              "error": {
                "code": 400,
                "message": "The registration token is not a valid FCM registration token",
                "status": "INVALID_ARGUMENT",
                "details": [
                  {
                    "@type": "type.googleapis.com/google.firebase.fcm.v1.FcmError",
                    "errorCode": "INVALID_ARGUMENT"
                  },
                  {
                    "@type": "type.googleapis.com/google.rpc.BadRequest",
                    "fieldViolations": [
                      {
                        "field": "message.token",
                        "description": "The registration token is not a valid FCM registration token"
                      }
                    ]
                  }
                ]
              }
            }
            """, """
            {
              "error": {
                "code": 400,
                "message": "The registration token is not a valid FCM registration token",
                "status": "INVALID_ARGUMENT",
                "details": [
                  {
                    "@type": "type.googleapis.com/google.firebase.fcm.v1.FcmError",
                    "errorCode": "INVALID_ARGUMENT"
                  }
                ]
              }
            }
            """);
}
