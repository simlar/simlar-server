package org.simlar.simlarserver.services.pushnotification;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;

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

    @SuppressFBWarnings("UTAO_JUNIT_ASSERTION_ODDITIES_NO_ASSERT")
    @SuppressWarnings({"JUnitTestMethodWithNoAssertions", "PMD.JUnitTestsShouldIncludeAssert"})
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

        GooglePushNotificationService.requestGooglePush("http://localhost:" + mockServer.getLocalPort(), "simlar-org", "someBearer", "someToken");
    }

    @After
    public void after() {
        mockServer.stop();
    }
}
