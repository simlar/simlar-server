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
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
public final class ErrorControllerTest extends BaseControllerTest {
    private static void assertUnknownStructure(final XmlError xmlError) {
        assertNotNull(xmlError);
        assertEquals(1, xmlError.getId());
    }

    private void httpPost(final String requestPath, final MultiValueMap<String, String> parameters) {
        assertNotNull(requestPath);
        assertUnknownStructure(postRequest(XmlError.class, requestPath, parameters));
    }

    @Test
    public void testHttpPostRequest() {
        httpPost("", null);
        httpPost(ContactsController.REQUEST_PATH + 'x', null);
        httpPost("index", null);
        httpPost("index.html", null);
        httpPost(ContactsController.REQUEST_PATH, null);
    }

    @Test
    public void testHttpPostRequestWrongParameter() {
        httpPost(ContactsController.REQUEST_PATH, createParameters(new String[][] {
                { "login", "007" },
                { "password", "007" }
        }));
    }

    private void httpGet(final String requestPath) {
        assertNotNull(requestPath);
        assertUnknownStructure(unmarshal(XmlError.class, new RestTemplate().getForObject(getBaseUrl() + requestPath, String.class)));
    }

    @Test
    public void testHttpGetRequest() {
        httpGet("");
        httpGet("/");
        httpGet(ContactsController.REQUEST_PATH + 'x');
        httpGet("/index");
        httpGet("/index.html");
        httpGet(ContactsController.REQUEST_PATH);
    }
}
