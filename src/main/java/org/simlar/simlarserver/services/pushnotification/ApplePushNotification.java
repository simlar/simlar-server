package org.simlar.simlarserver.services.pushnotification;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.CertificatePinner;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.StringUtils;
import org.simlar.simlarserver.services.pushnotification.json.ApplePushNotificationRequest;
import org.simlar.simlarserver.services.pushnotification.json.ApplePushNotificationRequestDetails;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.net.ssl.*;
import java.io.File;
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
    private static final String APPLE_SERVER_SANDBOX = "api.sandbox.push.apple.com";
    static final String APPLE_SERVER_SANDBOX_URL = "https://" + APPLE_SERVER_SANDBOX + "/3/device/";

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

    KeyStore createKeyStore() {
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

    private SSLSocketFactory createSSLSocketFactory(final KeyStore keyStore) {
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

    public void requestVoipPushNotification(final String deviceToken) {
        final OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                .sslSocketFactory(
                        createSSLSocketFactory(createKeyStore()),
                        createTrustManager());

        final String certificatePinning = pushNotificationSettings.getAppleVoipCertificatePinning();
        if (StringUtils.isEmpty(certificatePinning)) {
            log.warn("certificate pinning disabled");
        } else {
            clientBuilder.certificatePinner(new CertificatePinner.Builder()
                    .add(APPLE_SERVER_SANDBOX, certificatePinning)
                    .build());
        }

        final OkHttpClient client = clientBuilder.build();

        final ApplePushNotificationRequest request = new ApplePushNotificationRequest(new ApplePushNotificationRequestDetails("Simlar Call", "ringtone.wav"));

        new RestTemplateBuilder()
                .requestFactory(() -> new OkHttp3ClientHttpRequestFactory(client))
                .build()
                .postForObject(APPLE_SERVER_SANDBOX_URL + deviceToken, request, String.class);
    }
}
