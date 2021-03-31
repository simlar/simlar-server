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

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.simlar.simlarserver.SimlarServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SimlarServer.class)
public final class GooglePushNotificationServiceTest {
    @Autowired
    private GooglePushNotificationSettings pushNotificationSettings;

    @Autowired
    private GooglePushNotificationService pushNotificationService;

    @Before
    public void verifyConfiguration() {
        assumeTrue("This test needs a valid google credential file", pushNotificationSettings.isConfigured());
    }

    @Test
    public void testGetJsonWebToken() {
        final String bearer = pushNotificationService.getAccessTokenValue();
        assertEquals("ya29.c.", StringUtils.left(bearer, 7));
    }

    @Test
    public void testRequestPushNotificationWithWrongCertificatePinning() {
        try {
            final GooglePushNotificationSettings settings = GooglePushNotificationSettings.builder()
                    .credentialsJsonPath(pushNotificationSettings.getCredentialsJsonPath())
                    .projectId(pushNotificationSettings.getProjectId())
                    .firebaseCertificatePinning(Collections.singletonList("sha256/_________WRONG_CERTIFICATE_PINNING_________="))
                    .build();

            new GooglePushNotificationService(settings).requestPushNotification("invalidDeviceToken");
            fail("expected exception not thrown: " + ResourceAccessException.class.getSimpleName());
        } catch (final ResourceAccessException e) {
            assertEquals("SSLPeerUnverifiedException", e.getCause().getClass().getSimpleName());
            assertNotNull(e.getMessage());
            assertTrue(e.getMessage().contains("Certificate pinning failure!"));
        }
    }

    @Test
    public void testRequestPushNotificationWithInvalidToken() {
        try {
            pushNotificationService.requestPushNotification("invalidToken");
            fail("expected exception not thrown: " + HttpClientErrorException.class.getSimpleName());
        } catch (final HttpClientErrorException e) {
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
            assertEquals(GooglePushNotificationErrorResponse.INVALID_TOKEN, e.getResponseBodyAsString());
        }
    }

    @Test
    public void testRequestPushNotificationWithValidToken() {
        final String deviceToken = pushNotificationSettings.getTestDeviceToken();
        assumeTrue("This test needs a valid device token in the properties", StringUtils.isNotEmpty(deviceToken));

        assertNotNull(pushNotificationService.requestPushNotification(deviceToken));
    }
}
