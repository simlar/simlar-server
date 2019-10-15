package org.simlar.simlarserver.services.pushnotification;

import org.junit.Before;
import okhttp3.OkHttpClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.simlar.simlarserver.SimlarServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpClientErrorException;

import javax.net.ssl.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SimlarServer.class)
public final class ApplePushNotificationTest {
    private static final String APPLE_SERVER_SANDBOX = "api.sandbox.push.apple.com";

    @Autowired
    private PushNotificationSettingsService pushNotificationSettings;

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
                    .postForObject("https://" + APPLE_SERVER_SANDBOX + "/3/device/" + "deviceToken", null, String.class);
            fail("expected exception not thrown: " + HttpClientErrorException.class.getSimpleName());
        } catch (final HttpClientErrorException e) {
            assertEquals(HttpStatus.FORBIDDEN, e.getStatusCode());
            assertEquals("{\"reason\":\"MissingProviderToken\"}", e.getResponseBodyAsString());
        }
    }

    private KeyStore createKeyStore() throws Exception {
        final File file = new File(pushNotificationSettings.getAppleVoipCertificatePath());
        if (!file.exists()) {
            throw new FileNotFoundException("File does not exist: " + file.getAbsolutePath());
        }

        return KeyStore.getInstance(file, pushNotificationSettings.getAppleVoipCertificatePassword().toCharArray());
    }

    @Test
    public void testReadKeyStore() throws Exception {
        final KeyStore keyStore = createKeyStore();

        final Collection<String> aliases = Collections.list(keyStore.aliases());
        assertEquals(1, aliases.size());

        for (final String alias : aliases) {
            //System.out.println("alias: " + alias);

            final X509Certificate certificate = (X509Certificate) keyStore.getCertificate(alias);
            //System.out.println("certificate subject: " + certificate.getSubjectDN());
            assertNotNull(certificate);

            final Key key = keyStore.getKey(alias, pushNotificationSettings.getAppleVoipCertificatePassword().toCharArray());
            //System.out.println("key:" + key);
            assertNotNull(key);
        }
    }

    private SSLSocketFactory createSSLSocketFactory(final KeyStore keyStore) throws NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException, KeyManagementException {
        final KeyManagerFactory keyFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyFactory.init(keyStore, pushNotificationSettings.getAppleVoipCertificatePassword().toCharArray());

        final KeyManager[] keyManagers = keyFactory.getKeyManagers();

        final SSLContext sslContext = SSLContext.getInstance(pushNotificationSettings.getApplePushProtocol());
        sslContext.init(keyManagers, null, null);

        return sslContext.getSocketFactory();
    }

    private static X509TrustManager createTrustManager() throws NoSuchAlgorithmException, KeyStoreException {
        final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init((KeyStore) null);
        return (X509TrustManager) trustManagerFactory.getTrustManagers()[0];
    }

    @Test
    public void testConnectToAppleWithCertificatePayloadEmpty() throws Exception {
        final OkHttpClient client = new OkHttpClient.Builder()
                .sslSocketFactory(
                        createSSLSocketFactory(createKeyStore()),
                        createTrustManager())
                .build();

        try {
            new RestTemplateBuilder()
                    .requestFactory(() -> new OkHttp3ClientHttpRequestFactory(client))
                    .build()
                    .postForObject("https://" + APPLE_SERVER_SANDBOX + "/3/device/" + "device", null, String.class);
            fail("expected exception not thrown: " + HttpClientErrorException.class.getSimpleName());
        } catch (final HttpClientErrorException e) {
            assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
            assertEquals("{\"reason\":\"PayloadEmpty\"}", e.getResponseBodyAsString());
        }
    }
}
