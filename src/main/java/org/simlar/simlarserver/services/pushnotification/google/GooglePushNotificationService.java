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

package org.simlar.simlarserver.services.pushnotification.google;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.extern.slf4j.Slf4j;
import okhttp3.CertificatePinner;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.simlar.simlarserver.data.GooglePushServer;
import org.simlar.simlarserver.services.pushnotification.google.json.GooglePushNotificationAndroidDetails;
import org.simlar.simlarserver.services.pushnotification.google.json.GooglePushNotificationRequest;
import org.simlar.simlarserver.services.pushnotification.google.json.GooglePushNotificationRequestDetails;
import org.simlar.simlarserver.services.pushnotification.google.json.GooglePushNotificationResponse;
import org.simlar.simlarserver.utils.CertificatePinnerUtil;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

@Slf4j
@Component
public final class GooglePushNotificationService {
    private final GooglePushNotificationSettings pushNotificationSettings;

    private final GoogleCredentials googleCredentials;

    GooglePushNotificationService(final GooglePushNotificationSettings pushNotificationSettings) {
        this.pushNotificationSettings = pushNotificationSettings;

        //noinspection AssignmentToNull
        googleCredentials = pushNotificationSettings.isConfigured()
                ? createGoogleCredentials(pushNotificationSettings.getCredentialsJsonPath())
                : null;
    }

    private static GoogleCredentials createGoogleCredentials(final String credentialsJsonPath) {
        try {
            return GoogleCredentials
                    .fromStream(Files.newInputStream(Path.of(credentialsJsonPath)))
                    .createScoped(Collections.singletonList("https://www.googleapis.com/auth/firebase.messaging"));
        } catch (final IOException e) {
            log.error("'{}' while creating google credentials using file '{}'", e.getClass().getSimpleName(), credentialsJsonPath, e);
        }
        return null;
    }

    @Nullable
    String getAccessTokenValue() {
        if (googleCredentials == null) {
            if (pushNotificationSettings.isConfigured()) {
                log.warn("no google credentials configured");
            } else {
                log.error("no google credentials configured with settings: '{}'", pushNotificationSettings);
            }
            return null;
        }

        try {
            googleCredentials.refreshIfExpired();
        } catch (final IOException e) {
            log.error("failed to refresh google credentials", e);
            return null;
        }

        final AccessToken accessToken = googleCredentials.getAccessToken();
        return accessToken == null ? null : accessToken.getTokenValue();
    }

    @Nullable
    public String requestPushNotification(final String token) {
        final String bearer = getAccessTokenValue();
        if (StringUtils.isEmpty(bearer)) {
            log.error("no bearer token");
            return null;
        }

        return requestPushNotification(
                GooglePushServer.URL,
                CertificatePinnerUtil.createCertificatePinner(GooglePushServer.BASE_URL, pushNotificationSettings.getFirebaseCertificatePinning()),
                pushNotificationSettings.getProjectId(),
                bearer,
                token);
    }

    @Nullable
    @SuppressFBWarnings({"MOM_MISLEADING_OVERLOAD_MODEL", "EXS_EXCEPTION_SOFTENING_NO_CONSTRAINTS"})
    static String requestPushNotification(final String url, final CertificatePinner certificatePinner, final String projectId, final String bearer, final String token) {
        final OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();

        if (certificatePinner.getPins().isEmpty()) {
            log.warn("certificate pinning disabled");
        } else {
            clientBuilder.certificatePinner(certificatePinner);
        }

        final OkHttpClient client = clientBuilder.build();

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", "Bearer " + bearer);

        try {
            final GooglePushNotificationRequest request = new GooglePushNotificationRequest(
                    new GooglePushNotificationRequestDetails(
                            new GooglePushNotificationAndroidDetails("60s", "call", "high"),
                            token));

            final ResponseEntity<String> response = new RestTemplateBuilder()
                    .requestFactory(() -> new OkHttp3ClientHttpRequestFactory(client))
                    .build()
                    .postForEntity(url + "/v1/projects/" + projectId + "/messages:send",
                            new HttpEntity<>(request, headers),
                            String.class);

            final String messageId = parseResponse(response.getBody());

            if (StringUtils.isEmpty(messageId)) {
                log.error("request with device token '{}' unable to parse messageId from response '{}'", token, response.getBody());
                return null;
            }

            log.info("request with device token '{}' received response with messageId '{}'", token, messageId);
            return messageId;
        } catch (final HttpStatusCodeException e) {
            log.error("request with device token '{}' status code '{}' and body '{}'", token, e.getStatusCode(), e.getResponseBodyAsString());
            throw e;
        } catch (final RestClientException e) {
            log.error("request with device token '{}' error '{}'", token, ExceptionUtils.getRootCauseMessage(e));
            throw e;
        }
    }

    @Nullable
    private static String parseResponse(final String response) {
        try {
            return new ObjectMapper().readValue(
                    ObjectUtils.defaultIfNull(response, ""),
                    GooglePushNotificationResponse.class)
                    .getName();
        } catch (final JsonProcessingException e) {
            log.error("unable to parse response '{}'", response);
            return null;
        }
    }
}
