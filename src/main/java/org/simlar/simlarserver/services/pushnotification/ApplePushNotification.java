package org.simlar.simlarserver.services.pushnotification;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.CertificatePinner;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.StringUtils;
import org.simlar.simlarserver.services.pushnotification.json.ApplePushNotificationRequest;
import org.simlar.simlarserver.services.pushnotification.json.ApplePushNotificationRequestDetails;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.IOException;
import java.security.Key;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Objects;

@AllArgsConstructor
@Slf4j
@Component
final class ApplePushNotification {
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

    @SuppressFBWarnings("PATH_TRAVERSAL_IN")
    KeyStore createKeyStore() throws AppleKeyStoreException {
        final File file = new File(pushNotificationSettings.getAppleVoipCertificatePath());
        if (!file.exists()) {
            throw new AppleKeyStoreException("Certificate file does not exist: " + file.getAbsolutePath());
        }

        try {
            final KeyStore keyStore = KeyStore.getInstance(file, pushNotificationSettings.getAppleVoipCertificatePassword().toCharArray());
            Collections.list(keyStore.aliases()).forEach(alias ->
                    log.info("found alias '{}'key='{}'  cert='{}'",
                            alias,
                            getKey(keyStore, alias, pushNotificationSettings.getAppleVoipCertificatePassword()),
                            getCertificateSubject(keyStore, alias)));
            return keyStore;
        } catch (final IOException | CertificateException | NoSuchAlgorithmException | KeyStoreException e) {
            throw new AppleKeyStoreException("failed to create key store from: " + file.getAbsolutePath(), e);
        }
    }

    private SSLSocketFactory createSSLSocketFactory() throws AppleKeyStoreException {
        final KeyStore keyStore = createKeyStore();

        try {
            final KeyManagerFactory keyFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyFactory.init(keyStore, pushNotificationSettings.getAppleVoipCertificatePassword().toCharArray());

            final SSLContext sslContext = SSLContext.getInstance(pushNotificationSettings.getApplePushProtocol());
            sslContext.init(keyFactory.getKeyManagers(), null, null);

            return sslContext.getSocketFactory();
        } catch (final NoSuchAlgorithmException | UnrecoverableKeyException | KeyStoreException | KeyManagementException e) {
            throw new AppleKeyStoreException("failed to create SSLSocketFactory store from keystore with protocol; " + pushNotificationSettings.getApplePushProtocol(),  e);
        }
    }

    @SuppressFBWarnings("WEM_WEAK_EXCEPTION_MESSAGING")
    private static TrustManagerFactory createTrustManagerFactory() throws AppleKeyStoreException {
        try {
            final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init((KeyStore) null);
            return trustManagerFactory;
        } catch (final NoSuchAlgorithmException | KeyStoreException e) {
            throw new AppleKeyStoreException("failed to create TrustManagerFactory", e);
        }
    }

    private static X509TrustManager createTrustManager() throws AppleKeyStoreException {
        final TrustManager trustManager = createTrustManagerFactory().getTrustManagers()[0];
        if (!(trustManager instanceof X509TrustManager)) {
            throw new AppleKeyStoreException("first trust manager of invalid type: " + trustManager.getClass().getSimpleName());
        }
        return (X509TrustManager) trustManager;
    }

    public void requestVoipPushNotification(final ApplePushServer server, final String deviceToken) throws AppleKeyStoreException {
        requestVoipPushNotification(server.getUrl() + deviceToken, server.getBaseUrl(), Instant.now().plusSeconds(60));
    }

    void requestVoipPushNotification(final String url, final String urlPin, final Instant expiration) throws AppleKeyStoreException {
        final OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                .sslSocketFactory(
                        createSSLSocketFactory(),
                        createTrustManager());

        final String certificatePinning = pushNotificationSettings.getAppleVoipCertificatePinning();
        if (StringUtils.isEmpty(certificatePinning)) {
            log.warn("certificate pinning disabled");
        } else {
            clientBuilder.certificatePinner(new CertificatePinner.Builder()
                    .add(urlPin, certificatePinning)
                    .build());
        }

        final OkHttpClient client = clientBuilder.build();

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("apns-push-type", "voip");
        headers.add("apns-topic", "org.simlar.Simlar.voip");
        headers.add("apns-expiration", Long.toString(expiration.atZone(ZoneOffset.UTC).toEpochSecond()));

        final ApplePushNotificationRequest request = new ApplePushNotificationRequest(new ApplePushNotificationRequestDetails("Simlar Call", "ringtone.wav"));
        final HttpEntity<ApplePushNotificationRequest> entity = new HttpEntity<>(request, headers);

        new RestTemplateBuilder()
                .requestFactory(() -> new OkHttp3ClientHttpRequestFactory(client))
                .build()
                .postForObject(url, entity, String.class);
    }
}
