package org.simlar.simlarserver.services.pushnotification;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.CertificatePinner;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.simlar.simlarserver.services.pushnotification.json.ApplePushNotificationRequest;
import org.simlar.simlarserver.services.pushnotification.json.ApplePushNotificationRequestDetails;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;

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
import java.util.Collections;
import java.util.Objects;

@SuppressWarnings("DesignForExtension") // mocked in tests
@AllArgsConstructor
@Slf4j
@Component
class ApplePushNotificationService {
    private final ApplePushNotificationSettingsService pushNotificationSettings;

    @Nullable
    static String getCertificateSubject(final KeyStore keyStore, final String alias) {
        try {
            final Certificate certificate = keyStore.getCertificate(alias);
            if (!(certificate instanceof X509Certificate)) {
                return null;
            }

            return Objects.toString(((X509Certificate) certificate).getSubjectX500Principal());
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

    @SuppressFBWarnings({"PATH_TRAVERSAL_IN", "EXS_EXCEPTION_SOFTENING_NO_CONSTRAINTS"})
    KeyStore createKeyStore() {
        final File file = new File(pushNotificationSettings.getVoipCertificatePath());
        if (!file.exists()) {
            throw new AppleKeyStoreException("Certificate file does not exist: " + file.getAbsolutePath());
        }

        try {
            final KeyStore keyStore = KeyStore.getInstance(file, pushNotificationSettings.getVoipCertificatePassword().toCharArray());
            Collections.list(keyStore.aliases()).forEach(alias ->
                    log.info("found alias '{}'key='{}'  cert='{}'",
                            alias,
                            getKey(keyStore, alias, pushNotificationSettings.getVoipCertificatePassword()),
                            getCertificateSubject(keyStore, alias)));
            return keyStore;
        } catch (final IOException | CertificateException | NoSuchAlgorithmException | KeyStoreException e) {
            throw new AppleKeyStoreException("failed to create key store from: " + file.getAbsolutePath(), e);
        }
    }

    @SuppressFBWarnings("EXS_EXCEPTION_SOFTENING_NO_CONSTRAINTS")
    private SSLSocketFactory createSSLSocketFactory() {
        final KeyStore keyStore = createKeyStore();

        try {
            final KeyManagerFactory keyFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyFactory.init(keyStore, pushNotificationSettings.getVoipCertificatePassword().toCharArray());

            final SSLContext sslContext = SSLContext.getInstance(pushNotificationSettings.getSslProtocol());
            sslContext.init(keyFactory.getKeyManagers(), null, null);

            return sslContext.getSocketFactory();
        } catch (final NoSuchAlgorithmException | UnrecoverableKeyException | KeyStoreException | KeyManagementException e) {
            throw new AppleKeyStoreException("failed to create SSLSocketFactory store from keystore with protocol; " + pushNotificationSettings.getSslProtocol(),  e);
        }
    }

    @SuppressFBWarnings({"WEM_WEAK_EXCEPTION_MESSAGING", "EXS_EXCEPTION_SOFTENING_NO_CONSTRAINTS"})
    private static TrustManagerFactory createTrustManagerFactory() {
        try {
            final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init((KeyStore) null);
            return trustManagerFactory;
        } catch (final NoSuchAlgorithmException | KeyStoreException e) {
            throw new AppleKeyStoreException("failed to create TrustManagerFactory", e);
        }
    }

    private static X509TrustManager createTrustManager() {
        final TrustManager trustManager = createTrustManagerFactory().getTrustManagers()[0];
        if (!(trustManager instanceof X509TrustManager)) {
            throw new AppleKeyStoreException("first trust manager of invalid type: " + trustManager.getClass().getSimpleName());
        }
        return (X509TrustManager) trustManager;
    }

    public String requestVoipPushNotification(final ApplePushServer server, final String deviceToken) {
        return requestVoipPushNotification(server.getUrl(), deviceToken, server.getBaseUrl(), Instant.now().plusSeconds(60));
    }

    @SuppressFBWarnings("EXS_EXCEPTION_SOFTENING_NO_CONSTRAINTS")
    String requestVoipPushNotification(final String url, final String deviceToken, final String urlPin, final Instant expiration) {
        final OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                .sslSocketFactory(
                        createSSLSocketFactory(),
                        createTrustManager());

        final String certificatePinning = pushNotificationSettings.getVoipCertificatePinning();
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
        headers.add("apns-expiration", Long.toString(expiration.getEpochSecond()));

        final ApplePushNotificationRequest request = new ApplePushNotificationRequest(new ApplePushNotificationRequestDetails("Simlar Call", "ringtone.wav"));
        final HttpEntity<ApplePushNotificationRequest> entity = new HttpEntity<>(request, headers);

        try {
            final ResponseEntity<String> response = new RestTemplateBuilder()
                    .requestFactory(() -> new OkHttp3ClientHttpRequestFactory(client))
                    .build()
                    .postForEntity(url + deviceToken, entity, String.class);

            if (response.hasBody()) {
                log.warn("request with device token '{}' received unexpected body '{}'", deviceToken, response.getBody());
            }

            final HttpStatus statusCode = response.getStatusCode();
            if (statusCode != HttpStatus.OK) {
                log.warn("request with device token '{}' received unexpected response status '{}'", deviceToken, statusCode);
            }

            final String apnsId = response.getHeaders().getFirst("apns-id");
            if (StringUtils.isEmpty(apnsId)) {
                log.warn("request with device token '{}' received empty apnsId", deviceToken);
            } else {
                log.info("request with device token '{}' successfully received apnsId '{}'", apnsId, deviceToken);
            }

            return apnsId;
        } catch (final HttpStatusCodeException e) {
            log.error("request with device token '{}' status code '{}' and body '{}'", deviceToken, e.getStatusCode(), e.getResponseBodyAsString());
            throw e;
        } catch (final RestClientException e) {
            log.error("request with device token '{}' error '{}'", deviceToken, ExceptionUtils.getRootCauseMessage(e));
            throw e;
        }
    }
}
