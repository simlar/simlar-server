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

package org.simlar.simlarserver.services.pushnotification;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.simlar.simlarserver.SimlarServer;
import org.simlar.simlarserver.data.DeviceType;
import org.simlar.simlarserver.database.models.PushNotification;
import org.simlar.simlarserver.database.repositories.PushNotificationsRepository;
import org.simlar.simlarserver.services.pushnotification.apple.ApplePushNotificationService;
import org.simlar.simlarserver.services.pushnotification.apple.ApplePushServer;
import org.simlar.simlarserver.services.pushnotification.apple.json.ApplePushNotificationRequestCaller;
import org.simlar.simlarserver.services.pushnotification.google.GooglePushNotificationService;
import org.simlar.simlarserver.services.subscriberservice.SubscriberService;
import org.simlar.simlarserver.utils.AesUtil;
import org.simlar.simlarserver.utils.SimlarId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = SimlarServer.class)
public final class PushNotificationServiceTest {
    @Autowired
    private PushNotificationsRepository pushNotificationsRepository;

    @SuppressWarnings("unused")
    @MockBean
    private ApplePushNotificationService applePushNotificationService;

    @SuppressWarnings("unused")
    @MockBean
    private GooglePushNotificationService googlePushNotificationService;

    @SuppressWarnings("unused")
    @MockBean
    private SubscriberService subscriberService;

    @Autowired
    private PushNotificationService pushNotificationService;

    @Test
    public void testRequestPushNotificationWithNull() {
        assertNull(pushNotificationService.sendPushNotification(null, null));
    }

    @Test
    public void testRequestApplePushNotificationUnsupported() {
        pushNotificationsRepository.save(new PushNotification("*12340*", DeviceType.IOS, "someToken"));
        assertNull(pushNotificationService.sendPushNotification(SimlarId.create("*12340*"), SimlarId.create("*04321*")));
    }

    @Test
    public void testRequestApplePushNotificationUnsupportedDevelopment() {
        pushNotificationsRepository.save(new PushNotification("*12341*", DeviceType.IOS_DEVELOPMENT, "otherToken"));
        assertNull(pushNotificationService.sendPushNotification(SimlarId.create("*12341*"), SimlarId.create("*14321*")));
    }

    @Test
    public void testRequestApplePushNotification() {
        final SimlarId caller = SimlarId.create("*24321*");
        assertNotNull(caller);
        final SimlarId callee = SimlarId.create("*12342*");
        assertNotNull(callee);
        pushNotificationsRepository.save(new PushNotification(callee.get(), DeviceType.IOS_VOIP, "someToken"));

        when(subscriberService.getHa1(callee)).thenReturn("somePasswordHash");
        when(applePushNotificationService.requestVoipPushNotification(eq(ApplePushServer.PRODUCTION), any(ApplePushNotificationRequestCaller.class), eq("someToken"))).thenReturn("someMessageId");
        assertEquals("someMessageId", pushNotificationService.sendPushNotification(caller, callee));
        verify(applePushNotificationService).requestVoipPushNotification(
                eq(ApplePushServer.PRODUCTION),
                argThat(arg -> AesUtil.decrypt(arg.getEncryptedSimlarId(), arg.getInitializationVector(), "somePasswordHash").equals(caller.get())),
                eq("someToken"));
    }

    @Test
    public void testRequestApplePushNotificationDevelopment() {
        final SimlarId caller = SimlarId.create("*34321*");
        assertNotNull(caller);
        final SimlarId callee = SimlarId.create("*12343*");
        assertNotNull(callee);
        pushNotificationsRepository.save(new PushNotification(callee.get(), DeviceType.IOS_VOIP_DEVELOPMENT, "otherToken"));

        when(subscriberService.getHa1(callee)).thenReturn("otherPasswordHash");
        when(applePushNotificationService.requestVoipPushNotification(eq(ApplePushServer.SANDBOX), any(ApplePushNotificationRequestCaller.class), eq("otherToken"))).thenReturn("otherMessageId");
        assertEquals("otherMessageId", pushNotificationService.sendPushNotification(caller, callee));
        verify(applePushNotificationService).requestVoipPushNotification(
                eq(ApplePushServer.SANDBOX),
                argThat(arg -> AesUtil.decrypt(arg.getEncryptedSimlarId(), arg.getInitializationVector(), "otherPasswordHash").equals(caller.get())),
                eq("otherToken"));
    }

    @Test
    public void testRequestGooglePushNotification() {
        pushNotificationsRepository.save(new PushNotification("*12345*", DeviceType.ANDROID, "someToken"));

        when(googlePushNotificationService.requestPushNotification("someToken")).thenReturn("someMessageId");
        assertEquals("someMessageId", pushNotificationService.sendPushNotification(SimlarId.create("*54321*"), SimlarId.create("*12345*")));
        verify(googlePushNotificationService).requestPushNotification(eq("someToken"));
    }

    @Test
    public void testRequestPushNotificationWithUnknownSimlarId() {
        assertNull(pushNotificationService.sendPushNotification(SimlarId.create("*64321*"), SimlarId.create("*12346*")));
    }
}
