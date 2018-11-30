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
import org.simlar.simlarserver.services.settingsservice.SettingsService;
import org.simlar.simlarserver.testdata.TestUser;
import org.simlar.simlarserver.utils.SimlarId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SimlarServer.class)
public final class SubscriberServiceTest {
    @Autowired
    private SubscriberService subscriberService;

    @Autowired
    private SubscriberRepository subscriberRepository;

    @Autowired
    private SettingsService settingsService;

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
        @SuppressWarnings("TooBroadScope")
        final String simlarId = "*2000*";

        assertTrue(subscriberRepository.findHa1ByUsernameAndDomain(simlarId, settingsService.getDomain()).isEmpty());
        subscriberService.save(SimlarId.create(simlarId), "sdflkj34gd3F");
        assertEquals(1, subscriberRepository.findHa1ByUsernameAndDomain(simlarId, settingsService.getDomain()).size());
        assertEquals("988395d60155f38eae4bb15657275d13", subscriberRepository.findHa1ByUsernameAndDomain(simlarId, settingsService.getDomain()).get(0));
        subscriberService.save(SimlarId.create(simlarId), "FdUfFjH34gd3");
        assertEquals(1, subscriberRepository.findHa1ByUsernameAndDomain(simlarId, settingsService.getDomain()).size());
        assertEquals("fb14b3adf050f0e9b71bf866702188b5", subscriberRepository.findHa1ByUsernameAndDomain(simlarId, settingsService.getDomain()).get(0));
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

    @DirtiesContext
    @Test
    public void testUsers() {
        for (final TestUser user: TestUser.values()) {
            subscriberService.save(SimlarId.create(user.getSimlarId()), user.getPassword());
            assertTrue(subscriberService.checkCredentials(user.getSimlarId(), user.getPasswordHash()));
        }
    }

    @SuppressWarnings("SpellCheckingInspection")
    @Test
    public void testCreateHashHa1() {
        assertEquals("3d2c2bbe810e8510bd7f051d80f5a85e", subscriberService.createHashHa1(SimlarId.create("*2342*"), "53cur3"));
        assertEquals("c7e7a2b130cb4398a3682559a498c025", subscriberService.createHashHa1(SimlarId.create("*2342*"), "53cur4"));
        assertEquals("5abdc6343ca3b039fc5ae1c1c68b22ab", subscriberService.createHashHa1(SimlarId.create("*2343*"), "53cur4"));
    }

    @SuppressWarnings("SpellCheckingInspection")
    @Test
    public void testCreateHashHa1b() {
        assertEquals("c832b701c3840182da6bf33b3c588aa4", subscriberService.createHashHa1b(SimlarId.create("*2342*"), "53cur3"));
        assertEquals("b66abb6dc2a30f94380e0be4d87dcd38", subscriberService.createHashHa1b(SimlarId.create("*2342*"), "53cur4"));
        assertEquals("facc6b39f73e54ea33d8141804c1f951", subscriberService.createHashHa1b(SimlarId.create("*2343*"), "53cur4"));
    }
}
