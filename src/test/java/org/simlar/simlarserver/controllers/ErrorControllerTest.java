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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
public final class ErrorControllerTest extends BaseControllerTest {
    private void testHttpPost(final String requestUrl, final MultiValueMap<String, String> parameters) {
        final XmlError xmlError = postRequest(XmlError.class, requestUrl, parameters);
        assertNotNull(xmlError);
        assertEquals(1, xmlError.getId());
    }

    @Test
    public void httpPostRequest() {
        testHttpPost("", null);
        testHttpPost(ContactsController.REQUEST_PATH + 'x', null);
        testHttpPost("index", null);
        testHttpPost("index.html", null);
        testHttpPost(ContactsController.REQUEST_PATH, null);
    }

    @Test
    public void httpPostRequestWrongParameter() {
        final MultiValueMap<String, String> parameter = new LinkedMultiValueMap<>();
        parameter.add("login", "007");
        parameter.add("password", "007");
        testHttpPost(ContactsController.REQUEST_PATH, parameter);
    }

    private void testHttpGet(final String requestUrl) {
        final String result = new RestTemplate().getForObject(getBaseUrl() + requestUrl, String.class);
        assertNotNull(result);

        try {
            final XmlError xmlError = (XmlError)JAXBContext.newInstance(XmlError.class).createUnmarshaller().unmarshal(new StringReader(result));
            assertNotNull(xmlError);
            assertEquals(1, xmlError.getId());
        } catch (final JAXBException e) {
            throw new AssertionError("JAXBException: for postResult: " + result, e);
        } catch (final ClassCastException e) {
            throw new AssertionError("ClassCastException: for postResult: " + result, e);
        }
    }

    @Test
    public void httpGetRequest() {
        testHttpGet("");
        testHttpGet("/");
        testHttpGet(ContactsController.REQUEST_PATH + 'x');
        testHttpGet("/index");
        testHttpGet("/index.html");
        testHttpGet(ContactsController.REQUEST_PATH);
    }
}
