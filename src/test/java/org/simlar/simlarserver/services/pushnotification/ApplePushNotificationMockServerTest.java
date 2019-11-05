package org.simlar.simlarserver.services.pushnotification;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.time.Instant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@SuppressFBWarnings("UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR")
@SuppressWarnings({"IOResourceOpenedButNotSafelyClosed", "resource"}) // MockServerClient used as documented
public final class ApplePushNotificationMockServerTest {
    private ApplePushNotification applePushNotification;

    private ClientAndServer mockServer;

    @Before
    public void setup() {
        applePushNotification = new ApplePushNotification(PushNotificationSettingsService.builder()
                .applePushProtocol("TLSv1.3")
                .appleVoipCertificatePath("src/test/resources/self-signed.p12")
                .appleVoipCertificatePassword("s3cr3t")
                .build());

        mockServer = startClientAndServer();
    }

    @SuppressFBWarnings("UTAO_JUNIT_ASSERTION_ODDITIES_NO_ASSERT")
    @SuppressWarnings({"JUnitTestMethodWithNoAssertions", "PMD.JUnitTestsShouldIncludeAssert"})
    @Test
    public void testRequestAppleVoipPushNotification() throws AppleKeyStoreException {
        new MockServerClient("localhost", mockServer.getLocalPort())
                .when(
                        request()
                                .withMethod("POST")
                                .withPath("/deviceToken")
                                .withHeader("apns-push-type", "voip")
                                .withHeader("apns-topic", "org.simlar.Simlar.voip")
                                .withHeader("apns-expiration", "42")
                                .withBody("{\"aps\":{\"alert\":\"Simlar Call\",\"sound\":\"ringtone.wav\"}}")
                )
                .respond(
                        response()
                                .withStatusCode(200)
                );

        applePushNotification.requestVoipPushNotification("http://localhost:" + mockServer.getLocalPort() + "/deviceToken", "localhost", Instant.ofEpochSecond(42));
    }

    @Test
    public void testRequestAppleVoipPushNotificationWithInvalidDeviceToken() throws AppleKeyStoreException {
        new MockServerClient("localhost", mockServer.getLocalPort())
                .when(
                        request()
                                .withMethod("POST")
                                .withPath("/invalidDeviceToken")
                )
                .respond(
                        response()
                                .withStatusCode(400)
                                .withBody("{\"reason\":\"BadDeviceToken\"}")
                );

        try {
            applePushNotification.requestVoipPushNotification("http://localhost:" + mockServer.getLocalPort() + "/invalidDeviceToken", "localhost", Instant.now());
            fail("expected exception not thrown: " + HttpClientErrorException.class.getSimpleName());
        } catch (final HttpClientErrorException e) {
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
            assertEquals("{\"reason\":\"BadDeviceToken\"}", e.getResponseBodyAsString());
        }
    }

    @After
    public void after() {
        mockServer.stop();
    }
}
