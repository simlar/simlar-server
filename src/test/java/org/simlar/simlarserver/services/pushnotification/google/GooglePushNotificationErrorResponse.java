package org.simlar.simlarserver.services.pushnotification.google;

@SuppressWarnings("UtilityClass")
final class GooglePushNotificationErrorResponse {
    private GooglePushNotificationErrorResponse() {
        throw new AssertionError("This class was not meant to be instantiated");
    }

    static final String INVALID_TOKEN =
            "{\n" +
            "  \"error\": {\n" +
            "    \"code\": 400,\n" +
            "    \"message\": \"The registration token is not a valid FCM registration token\",\n" +
            "    \"status\": \"INVALID_ARGUMENT\",\n" +
            "    \"details\": [\n" +
            "      {\n" +
            "        \"@type\": \"type.googleapis.com/google.firebase.fcm.v1.FcmError\",\n" +
            "        \"errorCode\": \"INVALID_ARGUMENT\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"@type\": \"type.googleapis.com/google.rpc.BadRequest\",\n" +
            "        \"fieldViolations\": [\n" +
            "          {\n" +
            "            \"field\": \"message.token\",\n" +
            "            \"description\": \"The registration token is not a valid FCM registration token\"\n" +
            "          }\n" +
            "        ]\n" +
            "      }\n" +
            "    ]\n" +
            "  }\n" +
            "}\n";
}
