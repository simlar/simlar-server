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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.simlar.simlarserver.services.pushnotification.PushNotificationService;
import org.simlar.simlarserver.utils.SimlarId;
import org.simlar.simlarserver.xml.XmlSuccessSendPushNotification;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
public final class SendPushNotificationControllerTest extends BaseControllerTest {
    @SuppressWarnings("unused")
    @MockBean
    private PushNotificationService pushNotificationsService;

    private <T> T postSendPushNotification(final Class<T> responseClass, final String apiKey, final String simlarId) {
        return postRequest(responseClass, SendPushNotificationController.REQUEST_PATH, createParameters(new String[][] {
                { "apiKey", apiKey },
                { "simlarId", simlarId }
        }));
    }

    @Test
    public void testSendPushNotificationWithMalformattedSimlarId() {
        final String simlarId = "NoSimlarId";
        final XmlSuccessSendPushNotification response = postSendPushNotification(XmlSuccessSendPushNotification.class,"someApiKey", simlarId);

        verify(pushNotificationsService).sendPushNotification(eq(null));
        assertNotNull(response);
        assertEquals(simlarId, response.getSimlarId());
    }

    @Test
    public void testSendPushNotification() {
        final String simlarId = "*0001*";
        final XmlSuccessSendPushNotification response = postSendPushNotification(XmlSuccessSendPushNotification.class,"someApiKey", simlarId);

        verify(pushNotificationsService).sendPushNotification(eq(SimlarId.create(simlarId)));
        assertNotNull(response);
        assertEquals(simlarId, response.getSimlarId());
    }
}
