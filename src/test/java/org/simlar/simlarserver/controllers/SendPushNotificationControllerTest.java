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

package org.simlar.simlarserver.controllers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.simlar.simlarserver.services.pushnotification.PushNotificationService;
import org.simlar.simlarserver.services.pushnotification.PushNotificationSettings;
import org.simlar.simlarserver.utils.SimlarId;
import org.simlar.simlarserver.xml.XmlError;
import org.simlar.simlarserver.xml.XmlSuccessSendPushNotification;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public final class SendPushNotificationControllerTest extends BaseControllerTest {
    private static final String API_KEY = "someApiKey";

    @SuppressWarnings("unused")
    @MockBean
    private PushNotificationService pushNotificationsService;

    @SuppressWarnings("unused")
    @MockBean
    private PushNotificationSettings pushNotificationSettings;

    private <T> T postSendPushNotification(final Class<T> responseClass, final String apiKey, final String simlarId) {
        return postRequest(responseClass, SendPushNotificationController.REQUEST_PATH, createParameters(new String[][] {
                { "apiKey", apiKey },
                { "simlarId", simlarId }
        }));
    }

    @Before
    public void mockSettings() {
        when(pushNotificationSettings.getApiKey()).thenReturn(API_KEY);
    }

    @Test
    public void testSendPushNotificationWithNoApiKeyConfigured() {
        when(pushNotificationSettings.getApiKey()).thenReturn(null);
        final XmlError response = postSendPushNotification(XmlError.class, null, "NoSimlarId");

        assertNotNull(response);
        assertEquals(10, response.getId());
    }

    @Test
    public void testSendPushNotificationWithWrongApiKey() {
        final XmlError response = postSendPushNotification(XmlError.class, "wrongApiKey", "NoSimlarId");

        assertNotNull(response);
        assertEquals(10, response.getId());
    }

    @Test
    public void testSendPushNotificationWithMalformattedSimlarId() {
        final XmlError response = postSendPushNotification(XmlError.class, API_KEY, "NoSimlarId");

        verify(pushNotificationsService).sendPushNotification(eq(null));
        assertNotNull(response);
        assertEquals(98, response.getId());
    }

    @Test
    public void testSendPushNotification() {
        final SimlarId simlarId = SimlarId.create("*0001*");
        assertNotNull(simlarId);

        when(pushNotificationsService.sendPushNotification(simlarId)).thenReturn("someMessageId");
        final XmlSuccessSendPushNotification response = postSendPushNotification(XmlSuccessSendPushNotification.class, API_KEY, simlarId.get());

        verify(pushNotificationsService).sendPushNotification(eq(simlarId));
        assertNotNull(response);
        assertEquals("someMessageId", response.getMessageId());
    }
}
