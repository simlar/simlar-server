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

import org.simlar.simlarserver.testdata.TestUser;
import org.simlar.simlarserver.xml.XmlContact;
import org.simlar.simlarserver.xml.XmlContacts;
import org.simlar.simlarserver.xml.XmlError;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;

import static org.junit.Assert.assertNotNull;

public class ContactsControllerBaseTest extends BaseControllerTest {
    @SuppressWarnings("unchecked")
    private <T> T requestContactStatus(final Class<T> responseClass, final String login, final String password, final String contacts) {
        final MultiValueMap<String, String> parameter = new LinkedMultiValueMap<>();
        parameter.add("login", login);
        parameter.add("password", password);
        parameter.add("contacts", contacts);

        return postRequest(responseClass, ContactsController.REQUEST_URL_CONTACTS_STATUS, parameter);
    }

    int requestError(final String username, final String password, final String contacts) {
        final XmlError error = requestContactStatus(XmlError.class, username, password, contacts);
        assertNotNull(error);
        return error.getId();
    }

    List<XmlContact> requestContactList(final TestUser user, final String contactList) {
        final XmlContacts response = requestContactStatus(XmlContacts.class, user.getSimlarId(), user.getPasswordHash(), contactList);
        assertNotNull(response);
        return response.getContacts();
    }
}
