package org.simlar.simlarserver.services.pushnotification;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.simlar.simlarserver.SimlarServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertNull;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = SimlarServer.class)
public final class PushNotificationServiceTest {

    @Autowired
    private PushNotificationService pushNotificationService;

    @Test
    public void testRequestPushNotificationWithNull() {
        assertNull(pushNotificationService.sendPushNotification(null));
    }
}
