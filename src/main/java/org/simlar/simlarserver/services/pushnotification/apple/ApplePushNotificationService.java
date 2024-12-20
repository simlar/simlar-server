/*
 * Copyright (C) 2019 The Simlar Authors.
 *
 * This file is part of Simlar. (https://www.simlar.org)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 */

package org.simlar.simlarserver.services.pushnotification.apple;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.simlar.simlarserver.data.ApplePushServer;
import org.simlar.simlarserver.services.pushnotification.apple.json.ApplePushNotificationRequest;
import org.simlar.simlarserver.services.pushnotification.apple.json.ApplePushNotificationRequestCaller;
import org.simlar.simlarserver.services.pushnotification.apple.json.ApplePushNotificationRequestDetails;
import org.simlar.simlarserver.utils.certificatepinning.CertificatePinningUtil;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;

import javax.annotation.Nullable;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import java.io.File;
import java.io.IOException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@AllArgsConstructor
@Slf4j
@Component
public final class ApplePushNotificationService {
    private final ApplePushNotificationSettings pushNotificationSettings;

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
    private KeyManager[] createKeyManagers() {
        final KeyStore keyStore = createKeyStore();

        try {
            final KeyManagerFactory keyFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyFactory.init(keyStore, pushNotificationSettings.getVoipCertificatePassword().toCharArray());

            return keyFactory.getKeyManagers();
        } catch (final NoSuchAlgorithmException | UnrecoverableKeyException | KeyStoreException e) {
            throw new AppleKeyStoreException("failed to create KeyManagers from keystore with protocol: " + pushNotificationSettings.getSslProtocol(), e);
        }
    }

    public String requestVoipPushNotification(final ApplePushServer server, final ApplePushNotificationRequestCaller caller, final String deviceToken) {
        return requestVoipPushNotification(server.getUrl(), caller, deviceToken, Instant.now().plusSeconds(60));
    }

    @SuppressFBWarnings("EXS_EXCEPTION_SOFTENING_NO_CONSTRAINTS")
    String requestVoipPushNotification(final String url, final ApplePushNotificationRequestCaller caller, final String deviceToken, final Instant expiration) {
        final List<String> certificatePinnings = pushNotificationSettings.getVoipCertificatePinning();
        if (certificatePinnings == null || certificatePinnings.isEmpty()) {
            log.warn("certificate pinning disabled");
        }

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("apns-push-type", "voip");
        headers.add("apns-topic", "org.simlar.Simlar.voip");
        headers.add("apns-expiration", Long.toString(expiration.getEpochSecond()));

        final ApplePushNotificationRequest request = new ApplePushNotificationRequest(new ApplePushNotificationRequestDetails("Simlar Call", "ringtone.wav"), caller);
        final HttpEntity<ApplePushNotificationRequest> entity = new HttpEntity<>(request, headers);

        try {
            final ResponseEntity<String> response =
                    CertificatePinningUtil.createRestTemplate(certificatePinnings, pushNotificationSettings.getSslProtocol(), createKeyManagers())
                    .postForEntity(url + deviceToken, entity, String.class);

            if (response.hasBody()) {
                log.warn("request with device token '{}' received unexpected body '{}'", deviceToken, response.getBody());
            }

            final HttpStatusCode statusCode = response.getStatusCode();
            if (statusCode != HttpStatus.OK) {
                log.warn("request with device token '{}' received unexpected response status '{}'", deviceToken, statusCode);
            }

            final String apnsId = response.getHeaders().getFirst("apns-id");
            if (StringUtils.isEmpty(apnsId)) {
                log.warn("request with device token '{}' received empty apnsId", deviceToken);
            } else {
                log.info("request with device token '{}' successfully received apnsId '{}'", deviceToken, apnsId);
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
