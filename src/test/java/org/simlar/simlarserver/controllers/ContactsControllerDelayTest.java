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
import org.simlar.simlarserver.helper.SimlarIds;
import org.simlar.simlarserver.testdata.TestUser;
import org.simlar.simlarserver.utils.SimlarId;
import org.simlar.simlarserver.xml.XmlContact;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@SuppressFBWarnings({"PRMC_POSSIBLY_REDUNDANT_METHOD_CALLS", "UCPM_USE_CHARACTER_PARAMETERIZED_METHOD"})
@RunWith(SpringJUnit4ClassRunner.class)
public final class ContactsControllerDelayTest extends ContactsControllerBaseTest {
    private static String pipeJoin(final Collection<SimlarId> simlarIds) {
        assertNotNull(simlarIds);
        return String.join("|", simlarIds.stream().map(SimlarId::get).collect(Collectors.toList()));
    }

    private void requestContactListSuccess(final int amount) {
        final List<XmlContact> response = requestContactList(pipeJoin(SimlarIds.createContacts(amount)));
        assertNotNull(response);
        assertEquals(amount, response.size());
    }

    private void requestedTooManyContacts(final int amount) {
        assertEquals(50, requestError(TestUser.get(0).getSimlarId(), TestUser.get(0).getPasswordHash(), pipeJoin(SimlarIds.createContacts(amount))));
    }

    @Test
    public void requestTooManyContacts() {
        requestContactListSuccess(23);
        requestContactListSuccess(5000);
        requestContactListSuccess(5000);
        requestedTooManyContacts(100000);
        requestedTooManyContacts(5000);
        requestedTooManyContacts(5000);
    }
}
