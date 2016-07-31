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
import static org.junit.Assert.assertNull;

import java.util.List;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.simlar.simlarserver.testdata.TestUser;
import org.simlar.simlarserver.xml.XmlContact;
import org.simlar.simlarserver.xml.XmlContacts;
import org.simlar.simlarserver.xml.XmlError;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@SuppressFBWarnings({"PRMC_POSSIBLY_REDUNDANT_METHOD_CALLS", "UCPM_USE_CHARACTER_PARAMETERIZED_METHOD"})
@RunWith(SpringJUnit4ClassRunner.class)
public final class ContactsControllerTest extends BaseControllerTest {

    @SuppressWarnings("unchecked")
    private <T> T requestContactStatus(final Class<T> responseClass, final String login, final String password, final String contacts) {
        final MultiValueMap<String, String> parameter = new LinkedMultiValueMap<>();
        parameter.add("login", login);
        parameter.add("password", password);
        parameter.add("contacts", contacts);

        return postRequest(responseClass, ContactsController.REQUEST_URL_CONTACTS_STATUS, parameter);
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

    private void loginWithWrongCredentials(final String username, final String password) {
        final XmlError error = requestContactStatus(XmlError.class, username, password, "*0002*|*0003*");
        assertNotNull(error);
        assertEquals(10, error.getId());
    }

    @Test
    public void loginWithWrongCredentials() {
        loginWithWrongCredentials(null, "xxxxxxx");
        loginWithWrongCredentials("*", "xxxxxxx");
        loginWithWrongCredentials(TestUser.get(0).getSimlarId(), null);
        loginWithWrongCredentials(TestUser.get(0).getSimlarId(), "xxxxxxx");
    }

    private void loginWithEmptyContactList(final String contactList) {
        final XmlContacts response = requestContactStatus(XmlContacts.class, TestUser.get(0).getSimlarId(), TestUser.get(0).getPasswordHash(), contactList);
        assertNotNull(response);
        assertNull(response.getContacts());
    }

    @Test
    public void loginWithEmptyContactList() {
        loginWithEmptyContactList(null);
        loginWithEmptyContactList("");
        loginWithEmptyContactList(TestUser.get(1).getSimlarId() + " " + TestUser.SIMLAR_ID_NOT_REGISTERED);
    }
}
