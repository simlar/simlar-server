package org.simlar.simlarserver.services.pushnotification;

import com.google.auth.oauth2.AccessToken;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.simlar.simlarserver.SimlarServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SimlarServer.class)
public final class GooglePushNotificationServiceTest {
    @Autowired
    private GooglePushNotificationSettingsService pushNotificationSettings;

    @Autowired
    private GooglePushNotificationService pushNotificationService;

    @Before
    public void verifyConfiguration() {
        assumeTrue("This test needs a valid google credential file", pushNotificationSettings.isConfigured());
    }

    @Test
    public void testGetJsonWebToken() throws IOException {
        final AccessToken accessToken = pushNotificationService.getAccessToken();
        assertNotNull(accessToken);
        assertEquals("ya29.c.Kl6", accessToken.getTokenValue().substring(0, 10));
    }
}
