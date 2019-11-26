package org.simlar.simlarserver.services.pushnotification;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.simlar.simlarserver.SimlarServer;
import org.simlar.simlarserver.data.DeviceType;
import org.simlar.simlarserver.database.models.PushNotification;
import org.simlar.simlarserver.database.repositories.PushNotificationsRepository;
import org.simlar.simlarserver.utils.SimlarId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
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

    @Autowired
    private PushNotificationService pushNotificationService;

    @Test
    public void testRequestPushNotificationWithNull() {
        assertNull(pushNotificationService.sendPushNotification(null));
    }

    @Test
    public void testRequestApplePushNotification() {
        pushNotificationsRepository.save(new PushNotification("*12342*", DeviceType.IOS_VOIP, "someToken"));

        when(applePushNotificationService.requestVoipPushNotification(ApplePushServer.PRODUCTION, "someToken")).thenReturn("someMessageId");
        assertEquals("someMessageId", pushNotificationService.sendPushNotification( SimlarId.create("*12342*")));
        verify(applePushNotificationService).requestVoipPushNotification(eq(ApplePushServer.PRODUCTION), eq("someToken"));
    }

    @Test
    public void testRequestGooglePushNotification() {
        pushNotificationsRepository.save(new PushNotification("*12345*", DeviceType.ANDROID, "someToken"));

        when(googlePushNotificationService.requestPushNotification("someToken")).thenReturn("someMessageId");
        assertEquals("someMessageId", pushNotificationService.sendPushNotification( SimlarId.create("*12345*")));
        verify(googlePushNotificationService).requestPushNotification(eq("someToken"));
    }
}
