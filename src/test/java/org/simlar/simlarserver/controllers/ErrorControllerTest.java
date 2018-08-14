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
public final class ErrorControllerTest extends BaseControllerTest {
    private static final Logger LOGGER = Logger.getLogger(ErrorControllerTest.class.getName());

    private boolean testHttpPost(final String requestUrl, final MultiValueMap<String, String> parameter) {
        final XmlError xmlError = postRequest(XmlError.class, requestUrl, parameter);
        return xmlError != null && xmlError.getId() == 1;
    }

    @Test
    public void httpPostRequest() {
        assertTrue(testHttpPost("", null));
        assertTrue(testHttpPost(ContactsController.REQUEST_URL_CONTACTS_STATUS + 'x', null));
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
        final String result = new RestTemplate().getForObject("http://localhost:" + port + '/' + requestUrl, String.class);
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
