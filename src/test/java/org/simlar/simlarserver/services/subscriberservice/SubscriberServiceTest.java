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

package org.simlar.simlarserver.services.subscriberservice;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.simlar.simlarserver.SimlarServer;
import org.simlar.simlarserver.database.repositories.SubscriberRepository;
import org.simlar.simlarserver.helper.SimlarIds;
import org.simlar.simlarserver.services.SharedSettings;
import org.simlar.simlarserver.testdata.TestUser;
import org.simlar.simlarserver.utils.SimlarId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SimlarServer.class)
public final class SubscriberServiceTest {
    @Autowired
    private SubscriberService subscriberService;

    @Autowired
    private SubscriberRepository subscriberRepository;

    @Autowired
    private SharedSettings sharedSettings;

    @Test(expected = IllegalArgumentException.class)
    public void testSaveNoSimlarIdNoPassword() {
        subscriberService.save(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSaveNoSimlarId() {
        subscriberService.save(null, "sdflkj34gd3F");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSaveNoPassword() {
        subscriberService.save(SimlarId.create("*2000*"), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSaveEmptyPassword() {
        subscriberService.save(SimlarId.create("*2000*"), "");
    }

    @Test
    public void testSaveSuccess() {
        final String simlarId = "*2000*";

        assertTrue(subscriberRepository.findHa1ByUsernameAndDomain(simlarId, sharedSettings.domain()).isEmpty());
        subscriberService.save(SimlarId.create(simlarId), "sdflkj34gd3F");
        assertEquals(1, subscriberRepository.findHa1ByUsernameAndDomain(simlarId, sharedSettings.domain()).size());
        assertEquals("988395d60155f38eae4bb15657275d13", subscriberRepository.findHa1ByUsernameAndDomain(simlarId, sharedSettings.domain()).get(0));
        subscriberService.save(SimlarId.create(simlarId), "FdUfFjH34gd3");
        assertEquals(1, subscriberRepository.findHa1ByUsernameAndDomain(simlarId, sharedSettings.domain()).size());
        assertEquals("fb14b3adf050f0e9b71bf866702188b5", subscriberRepository.findHa1ByUsernameAndDomain(simlarId, sharedSettings.domain()).get(0));
    }

    @SuppressWarnings("TooBroadScope")
    @Test
    public void testCheckCredentials() {
        final String simlarId = "*2001*";
        final String password = "sp4mv02fvu";
        final String ha1 = "9fcff156b76304b5db012cbd5f0b0916";

        assertFalse(subscriberService.checkCredentials("*", ha1));
        assertFalse(subscriberService.checkCredentials(simlarId, ha1));
        subscriberService.save(SimlarId.create(simlarId), password);
        assertFalse(subscriberService.checkCredentials("*", ha1));
        assertFalse(subscriberService.checkCredentials(simlarId, password));
        assertTrue(subscriberService.checkCredentials(simlarId, ha1));
        assertFalse(subscriberService.checkCredentials(null, null));
        assertFalse(subscriberService.checkCredentials(null, ha1));
        assertFalse(subscriberService.checkCredentials(simlarId, null));
    }

    @Test
    public void testGetHa1() {
        final SimlarId simlarIdSaved = SimlarId.create("*2004*");
        final SimlarId simlarIdNotSaved = SimlarId.create("*2005*");

        assertNull(subscriberService.getHa1(null));
        assertNull(subscriberService.getHa1(simlarIdSaved));
        assertNull(subscriberService.getHa1(simlarIdNotSaved));
        subscriberService.save(simlarIdSaved, "xxxxxx");
        assertEquals("9efee90fe330da22762a2493e6805a69", subscriberService.getHa1(simlarIdSaved));
        assertNull(subscriberService.getHa1(simlarIdNotSaved));
        subscriberService.save(simlarIdSaved, "as234f2dsd");
        assertEquals("234f6f5d04f73725a14c8f3f3a664e11", subscriberService.getHa1(simlarIdSaved));
    }

    @Test
    public void testFilterSimlarIdsRegistered() {
        final SimlarId simlarIdSaved = SimlarId.create("*2002*");
        assertNotNull(simlarIdSaved);
        final SimlarId simlarIdNotSaved = SimlarId.create("*2003*");
        assertNotNull(simlarIdNotSaved);

        assertEquals(Collections.emptyList(), subscriberService.filterSimlarIdsRegistered(List.of(simlarIdNotSaved, simlarIdSaved, simlarIdSaved)));

        subscriberService.save(simlarIdSaved, "xxxxxx");
        assertEquals(List.of(simlarIdSaved), subscriberService.filterSimlarIdsRegistered(List.of(simlarIdSaved, simlarIdNotSaved, simlarIdSaved)));

        subscriberService.save(simlarIdSaved, "as234f2dsd");
        assertEquals(List.of(simlarIdSaved), subscriberService.filterSimlarIdsRegistered(List.of(simlarIdSaved, simlarIdSaved, simlarIdNotSaved)));
    }

    @Test
    public void testFilterSimlarIdsRegisteredWithLargeList() {
        subscriberService.save(SimlarId.create("*1*"), "xxxxxx");
        subscriberService.save(SimlarId.create("*1000000*"), "xxxxxx");
        final List<SimlarId> simlarIds = SimlarIds.createContacts(1000000).stream().toList();
        assertEquals(2, subscriberService.filterSimlarIdsRegistered(simlarIds).size());
    }

    @DirtiesContext
    @Test
    public void testUsers() {
        for (final TestUser user : TestUser.values()) {
            subscriberService.save(SimlarId.create(user.getSimlarId()), user.getPassword());
            assertTrue(subscriberService.checkCredentials(user.getSimlarId(), user.getPasswordHash()));
        }
    }

    @Test
    public void testIsRegisteredWithNoSimlarId() {
        assertFalse(subscriberService.isRegistered(null));
    }

    @Test
    public void testIsRegistered() {
        final SimlarId simlarId = SimlarId.create("*2003*");
        assertFalse(subscriberService.isRegistered(simlarId));
        subscriberService.save(simlarId, "somePassword");
        assertTrue(subscriberService.isRegistered(simlarId));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeleteBySimlarIdWithNoSimlarId() {
        subscriberService.deleteBySimlarId(null);
    }

    @Test
    public void testDeleteBySimlarId() {
        final SimlarId simlarId = SimlarId.create("*2004*");

        subscriberService.save(simlarId, "somePassword");
        assertTrue(subscriberService.isRegistered(simlarId));

        subscriberService.deleteBySimlarId(simlarId);
        assertFalse(subscriberService.isRegistered(simlarId));
    }
}
