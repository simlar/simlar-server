package org.simlar.simlarserver.services.pushnotification;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
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

    @Test
    public void testRequestAppleVoipPushNotification() {
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
                                          "\"token\":\"someToken\"" +
                                        "}}")
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody("{\n" +
                                        "  \"name\": \"projects/simlar-org/messages/0:1572168901680225%09814fb0002e7a5e\"\n" +
                                        "}\n")
                );

        assertEquals(
                "projects/simlar-org/messages/0:1572168901680225%09814fb0002e7a5e",
                GooglePushNotificationService.requestPushNotification("http://localhost:" + mockServer.getLocalPort(), "simlar-org", "someBearer", "someToken"));
    }

    @Test
    public void testRequestPushNotificationWithInvalidToken() {
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
                                          "\"token\":\"invalidToken\"" +
                                        "}}")
                )
                .respond(
                        response()
                                .withStatusCode(400)
                                .withBody("{\n" +
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
                                        "}\n")
                );

        try {
            GooglePushNotificationService.requestPushNotification("http://localhost:" + mockServer.getLocalPort(), "simlar-org", "someBearer", "invalidToken");
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
