package org.simlar.simlarserver.services.pushnotification;

import org.junit.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.web.client.HttpClientErrorException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public final class ApplePushNotificationTest {
    private static final String APPLE_SERVER_SANDBOX = "api.sandbox.push.apple.com";

    @Test
    public void testConnectToAppleWithoutCertificateForbidden() {
        try {
            new RestTemplateBuilder()
                    .requestFactory(OkHttp3ClientHttpRequestFactory::new)
                    .build()
                    .postForObject("https://" + APPLE_SERVER_SANDBOX + "/3/device/" + "deviceToken", null, String.class);
            fail("expected exception not thrown: " + HttpClientErrorException.class.getSimpleName());
        } catch (final HttpClientErrorException e) {
            assertEquals(HttpStatus.FORBIDDEN, e.getStatusCode());
            assertEquals("{\"reason\":\"MissingProviderToken\"}", e.getResponseBodyAsString());
        }
    }
}
