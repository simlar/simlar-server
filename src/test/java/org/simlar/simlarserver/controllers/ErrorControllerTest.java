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
import org.simlar.simlarserver.xml.XmlError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
public final class ErrorControllerTest extends BaseControllerTest {
    @SuppressWarnings("JUnitTestClassNamingConvention")
    private static final class NoExceptionResponseErrorHandler implements ResponseErrorHandler {
        @SuppressWarnings("MethodReturnAlwaysConstant")
        @Override
        public boolean hasError(final ClientHttpResponse response) {
            return false;
        }

        @Override
        public void handleError(final ClientHttpResponse response) {
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

    private void httpPost(final String requestPath, final MultiValueMap<String, String> parameters) {
        assertNotNull(requestPath);
        assertUnknownStructure(postRequest(XmlError.class, requestPath, parameters));
    }

    private static void assertUnknownStructure404(final ResponseEntity<String> response) {
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());

        assertUnknownStructure(unmarshal(XmlError.class, response.getBody()));
    }

    private void httpPost404(final String requestPath, final MultiValueMap<String, String> parameters) {
        assertNotNull(requestPath);
        assertUnknownStructure404(createNoExceptionRestTemplate().postForEntity(getBaseUrl() + requestPath, parameters, String.class));
    }

    @Test
    public void testHttpPostRequest() {
        httpPost404("", null);
        httpPost404(ContactsController.REQUEST_PATH + 'x', null);
        httpPost404("index", null);
        httpPost404("index.html", null);

        httpPost404(ContactsController.REQUEST_PATH + 'x', createParameters(new String[][] {
                { "login", "*0007*" },
                { "password", "007" }
        }));
    }

    @Test
    public void testHttpPostRequestWrongParameter() {
        httpPost(ContactsController.REQUEST_PATH, null);

        httpPost(ContactsController.REQUEST_PATH, createParameters(new String[][] {
                { "login", "007" },
                { "password", "007" }
        }));

        httpPost(ContactsController.REQUEST_PATH, createParameters(new String[][] {
                { "loin", "*0007*" },
                { "password", "007" },
                { "contacts", "*0001*|*0002*" }
        }));

        httpPost(PushNotificationsController.REQUEST_PATH, createParameters(new String[][] {
                { "login", "*0007*" },
                { "password", "007" },
                { "contacts", "*0001*|*0002*" }
        }));
    }

    @Test
    public void testHttpPostRequestWrongParameterTwilio() {
        httpPost(TwilioController.REQUEST_PATH, createParameters(new String[][] {
                { "ErrorCode", "404" },
                { "SmsSid", "007" },
                { "SmsStatus", "sent" }
        }));
    }


    private void httpGet404(final String requestPath) {
        assertNotNull(requestPath);
        assertUnknownStructure404(createNoExceptionRestTemplate().getForEntity(getBaseUrl() + requestPath, String.class));
    }

    @Test
    public void testHttpGetRequest() {
        httpGet404("");
        httpGet404("/");
        httpGet404(ContactsController.REQUEST_PATH + 'x');
        httpGet404("/index");
        httpGet404("/index.html");
        httpGet404(ContactsController.REQUEST_PATH);
    }
}
