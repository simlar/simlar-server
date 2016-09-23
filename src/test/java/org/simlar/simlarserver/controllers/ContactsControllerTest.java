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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.simlar.simlarserver.testdata.TestUser;
import org.simlar.simlarserver.xml.XmlContact;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@SuppressFBWarnings({"PRMC_POSSIBLY_REDUNDANT_METHOD_CALLS", "UCPM_USE_CHARACTER_PARAMETERIZED_METHOD"})
@RunWith(SpringJUnit4ClassRunner.class)
public final class ContactsControllerTest extends ContactsControllerBaseTest {
    private void wrongCredentials(final String username, final String password) {
        assertEquals(10, requestError(username, password, "*0002*|*0003*"));
    }

    @Test
    public void testWrongCredentials() {
        wrongCredentials(null, "xxxxxxx");
        wrongCredentials("*", "xxxxxxx");
        wrongCredentials(TestUser.U1.getSimlarId(), null);
        wrongCredentials(TestUser.U1.getSimlarId(), "xxxxxxx");
    }

    private void emptyContactList(final String contactList) {
        assertNull(requestContactList(TestUser.U1, contactList));
    }

    @Test
    public void testEmptyContactList() {
        emptyContactList(null);
        emptyContactList("");
        emptyContactList(TestUser.U2.getSimlarId() + ' ' + TestUser.SIMLAR_ID_NOT_REGISTERED);
    }

    @Test
    public void testReceiveContactsStatus() {
        final List<XmlContact> contacts = requestContactList(TestUser.U1, TestUser.U2.getSimlarId() + '|' + TestUser.SIMLAR_ID_NOT_REGISTERED);
        assertNotNull(contacts);
        assertEquals(2, contacts.size());
        assertEquals(TestUser.U2.getSimlarId(), contacts.get(0).getSimlarId());
        assertEquals(1, contacts.get(0).getStatus());
        assertEquals(TestUser.SIMLAR_ID_NOT_REGISTERED, contacts.get(1).getSimlarId());
        assertEquals(0, contacts.get(1).getStatus());
    }
}
