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
import java.util.logging.Logger;

@SpringApplicationConfiguration(classes = Application.class)
@WebIntegrationTest(randomPort = true)
public class BaseControllerTest {
    private static final Logger LOGGER = Logger.getLogger(BaseControllerTest.class.getName());

    @SuppressWarnings("CanBeFinal")
    @Value("${local.server.port}")
    int port;

    <T> T postRequest(final Class<T> responseClass, final String url, final MultiValueMap<String, String> parameter) {
        final String result = new RestTemplate().postForObject("http://localhost:" + port + url, parameter,
                String.class);

        if (result == null) {
            return null;
        }

        try {
            //noinspection unchecked
            return (T) JAXBContext.newInstance(responseClass).createUnmarshaller().unmarshal(new StringReader(result));
        } catch (final JAXBException e) {
            LOGGER.severe("JAXBException: for postResult: " + result);
            return null;
        } catch (final ClassCastException e) {
            LOGGER.severe("ClassCastException for postResult: " + result);
            return null;
        }
    }
}
