package org.simlar.simlarserver.services.pushnotification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.simlar.simlarserver.services.pushnotification.json.GooglePushNotificationAndroidDetails;
import org.simlar.simlarserver.services.pushnotification.json.GooglePushNotificationRequest;
import org.simlar.simlarserver.services.pushnotification.json.GooglePushNotificationRequestDetails;
import org.simlar.simlarserver.services.pushnotification.json.GooglePushNotificationResponse;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

@Slf4j
@Component
final class GooglePushNotificationService {
    private final GooglePushNotificationSettingsService pushNotificationSettings;

    @Nullable
    private final GoogleCredentials googleCredentials;

    private GooglePushNotificationService(final GooglePushNotificationSettingsService pushNotificationSettings) {
        this.pushNotificationSettings = pushNotificationSettings;

        googleCredentials = pushNotificationSettings.isConfigured()
                ? createGoogleCredentials(pushNotificationSettings.getCredentialsJsonPath())
                : null;
    }

    private static GoogleCredentials createGoogleCredentials(final String jsonFile) {
        try {
            return GoogleCredentials
                    .fromStream(Files.newInputStream(Path.of(jsonFile)))
                    .createScoped(Collections.singletonList("https://www.googleapis.com/auth/firebase.messaging"));
        } catch (final IOException e) {
            log.error("'{}' while creating google credentials using file '{}'", e.getClass().getSimpleName(), jsonFile, e);
        }
        return null;
    }

    @Nullable
    AccessToken getAccessToken() throws IOException {
        if (googleCredentials == null) {
            if (pushNotificationSettings.isConfigured()) {
                log.warn("no google credentials configured");
            } else {
                log.error("no google credentials configured with settings: '{}'", pushNotificationSettings);
            }
            return null;
        }

        googleCredentials.refreshIfExpired();
        return googleCredentials.getAccessToken();
    }

    @Nullable
    public String requestPushNotification(final String token) throws IOException {
        final AccessToken accessToken = getAccessToken();
        final String bearer = accessToken == null ? null : accessToken.getTokenValue();
        if (StringUtils.isEmpty(bearer)) {
            log.error("no bearer token '{}'", accessToken);
            return null;
        }

        return requestPushNotification("https://fcm.googleapis.com/", pushNotificationSettings.getProjectId(), bearer, token);
    }

    @Nullable
    @SuppressFBWarnings("MOM_MISLEADING_OVERLOAD_MODEL")
    static String requestPushNotification(final String url, final String projectId, final String bearer, final String token) {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", "Bearer " + bearer);

        final GooglePushNotificationRequest request = new GooglePushNotificationRequest(
                new GooglePushNotificationRequestDetails(
                    new GooglePushNotificationAndroidDetails("60s", "call", "high"),
                    token));

        final ResponseEntity<String> response = new RestTemplateBuilder()
                .build()
                .postForEntity(url + "/v1/projects/" + projectId + "/messages:send",
                        new HttpEntity<>(request, headers),
                        String.class);

        try {
            final String messageId = new ObjectMapper().readValue(
                    ObjectUtils.defaultIfNull(response.getBody(), ""),
                    GooglePushNotificationResponse.class)
                    .getName();

            if (StringUtils.isEmpty(messageId)) {
                log.error("request with device token '{}' unable to parse messageId from response '{}'", token, response.getBody());
                return null;
            }

            log.info("request with device token '{}' received response with messageId '{}'", token, messageId);
            return messageId;
        } catch (final JsonProcessingException e) {
            log.error("request with device token '{}' unable to parse response '{}'", token, response.getBody());
            return null;
        }
    }
}
