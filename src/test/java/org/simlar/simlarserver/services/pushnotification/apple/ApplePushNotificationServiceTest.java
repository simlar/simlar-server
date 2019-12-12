package org.simlar.simlarserver.services.pushnotification.apple;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.simlar.simlarserver.SimlarServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SimlarServer.class)
public final class ApplePushNotificationServiceTest {
    @Autowired
    private ApplePushNotificationSettingsService pushNotificationSettings;

    @Autowired
    private ApplePushNotificationService applePushNotificationService;

    @Before
    public void verifyConfiguration() {
        assumeTrue("This test needs a configuration with certificates and passwords", pushNotificationSettings.isConfigured());
    }

    @Test
    public void testConnectToAppleWithoutCertificateForbidden() {
        try {
            new RestTemplateBuilder()
                    .requestFactory(OkHttp3ClientHttpRequestFactory::new)
                    .build()
                    .postForObject(ApplePushServer.SANDBOX.getUrl() + "deviceToken", null, String.class);
            fail("expected exception not thrown: " + HttpClientErrorException.class.getSimpleName());
        } catch (final HttpClientErrorException e) {
            assertEquals(HttpStatus.FORBIDDEN, e.getStatusCode());
            assertEquals("{\"reason\":\"MissingProviderToken\"}", e.getResponseBodyAsString());
        }
    }

    @Test
    public void testReadKeyStore() throws KeyStoreException {
        final KeyStore keyStore = applePushNotificationService.createKeyStore();

        final List<String> aliases = Collections.list(keyStore.aliases());
        assertEquals(1, aliases.size());

        final String alias = aliases.get(0);
        assertNotNull(ApplePushNotificationService.getKey(keyStore, alias, pushNotificationSettings.getVoipCertificatePassword()));

        final String certificateSubject = ApplePushNotificationService.getCertificateSubject(keyStore, alias);
        assertNotNull(certificateSubject);
        assertTrue(certificateSubject.contains("VoIP"));
        assertTrue(certificateSubject.contains("simlar"));
    }

    @Test
    public void testConnectToAppleWithWrongCertificatePinning() {
        try {
            final ApplePushNotificationSettingsService settings = ApplePushNotificationSettingsService.builder()
                    .sslProtocol(pushNotificationSettings.getSslProtocol())
                    .voipCertificatePath(pushNotificationSettings.getVoipCertificatePath())
                    .voipCertificatePassword(pushNotificationSettings.getVoipCertificatePassword())
                    .voipCertificatePinning("sha256/_________WRONG_CERTIFICATE_PINNING_________=")
                    .build();

            new ApplePushNotificationService(settings).requestVoipPushNotification(ApplePushServer.SANDBOX, "invalidDeviceToken");
            fail("expected exception not thrown: " + ResourceAccessException.class.getSimpleName());
        } catch (final ResourceAccessException e) {
            assertEquals("SSLPeerUnverifiedException", e.getCause().getClass().getSimpleName());
            assertNotNull(e.getMessage());
            assertTrue(e.getMessage().contains("Certificate pinning failure!"));
        }
    }

    @Test
    public void testConnectToAppleWithCertificateBadDeviceToken() {
        try {
            applePushNotificationService.requestVoipPushNotification(ApplePushServer.SANDBOX, "invalidDeviceToken");
            fail("expected exception not thrown: " + HttpClientErrorException.class.getSimpleName());
        } catch (final HttpClientErrorException e) {
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
            assertEquals("{\"reason\":\"BadDeviceToken\"}", e.getResponseBodyAsString());
        }
    }

    @Test
    public void testRequestAppleVoipPushNotification() {
        final String deviceToken = pushNotificationSettings.getVoipTestDeviceToken();
        assumeTrue("This test needs a valid device token in the properties", StringUtils.isNotEmpty(deviceToken));
        assertNotNull(applePushNotificationService.requestVoipPushNotification(ApplePushServer.SANDBOX, deviceToken));
    }
}
