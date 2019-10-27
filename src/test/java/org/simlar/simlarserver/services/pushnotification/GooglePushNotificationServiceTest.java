package org.simlar.simlarserver.services.pushnotification;

import com.google.auth.oauth2.AccessToken;
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
        final AccessToken accessToken = pushNotificationService.getAccessToken();
        assertNotNull(accessToken);
        assertEquals("ya29.c.Kl6", accessToken.getTokenValue().substring(0, 10));
    }

    @Test
    public void testRequestPushNotificationWithInvalidToken() throws IOException {
        try {
            pushNotificationService.requestPushNotification("invalidToken");
            fail("expected exception not thrown: " + HttpClientErrorException.class.getSimpleName());
        } catch (final HttpClientErrorException e) {
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
            //noinspection SpellCheckingInspection
            assertEquals("{\n" +
                    "  \"error\": {\n" +
                    "    \"code\": 400,\n" +
                    "    \"message\": \"The registration token is not a valid FCM registration token\",\n" +
                    "    \"status\": \"INVALID_ARGUMENT\",\n" +
                    "    \"details\": [\n" +
                    "      {\n" +
                    "        \"@type\": \"type.googleapis.com/google.firebase.fcm.v1.FcmError\",\n" +
                    "        \"errorCode\": \"INVALID_ARGUMENT\"\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"@type\": \"type.googleapis.com/google.rpc.BadRequest\",\n" +
                    "        \"fieldViolations\": [\n" +
                    "          {\n" +
                    "            \"field\": \"message.token\",\n" +
                    "            \"description\": \"The registration token is not a valid FCM registration token\"\n" +
                    "          }\n" +
                    "        ]\n" +
                    "      }\n" +
                    "    ]\n" +
                    "  }\n" +
                    "}\n", e.getResponseBodyAsString());
        }
    }
}
