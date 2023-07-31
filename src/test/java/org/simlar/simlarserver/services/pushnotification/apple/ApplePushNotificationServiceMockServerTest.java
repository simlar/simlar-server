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

package org.simlar.simlarserver.services.pushnotification.apple;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.JsonBody;
import org.simlar.simlarserver.services.pushnotification.apple.json.ApplePushNotificationRequestCaller;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.time.Instant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@SuppressFBWarnings("UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR")
@SuppressWarnings({"IOResourceOpenedButNotSafelyClosed", "resource"}) // MockServerClient used as documented
public final class ApplePushNotificationServiceMockServerTest {
    private ApplePushNotificationService applePushNotificationService;

    private ClientAndServer mockServer;

    @Before
    public void setup() {
        applePushNotificationService = new ApplePushNotificationService(ApplePushNotificationSettings.builder()
                .sslProtocol("TLSv1.3")
                .voipCertificatePath("src/test/resources/self-signed.p12")
                .voipCertificatePassword("s3cr3t")
                .build());

        mockServer = startClientAndServer();
    }

    private void createMockServerRequest(final String deviceToken, final long expiration, final HttpResponse response) {
        new MockServerClient("localhost", mockServer.getLocalPort())
                .when(
                        request()
                                .withMethod("POST")
                                .withPath('/' + deviceToken)
                                .withHeader("apns-push-type", "voip")
                                .withHeader("apns-topic", "org.simlar.Simlar.voip")
                                .withHeader("apns-expiration", Long.toString(expiration))
                                .withBody(new JsonBody("""
                                        {
                                          "caller" : {
                                            "initializationVector" : "someInitializationVector",
                                            "encryptedSimlarId" : "someEncryptedSimlarId"
                                          },
                                          "aps" : {
                                            "alert" : "Simlar Call",
                                            "sound" : "ringtone.wav"
                                          }
                                        }
                                        """))
                ).respond(response);
    }

    private String requestVoipPushNotification(final String deviceToken, final long expiration) {
        return applePushNotificationService.requestVoipPushNotification(
                "http://localhost:" + mockServer.getLocalPort() + '/',
                new ApplePushNotificationRequestCaller("someInitializationVector", "someEncryptedSimlarId"),
                deviceToken,
                "localhost",
                Instant.ofEpochSecond(expiration));
    }

    @Test
    public void testRequestAppleVoipPushNotification() {
        createMockServerRequest("deviceToken", 42,
                response()
                        .withStatusCode(200)
                        .withHeader("apns-id", "someApnsId"));

        assertEquals("someApnsId", requestVoipPushNotification("deviceToken", 42));
    }

    @Test
    public void testRequestAppleVoipPushNotificationWithResponseBody() {
        createMockServerRequest("otherDeviceToken", 23,
                response()
                        .withStatusCode(200)
                        .withHeader("apns-id", "otherApnsId")
                        .withBody("{\"reason\":\"Should not happen\"}"));

        assertEquals("otherApnsId", requestVoipPushNotification("otherDeviceToken", 23));
    }

    @Test
    public void testRequestAppleVoipPushNotificationWithResponseStatusNoContent() {
        createMockServerRequest("deviceToken204", 24,
                response()
                        .withStatusCode(204));

        assertNull(requestVoipPushNotification("deviceToken204", 24));
    }

    @Test
    public void testRequestAppleVoipPushNotificationWithInternalServerError() {
        createMockServerRequest("invalidDeviceToken", 25,
                response()
                        .withStatusCode(500)
                        .withBody("{\"reason\":\"Should not happen\"}"));

        try {
            requestVoipPushNotification("invalidDeviceToken", 25);
            fail("expected exception not thrown: " + HttpServerErrorException.class.getSimpleName());
        } catch (final HttpServerErrorException e) {
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatusCode());
            assertEquals("{\"reason\":\"Should not happen\"}", e.getResponseBodyAsString());
        }
    }

    @Test
    public void testRequestAppleVoipPushNotificationWithInvalidDeviceToken() {
        createMockServerRequest("invalidDeviceToken", 26,
                response()
                        .withStatusCode(400)
                        .withBody("{\"reason\":\"BadDeviceToken\"}"));

        try {
            requestVoipPushNotification("invalidDeviceToken", 26);
            fail("expected exception not thrown: " + HttpClientErrorException.class.getSimpleName());
        } catch (final HttpClientErrorException e) {
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
            assertEquals("{\"reason\":\"BadDeviceToken\"}", e.getResponseBodyAsString());
        }
    }

    @After
    public void after() {
        mockServer.stop();
    }
}
