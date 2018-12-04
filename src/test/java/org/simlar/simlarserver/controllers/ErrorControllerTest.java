/*
 * Copyright (C) 2016 The Simlar Authors.
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

package org.simlar.simlarserver.controllers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.simlar.simlarserver.services.twilio.TwilioSmsService;
import org.simlar.simlarserver.xml.XmlError;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@SuppressWarnings("ClassWithTooManyTransitiveDependencies")
@RunWith(SpringRunner.class)
public final class ErrorControllerTest extends BaseControllerTest {
    private static final class NoExceptionResponseErrorHandler implements ResponseErrorHandler {
        @SuppressWarnings("MethodReturnAlwaysConstant")
        @Override
        public boolean hasError(@NonNull final ClientHttpResponse response) {
            return false;
        }

        @Override
        public void handleError(@NonNull final ClientHttpResponse response) {
        }
    }

    private static RestOperations createNoExceptionRestTemplate() {
        final RestTemplate restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new NoExceptionResponseErrorHandler());
        return restTemplate;
    }

    private static void assertUnknownStructure(final XmlError xmlError) {
        assertNotNull(xmlError);
        assertEquals(1, xmlError.getId());
    }

    private void assertHttpPost(final String requestPath, final MultiValueMap<String, String> parameters) {
        assertNotNull(requestPath);
        assertUnknownStructure(postRequest(XmlError.class, requestPath, parameters));
    }

    private static void assertUnknownStructure404(final ResponseEntity<String> response) {
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_XML, response.getHeaders().getContentType());
        assertNotNull(response.getBody());

        assertUnknownStructure(unmarshal(XmlError.class, response.getBody()));
    }

    private void assertHttpPost404(final String requestPath, final MultiValueMap<String, String> parameters) {
        assertNotNull(requestPath);
        assertUnknownStructure404(createNoExceptionRestTemplate().postForEntity(getBaseUrl() + requestPath, parameters, String.class));
    }

    @Test
    public void testHttpPostRequest() {
        assertHttpPost404("", null);
        assertHttpPost404(ContactsController.REQUEST_PATH + 'x', null);
        assertHttpPost404("index", null);
        assertHttpPost404("index.html", null);

        assertHttpPost404(ContactsController.REQUEST_PATH + 'x', createParameters(new String[][] {
                { "login", "*0007*" },
                { "password", "007" }
        }));
    }

    @Test
    public void testHttpPostRequestWrongParameter() {
        assertHttpPost(ContactsController.REQUEST_PATH, null);

        assertHttpPost(ContactsController.REQUEST_PATH, createParameters(new String[][] {
                { "login", "007" },
                { "password", "007" }
        }));

        assertHttpPost(ContactsController.REQUEST_PATH, createParameters(new String[][] {
                { "loin", "*0007*" },
                { "password", "007" },
                { "contacts", "*0001*|*0002*" }
        }));

        assertHttpPost(PushNotificationsController.REQUEST_PATH, createParameters(new String[][] {
                { "login", "*0007*" },
                { "password", "007" },
                { "contacts", "*0001*|*0002*" }
        }));
    }

    @Test
    public void testHttpPostRequestWrongParameterTwilio() {
        assertHttpPost(TwilioSmsService.REQUEST_PATH_DELIVERY, createParameters(new String[][] {
                { "ErrorCode", "404" },
                { "SmsSid", "007" },
                { "SmsStatus", "sent" }
        }));
    }


    private void assertHttpGet404(final String requestPath) {
        assertNotNull(requestPath);
        assertUnknownStructure404(createNoExceptionRestTemplate().getForEntity(getBaseUrl() + requestPath, String.class));
    }

    @Test
    public void testHttpGetRequest() {
        assertHttpGet404("");
        assertHttpGet404("/");
        assertHttpGet404(ContactsController.REQUEST_PATH + 'x');
        assertHttpGet404("/index");
        assertHttpGet404("/index.html");
        assertHttpGet404(ContactsController.REQUEST_PATH);
    }
}
