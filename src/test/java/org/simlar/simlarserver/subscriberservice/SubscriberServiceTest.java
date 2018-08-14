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

package org.simlar.simlarserver.subscriberservice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.simlar.simlarserver.Application;
import org.simlar.simlarserver.services.subscriberservice.SubscriberService;
import org.simlar.simlarserver.testdata.TestUser;
import org.simlar.simlarserver.utils.SimlarId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
public final class SubscriberServiceTest {
    @SuppressWarnings("CanBeFinal")
    @Autowired
    private SubscriberService subscriberService;

    @Test
    public void save() {
        final String simlarId = "*0000*";

        assertFalse(subscriberService.save(null, null));
        assertFalse(subscriberService.save(null, "sdflkj34gd3F"));
        assertFalse(subscriberService.save(SimlarId.create(simlarId), null));
        assertFalse(subscriberService.save(SimlarId.create(simlarId), ""));
        assertTrue(subscriberService.save(SimlarId.create(simlarId), "sdflkj34gd3F"));
        assertTrue(subscriberService.save(SimlarId.create(simlarId), "FdUfFjH34gd3"));
    }

    @SuppressFBWarnings("PRMC_POSSIBLY_REDUNDANT_METHOD_CALLS")
    @Test
    public void checkCredentials() {
        final String simlarId = "*0001*";
        final String password = "sp4mv02fvu";
        final String ha1 = "1ee89de73dccb07194b19a25fdfad653";

        assertFalse(subscriberService.checkCredentials("*", ha1));
        assertFalse(subscriberService.checkCredentials(simlarId, ha1));
        assertTrue(subscriberService.save(SimlarId.create(simlarId), password));
        assertFalse(subscriberService.checkCredentials("*", ha1));
        assertFalse(subscriberService.checkCredentials(simlarId, password));
        assertTrue(subscriberService.checkCredentials(simlarId, ha1));
        assertFalse(subscriberService.checkCredentials(null, null));
        assertFalse(subscriberService.checkCredentials(null, ha1));
        assertFalse(subscriberService.checkCredentials(simlarId, null));
    }

    @Test
    public void getStatus() {
        final SimlarId simlarIdSaved = SimlarId.create("*0002*");
        final SimlarId simlarIdNotSaved = SimlarId.create("*0003*");

        assertEquals(0, subscriberService.getStatus(null));
        assertEquals(0, subscriberService.getStatus(simlarIdSaved));
        assertEquals(0, subscriberService.getStatus(simlarIdNotSaved));
        assertTrue(subscriberService.save(simlarIdSaved, "xxxxxx"));
        assertEquals(1, subscriberService.getStatus(simlarIdSaved));
        assertEquals(0, subscriberService.getStatus(simlarIdNotSaved));
        assertTrue(subscriberService.save(simlarIdSaved, "as234f2dsd"));
        assertEquals(1, subscriberService.getStatus(simlarIdSaved));
    }

    @DirtiesContext
    @Test
    public void testUsers() {
        for (final TestUser user: TestUser.USERS) {
            assertTrue(subscriberService.save(SimlarId.create(user.getSimlarId()), user.getPassword()));
            assertTrue(subscriberService.checkCredentials(user.getSimlarId(), user.getPasswordHash()));
        }
    }
}
