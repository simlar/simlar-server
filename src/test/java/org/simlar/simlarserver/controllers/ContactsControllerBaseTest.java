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

import org.simlar.simlarserver.testdata.TestUser;
import org.simlar.simlarserver.xml.XmlContact;
import org.simlar.simlarserver.xml.XmlContacts;
import org.simlar.simlarserver.xml.XmlError;

import java.util.List;

import static org.junit.Assert.assertNotNull;

class ContactsControllerBaseTest extends BaseControllerTest {
    private <T> T requestContactStatus(final Class<T> responseClass, final String login, final String password, final String contacts) {
        return postRequest(responseClass, ContactsController.REQUEST_PATH, createParameters(new String[][] {
                { "login", login },
                { "password", password },
                { "contacts", contacts }
        }));
    }

    final int requestError(final String username, final String password, final String contacts) {
        final XmlError error = requestContactStatus(XmlError.class, username, password, contacts);
        assertNotNull(error);
        return error.getId();
    }

    final List<XmlContact> requestContactList(final TestUser user, final String contactList) {
        final XmlContacts response = requestContactStatus(XmlContacts.class, user.getSimlarId(), user.getPasswordHash(), contactList);
        assertNotNull(response);
        return response.getContacts();
    }
}
