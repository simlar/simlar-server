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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.simlar.simlarserver.Application;
import org.simlar.simlarserver.services.subscriberservice.SubscriberService;
import org.simlar.simlarserver.utils.SimlarId;
import org.simlar.simlarserver.xml.XmlContacts;
import org.simlar.simlarserver.xml.XmlError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebIntegrationTest(randomPort = true)
public final class ContactsControllerTest {
    private static final Logger logger                   = Logger.getLogger(ContactsControllerTest.class.getName());

    private static final String SIMLAR_ID1               = "*0001*";
    private static final String SIMLAR_ID1_PASSWORD      = "x1fg6hk78";
    private static final String SIMLAR_ID1_PASSWORD_HASH = "5c3d66f5a3928cca2821d711a2c016bb";
    private static final String SIMLAR_ID2               = "*0002*";
    private static final String SIMLAR_ID2_PASSWORD      = "fdfho21j3";
    private static final String SIMLAR_ID_NOT_REGISTERED = "*0003*";

    @Value("${local.server.port}")
    private int                 port;

    @Autowired
    private SubscriberService   subscriberService;

    @SuppressWarnings("unchecked")
    private <T> T requestContactStatus(final Class<T> responseClass, final String login, final String password, final String contacts) {
        final MultiValueMap<String, String> parameter = new LinkedMultiValueMap<>();
        parameter.add("login", login);
        parameter.add("password", password);
        parameter.add("contacts", contacts);

        final String result = new RestTemplate().postForObject("http://localhost:" + port + ContactsController.REQUEST_URL_CONTACTS_STATUS, parameter,
                String.class);

        if (result == null) {
            return null;
        }

        try {
            return (T) JAXBContext.newInstance(responseClass).createUnmarshaller().unmarshal(new StringReader(result));
        } catch (final JAXBException e) {
            logger.severe("JAXBException: for postResult: " + result);
            return null;
        } catch (final ClassCastException e) {
            logger.severe("ClassCastException for postResult: " + result);
            return null;
        }
    }

    @Before
    public void init() {
        subscriberService.save(SimlarId.create(SIMLAR_ID1), SIMLAR_ID1_PASSWORD);
        subscriberService.save(SimlarId.create(SIMLAR_ID2), SIMLAR_ID2_PASSWORD);
    }

    @Test
    public void receiveContactsStatus() {
        final XmlContacts contacts = requestContactStatus(XmlContacts.class, SIMLAR_ID1, SIMLAR_ID1_PASSWORD_HASH, SIMLAR_ID2 + "|" + SIMLAR_ID_NOT_REGISTERED);
        assertNotNull(contacts);
        assertNotNull(contacts.getContacts());
        assertEquals(2, contacts.getContacts().size());
        assertEquals(SIMLAR_ID2, contacts.getContacts().get(0).getSimlarId());
        assertEquals(1, contacts.getContacts().get(0).getStatus());
        assertEquals(SIMLAR_ID_NOT_REGISTERED, contacts.getContacts().get(1).getSimlarId());
        assertEquals(0, contacts.getContacts().get(1).getStatus());
    }

    private boolean loginWithWrongCredentials(final String username, final String password) {
        final XmlError error = requestContactStatus(XmlError.class, username, password, "*0002*|*0003*");
        return error != null && error.getId() == 10;
    }

    @Test
    public void loginWithWrongCredentials() {
        assertTrue(loginWithWrongCredentials(null, "xxxxxxx"));
        assertTrue(loginWithWrongCredentials("*", "xxxxxxx"));
        assertTrue(loginWithWrongCredentials(SIMLAR_ID1, null));
        assertTrue(loginWithWrongCredentials(SIMLAR_ID1, "xxxxxxx"));
    }

    private boolean loginWithEmptyContactList(final String contactList) {
        final XmlContacts response= requestContactStatus(XmlContacts.class, SIMLAR_ID1, SIMLAR_ID1_PASSWORD_HASH, contactList);
        return response != null && response.getContacts() == null;
    }

    @Test
    public void loginWithEmptyContactList() {
        assertTrue(loginWithEmptyContactList(null));
        assertTrue(loginWithEmptyContactList(""));
        assertTrue(loginWithEmptyContactList(SIMLAR_ID2 + " " + SIMLAR_ID_NOT_REGISTERED));
    }
}
