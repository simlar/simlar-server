package org.simlar.simlarserver.services.pushnotification;

import org.simlar.simlarserver.services.pushnotification.json.GooglePushNotificationAndroidDetails;
import org.simlar.simlarserver.services.pushnotification.json.GooglePushNotificationRequest;
import org.simlar.simlarserver.services.pushnotification.json.GooglePushNotificationRequestDetails;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

final class GooglePushNotificationService {
    static void requestGooglePush(final String url, final String projectId, final String bearer, final String token) {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", "Bearer " + bearer);
/*
        final String request = "{\n" +
                "  \"message\":{\n" +
                "     \"android\":{\n" +
                "       \"ttl\":\"60s\",\n" +
                "       \"collapse_key\":\"call\",\n" +
                "       \"priority\":\"high\"\n" +
                "     },\n" +
                "     \"token\":\"" + token + "\"\n" +
                "  }\n" +
                "}\n";
 */

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