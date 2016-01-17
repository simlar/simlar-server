/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.simlar.simlarserver.controllers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.simlar.simlarserver.Application;
import org.simlar.simlarserver.xml.XmlError;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.StringReader;
import java.util.logging.Logger;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebIntegrationTest(randomPort = true)
public final class ErrorControllerTest {
    private static final Logger LOGGER = Logger.getLogger(ErrorControllerTest.class.getName());

    @Value("${local.server.port}")
    private int                 port;

    private boolean testHttpPost(final String requestUrl, final MultiValueMap parameter) {
        final String result = new RestTemplate().postForObject("http://localhost:" + port + "/" + requestUrl, parameter, String.class);
        assertNotNull(result);


        try {
            final XmlError xmlError = (XmlError)JAXBContext.newInstance(XmlError.class).createUnmarshaller().unmarshal(new StringReader(result));
            return xmlError != null && xmlError.getId() == 1;
        } catch (final JAXBException e) {
            LOGGER.severe("JAXBException: for postResult: " + result);
            return false;
        } catch (final ClassCastException e) {
            LOGGER.severe("ClassCastException for postResult: " + result);
            return false;
        }
    }

    @Test
    public void httpPostRequest() {
        assertTrue(testHttpPost("", null));
        assertTrue(testHttpPost(ContactsController.REQUEST_URL_CONTACTS_STATUS + "x", null));
        assertTrue(testHttpPost("index", null));
        assertTrue(testHttpPost("index.html", null));
        assertTrue(testHttpPost(ContactsController.REQUEST_URL_CONTACTS_STATUS, null));
    }

    @Test
    public void httpPostRequestWrongParameter() {
        final MultiValueMap<String, String> parameter = new LinkedMultiValueMap<>();
        parameter.add("login", "007");
        parameter.add("password", "007");
        assertTrue(testHttpPost(ContactsController.REQUEST_URL_CONTACTS_STATUS, parameter));
    }

    private boolean testHttpGet(final String requestUrl) {
        final String result = new RestTemplate().getForObject("http://localhost:" + port + "/" + requestUrl, String.class);
        assertNotNull(result);

        try {
            final XmlError xmlError = (XmlError)JAXBContext.newInstance(XmlError.class).createUnmarshaller().unmarshal(new StringReader(result));
            return xmlError != null && xmlError.getId() == 1;
        } catch (final JAXBException e) {
            LOGGER.severe("JAXBException: for postResult: " + result);
            return false;
        } catch (final ClassCastException e) {
            LOGGER.severe("ClassCastException for postResult: " + result);
            return false;
        }
    }

    @Test
    public void httpGetRequest() {
        assertTrue(testHttpGet(""));
        assertTrue(testHttpGet(ContactsController.REQUEST_URL_CONTACTS_STATUS + "x"));
        assertTrue(testHttpGet("index"));
        assertTrue(testHttpGet("index.html"));
        assertTrue(testHttpGet(ContactsController.REQUEST_URL_CONTACTS_STATUS));
    }
}
