package org.simlar.simlarserver.services.pushnotification.google;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@SuppressFBWarnings("UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR")
@SuppressWarnings({"IOResourceOpenedButNotSafelyClosed", "resource"}) // MockServerClient used as documented
public final class GooglePushNotificationMockServerTest {
    private ClientAndServer mockServer;

    @Before
    public void setup() {
        mockServer = startClientAndServer();
    }

    private void createMockServerRequest(final String deviceToken, final HttpResponse response) {
        new MockServerClient("localhost", mockServer.getLocalPort())
                .when(
                        request()
                                .withMethod("POST")
                                .withPath("/v1/projects/simlar-org/messages:send")
                                .withHeader("Authorization", "Bearer someBearer")
                                .withBody("{\"message\":{" +
                                          "\"android\":{" +
                                            "\"ttl\":\"60s\"," +
                                            "\"priority\":\"high\"," +
                                            "\"collapse_key\":\"call\"" +
                                          "}," +
                                          "\"token\":\"" + deviceToken + '"' +
                                        "}}")
                ).respond(response);
    }

    private String requestPushNotification(final String deviceToken) {
        return GooglePushNotificationService.requestPushNotification(
                "http://localhost:" + mockServer.getLocalPort(),
                "localhost",
                null,
                "simlar-org",
                "someBearer",
                deviceToken);
    }

    @Test
    public void testRequestAppleVoipPushNotification() {
        createMockServerRequest("someToken",
                response()
                        .withStatusCode(200)
                        .withBody("{\n" +
                                "  \"name\": \"projects/simlar-org/messages/0:1572168901680225%09814fb0002e7a5e\"\n" +
                                "}\n"));

        assertEquals(
                "projects/simlar-org/messages/0:1572168901680225%09814fb0002e7a5e",
                requestPushNotification("someToken"));
    }

    @Test
    public void testRequestPushNotificationWithInvalidToken() {
        createMockServerRequest("invalidToken",
                response()
                        .withStatusCode(400)
                        .withBody(GooglePushNotificationErrorResponse.INVALID_TOKEN));

        try {
            requestPushNotification("invalidToken");
            fail("expected exception not thrown: " + HttpClientErrorException.class.getSimpleName());
        } catch (final HttpClientErrorException e) {
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
        }
    }

    @After
    public void after() {
        mockServer.stop();
    }
}
