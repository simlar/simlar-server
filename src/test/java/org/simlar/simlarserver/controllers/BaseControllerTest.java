/*
 * Copyright (C) 2015 The Simlar Authors.
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

import org.simlar.simlarserver.Application;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.StringReader;

import static org.junit.Assert.assertNotNull;

@SpringApplicationConfiguration(classes = Application.class)
@WebIntegrationTest(randomPort = true)
public class BaseControllerTest {
    @SuppressWarnings("CanBeFinal")
    @Value("${local.server.port}")
    private int port;

    <T> T postRequest(final Class<T> responseClass, final String url, final MultiValueMap<String, String> parameter) {
        final String result = new RestTemplate().postForObject(getBaseUrl() + url, parameter,
                String.class);

        assertNotNull(result);

        try {
            //noinspection unchecked
            return (T) JAXBContext.newInstance(responseClass).createUnmarshaller().unmarshal(new StringReader(result));
        } catch (final JAXBException e) {
            throw new AssertionError("JAXBException: for postResult: " + result, e);
        } catch (final ClassCastException e) {
            throw new AssertionError("ClassCastException: for postResult: " + result, e);
        }
    }

    String getBaseUrl() {
        return "http://localhost:" + port;
    }
}
