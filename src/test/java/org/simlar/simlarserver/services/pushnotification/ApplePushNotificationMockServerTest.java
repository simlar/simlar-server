package org.simlar.simlarserver.services.pushnotification;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.junit.MockServerRule;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@SuppressFBWarnings("UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR")
@SuppressWarnings("resource")
public final class ApplePushNotificationMockServerTest {
    private ApplePushNotification applePushNotification;

    @SuppressWarnings("ThisEscapedInObjectConstruction") // used as documented
    @Rule
    public final MockServerRule mockServerRule = new MockServerRule(this);

    @SuppressFBWarnings({"UUF_UNUSED_FIELD", "FCBL_FIELD_COULD_BE_LOCAL"})
    @SuppressWarnings("unused")
    private ClientAndServer mockServer;

    @Before
    public void setup() {
        applePushNotification = new ApplePushNotification(PushNotificationSettingsService.builder()
                .applePushProtocol("TLSv1.3")
                .appleVoipCertificatePath("src/test/resources/self-signed.p12")
                .appleVoipCertificatePassword("s3cr3t")
                .build());
    }

    @SuppressFBWarnings("UTAO_JUNIT_ASSERTION_ODDITIES_NO_ASSERT")
    @SuppressWarnings({"JUnitTestMethodWithNoAssertions", "PMD.JUnitTestsShouldIncludeAssert"})
    @Test
    public void testRequestAppleVoipPushNotification() {
        mockServerRule.getClient()
                .when(
                        request()
                                .withMethod("POST")
                                .withPath("/deviceToken")
                                .withHeader("apns-push-type", "voip")
                                .withHeader("apns-topic", "org.simlar.Simlar.voip")
                                .withBody("{\"aps\":{\"alert\":\"Simlar Call\",\"sound\":\"ringtone.wav\"}}")
                )
                .respond(
                        response()
                                .withStatusCode(200)
                );

        applePushNotification.requestVoipPushNotification("http://localhost:" + mockServerRule.getPort() + "/deviceToken", "localhost");
    }

    @Test
    public void testRequestAppleVoipPushNotificationWithInvalidDeviceToken() {
        mockServerRule.getClient()
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
            applePushNotification.requestVoipPushNotification("http://localhost:" + mockServerRule.getPort() + "/invalidDeviceToken", "localhost");
            fail("expected exception not thrown: " + HttpClientErrorException.class.getSimpleName());
        } catch (final HttpClientErrorException e) {
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
            assertEquals("{\"reason\":\"BadDeviceToken\"}", e.getResponseBodyAsString());
        }
    }
}
