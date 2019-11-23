package org.simlar.simlarserver.services.pushnotification;

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

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
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
        final String bearer = pushNotificationService.getAccessTokenValue();
        assertEquals("ya29.c.Kl6", StringUtils.left(bearer, 10));
    }

    @Test
    public void testRequestPushNotificationWithInvalidToken() throws IOException {
        try {
            pushNotificationService.requestPushNotification("invalidToken");
            fail("expected exception not thrown: " + HttpClientErrorException.class.getSimpleName());
        } catch (final HttpClientErrorException e) {
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
            assertEquals(GooglePushNotificationErrorResponse.INVALID_TOKEN, e.getResponseBodyAsString());
        }
    }

    @Test
    public void testRequestPushNotificationWithValidToken() throws IOException {
        final String deviceToken = pushNotificationSettings.getTestDeviceToken();
        assumeTrue("This test needs a valid device token in the properties", StringUtils.isNotEmpty(deviceToken));

        assertNotNull(pushNotificationService.requestPushNotification(deviceToken));
    }
}
