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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.simlar.simlarserver.Application;
import org.simlar.simlarserver.services.subscriberservice.SubscriberService;
import org.simlar.simlarserver.testdata.TestUser;
import org.simlar.simlarserver.utils.SimlarId;
import org.simlar.simlarserver.xml.XmlContact;
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
    private static final Logger LOGGER                   = Logger.getLogger(ContactsControllerTest.class.getName());

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
            LOGGER.severe("JAXBException: for postResult: " + result);
            return null;
        } catch (final ClassCastException e) {
            LOGGER.severe("ClassCastException for postResult: " + result);
            return null;
        }
    }

    @Before
    public void init() {
        subscriberService.save(SimlarId.create(TestUser.get(0).getSimlarId()), TestUser.get(0).getPassword());
        subscriberService.save(SimlarId.create(TestUser.get(1).getSimlarId()), TestUser.get(1).getPassword());
    }

    @Test
    public void receiveContactsStatus() {
        final XmlContacts xmlContacts = requestContactStatus(XmlContacts.class, TestUser.get(0).getSimlarId(), TestUser.get(0).getPasswordHash(), TestUser.get(1).getSimlarId() + "|" + TestUser.SIMLAR_ID_NOT_REGISTERED);
        assertNotNull(xmlContacts);
        final List<XmlContact> contacts = xmlContacts.getContacts();
        assertNotNull(contacts);
        assertEquals(2, contacts.size());
        assertEquals(TestUser.get(1).getSimlarId(), contacts.get(0).getSimlarId());
        assertEquals(1, contacts.get(0).getStatus());
        assertEquals(TestUser.SIMLAR_ID_NOT_REGISTERED, contacts.get(1).getSimlarId());
        assertEquals(0, contacts.get(1).getStatus());
    }

    private boolean loginWithWrongCredentials(final String username, final String password) {
        final XmlError error = requestContactStatus(XmlError.class, username, password, "*0002*|*0003*");
        return error != null && error.getId() == 10;
    }

    @Test
    public void loginWithWrongCredentials() {
        assertTrue(loginWithWrongCredentials(null, "xxxxxxx"));
        assertTrue(loginWithWrongCredentials("*", "xxxxxxx"));
        assertTrue(loginWithWrongCredentials(TestUser.get(0).getSimlarId(), null));
        assertTrue(loginWithWrongCredentials(TestUser.get(0).getSimlarId(), "xxxxxxx"));
    }

    private boolean loginWithEmptyContactList(final String contactList) {
        final XmlContacts response = requestContactStatus(XmlContacts.class, TestUser.get(0).getSimlarId(), TestUser.get(0).getPasswordHash(), contactList);
        return response != null && response.getContacts() == null;
    }

    @Test
    public void loginWithEmptyContactList() {
        assertTrue(loginWithEmptyContactList(null));
        assertTrue(loginWithEmptyContactList(""));
        assertTrue(loginWithEmptyContactList(TestUser.get(1).getSimlarId() + " " + TestUser.SIMLAR_ID_NOT_REGISTERED));
    }
}
