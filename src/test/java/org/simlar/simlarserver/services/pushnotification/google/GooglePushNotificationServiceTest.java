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
        assertEquals("ya29.c.Kl6", StringUtils.left(bearer, 10));
    }

    @Test
    public void testRequestPushNotificationWithWrongCertificatePinning() {
        try {
            final GooglePushNotificationSettings settings = GooglePushNotificationSettings.builder()
                    .credentialsJsonPath(pushNotificationSettings.getCredentialsJsonPath())
                    .projectId(pushNotificationSettings.getProjectId())
                    .firebaseCertificatePinning("sha256/_________WRONG_CERTIFICATE_PINNING_________=")
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
