package org.simlar.simlarserver.services.pushnotification.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public final class GooglePushNotificationResponseTest {

    @Test
    public void testDeserializationPrettyPrint() throws JsonProcessingException {

        final String str = "{\n" +
                "  \"name\": \"projects/simlar-org/messages/0:1572168901680225%09814fb0002e7a5e\"\n" +
                "}\n";

        assertNotNull(new ObjectMapper().readValue(str, GooglePushNotificationResponse.class));
    }

    @Test
    public void testDeserialization3() throws JsonProcessingException {
        assertEquals("newName", new ObjectMapper().readValue("{\"name\":\"newName\"}", GooglePushNotificationResponse.class).getName());
    }

    @Test
    public void testSerialization() throws JsonProcessingException {
        assertEquals("{\"name\":\"newName\"}", new ObjectMapper().writeValueAsString(new GooglePushNotificationResponse("newName")));
    }
}
