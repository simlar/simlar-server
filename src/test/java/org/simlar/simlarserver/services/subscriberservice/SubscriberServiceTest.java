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
import org.simlar.simlarserver.services.SharedSettings;
import org.simlar.simlarserver.testdata.TestUser;
import org.simlar.simlarserver.utils.SimlarId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
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

        assertTrue(subscriberRepository.findHa1ByUsernameAndDomain(simlarId, sharedSettings.getDomain()).isEmpty());
        subscriberService.save(SimlarId.create(simlarId), "sdflkj34gd3F");
        assertEquals(1, subscriberRepository.findHa1ByUsernameAndDomain(simlarId, sharedSettings.getDomain()).size());
        assertEquals("988395d60155f38eae4bb15657275d13", subscriberRepository.findHa1ByUsernameAndDomain(simlarId, sharedSettings.getDomain()).get(0));
        subscriberService.save(SimlarId.create(simlarId), "FdUfFjH34gd3");
        assertEquals(1, subscriberRepository.findHa1ByUsernameAndDomain(simlarId, sharedSettings.getDomain()).size());
        assertEquals("fb14b3adf050f0e9b71bf866702188b5", subscriberRepository.findHa1ByUsernameAndDomain(simlarId, sharedSettings.getDomain()).get(0));
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
    public void testGetStatus() {
        final SimlarId simlarIdSaved = SimlarId.create("*2002*");
        final SimlarId simlarIdNotSaved = SimlarId.create("*2003*");

        assertEquals(0, subscriberService.getStatus(null));
        assertEquals(0, subscriberService.getStatus(simlarIdSaved));
        assertEquals(0, subscriberService.getStatus(simlarIdNotSaved));
        subscriberService.save(simlarIdSaved, "xxxxxx");
        assertEquals(1, subscriberService.getStatus(simlarIdSaved));
        assertEquals(0, subscriberService.getStatus(simlarIdNotSaved));
        subscriberService.save(simlarIdSaved, "as234f2dsd");
        assertEquals(1, subscriberService.getStatus(simlarIdSaved));
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

    @DirtiesContext
    @Test
    public void testUsers() {
        for (final TestUser user : TestUser.values()) {
            subscriberService.save(SimlarId.create(user.getSimlarId()), user.getPassword());
            assertTrue(subscriberService.checkCredentials(user.getSimlarId(), user.getPasswordHash()));
        }
    }
}
