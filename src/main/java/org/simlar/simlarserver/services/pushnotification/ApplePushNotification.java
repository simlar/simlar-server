package org.simlar.simlarserver.services.pushnotification;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.CertificatePinner;
import okhttp3.OkHttpClient;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.net.ssl.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Objects;

@AllArgsConstructor
@Slf4j
@Component
public final class ApplePushNotification {
    static final String APPLE_SERVER_SANDBOX = "api.sandbox.push.apple.com";

    private final PushNotificationSettingsService pushNotificationSettings;

    @Nullable
    static String getCertificateSubject(final KeyStore keyStore, final String alias) {
        try {
            final Certificate certificate = keyStore.getCertificate(alias);
            if (!(certificate instanceof X509Certificate)) {
                return null;
            }

            return Objects.toString(((X509Certificate) certificate).getSubjectDN());
        } catch (final KeyStoreException e) {
            log.error("failed to load certificate with alias '{}'", alias, e);
            return null;
        }
    }

    @Nullable
    static String getKey(final KeyStore keyStore, final String alias, final String password) {
        try {
            final Key key = keyStore.getKey(alias, password.toCharArray());
            return key == null ? null : key.getClass().getSimpleName();
        } catch (final KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
            log.error("failed to load key with alias '{}'", alias, e);
            return null;
        }
    }

    KeyStore createKeyStore() throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException {
        final File file = new File(pushNotificationSettings.getAppleVoipCertificatePath());
        if (!file.exists()) {
            throw new FileNotFoundException("File does not exist: " + file.getAbsolutePath());
        }

        final KeyStore keyStore = KeyStore.getInstance(file, pushNotificationSettings.getAppleVoipCertificatePassword().toCharArray());
        Collections.list(keyStore.aliases()).forEach(alias ->
                log.info("found alias '{}'key='{}'  cert='{}'",
                        alias,
                        getKey(keyStore, alias, pushNotificationSettings.getAppleVoipCertificatePassword()),
                        getCertificateSubject(keyStore, alias)));

        return keyStore;
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

    public void requestVoipPushNotification() throws Exception {
        final CertificatePinner certificatePinner = new CertificatePinner.Builder()
                .add("api.sandbox.push.apple.com", "sha256/tc+C1H75gj+ap48SMYbFLoh56oSw+CLJHYPgQnm3j9U=")
                .build();

        final OkHttpClient client = new OkHttpClient.Builder()
                .sslSocketFactory(
                        createSSLSocketFactory(createKeyStore()),
                        createTrustManager())
                .certificatePinner(certificatePinner)
                .build();

        new RestTemplateBuilder()
                .requestFactory(() -> new OkHttp3ClientHttpRequestFactory(client))
                .build()
                .postForObject("https://" + APPLE_SERVER_SANDBOX + "/3/device/" + "device", null, String.class);
    }
}
