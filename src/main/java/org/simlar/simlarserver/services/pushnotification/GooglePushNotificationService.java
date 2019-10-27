package org.simlar.simlarserver.services.pushnotification;

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.simlar.simlarserver.services.pushnotification.json.GooglePushNotificationAndroidDetails;
import org.simlar.simlarserver.services.pushnotification.json.GooglePushNotificationRequest;
import org.simlar.simlarserver.services.pushnotification.json.GooglePushNotificationRequestDetails;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;

@Slf4j
@Component
final class GooglePushNotificationService {
    private final GooglePushNotificationSettingsService pushNotificationSettings;

    @Nullable
    private final GoogleCredentials googleCredentials;

    GooglePushNotificationService(final GooglePushNotificationSettingsService pushNotificationSettings) {
        this.pushNotificationSettings = pushNotificationSettings;

        googleCredentials = pushNotificationSettings.isConfigured()
                ? createGoogleCredentials(pushNotificationSettings.getCredentialsJsonPath())
                : null;
    }

    private static GoogleCredentials createGoogleCredentials(final String jsonFile) {
        try (final FileInputStream stream = new FileInputStream(jsonFile)) {
            return GoogleCredentials
                    .fromStream(stream)
                    .createScoped(Collections.singletonList("https://www.googleapis.com/auth/firebase.messaging"));
        } catch (final FileNotFoundException e) {
            log.error("file not found '{}'", jsonFile, e);
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

    public void requestPushNotification(final String token) throws IOException {
        final AccessToken accessToken = getAccessToken();
        final String bearer = accessToken == null ? null : accessToken.getTokenValue();
        if (StringUtils.isEmpty(bearer)) {
            log.error("no bearer token '{}'", accessToken);
            return;
        }

        requestPushNotification("https://fcm.googleapis.com/", pushNotificationSettings.getProjectId(), bearer, token);
    }

    static void requestPushNotification(final String url, final String projectId, final String bearer, final String token) {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", "Bearer " + bearer);

        final GooglePushNotificationRequest request = new GooglePushNotificationRequest(
                new GooglePushNotificationRequestDetails(
                    new GooglePushNotificationAndroidDetails("60s", "call", "high"),
                    token));

        new RestTemplateBuilder()
                .build()
                .postForObject(url + "/v1/projects/" + projectId + "/messages:send",
                        new HttpEntity<>(request, headers),
                        String.class);
    }
}
