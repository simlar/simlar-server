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

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.simlar.simlarserver.helper.SimlarIds;
import org.simlar.simlarserver.testdata.TestUser;
import org.simlar.simlarserver.utils.SimlarId;
import org.simlar.simlarserver.xml.XmlContact;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@Slf4j
@DirtiesContext
@RunWith(SpringRunner.class)
public final class ContactsControllerDelayTest extends ContactsControllerBaseTest {
    private static String pipeJoin(final Collection<SimlarId> simlarIds) {
        assertNotNull(simlarIds);
        return simlarIds.stream().map(SimlarId::get).collect(Collectors.joining("|"));
    }

    @Test
    public void testPipeJoin() {
        assertEquals("*1111*|*9999*", pipeJoin(Arrays.asList(SimlarId.create("*1111*"), SimlarId.create("*9999*"))));
    }

    private void assertRequestContactListSuccess(final TestUser user, final int amount) {
        final MultiValueMap<String, String> parameters = createParameters( new String[][] {
                { "login", user.getSimlarId() },
                { "password", user.getPasswordHash() },
                { "contacts", pipeJoin(SimlarIds.createContacts(amount)) }
        });

        final RestTemplate restTemplate = new RestTemplate();

        final long begin = System.currentTimeMillis();
        final ResponseEntity<String> entity = restTemplate.postForEntity(getBaseUrl() + ContactsController.REQUEST_PATH, parameters, String.class);
        log.info("request 1 took: {}ms", System.currentTimeMillis() - begin);
        assertEquals(entity.getStatusCode(), HttpStatus.OK);

        final long beginOld = System.currentTimeMillis();
        final List<XmlContact> response = requestContactList(user, pipeJoin(SimlarIds.createContacts(amount)));
        assertNotNull(response);
        assertEquals(amount, response.size());
        log.info("request 2 took: {}ms", System.currentTimeMillis() - beginOld);

        final long begin3 = System.currentTimeMillis();
        final ResponseEntity<String> entity2 = restTemplate.postForEntity(getBaseUrl() + ContactsController.REQUEST_PATH, parameters, String.class);
        log.info("request 3 took: {}ms", System.currentTimeMillis() - begin3);
        assertEquals(entity2.getStatusCode(), HttpStatus.OK);

        final long beginOld2 = System.currentTimeMillis();
        final List<XmlContact> response2 = requestContactList(user, pipeJoin(SimlarIds.createContacts(amount)));
        assertNotNull(response2);
        assertEquals(amount, response2.size());
        log.info("request 4 took: {}ms", System.currentTimeMillis() - beginOld2);
    }

    private void assertRequestContactListSuccess(final int amount) {
        assertRequestContactListSuccess(TestUser.U1, amount);
    }

    private void assertRequestTooManyContacts(final int amount) {
        assertEquals(50, requestError(TestUser.U1.getSimlarId(), TestUser.U1.getPasswordHash(), pipeJoin(SimlarIds.createContacts(amount))));
    }

    @Test
    public void testRequestTooManyContacts() {
        assertRequestContactListSuccess(23);
        assertRequestContactListSuccess(5000);
        assertRequestContactListSuccess(5000);
        assertRequestTooManyContacts(100000);
        assertRequestTooManyContacts(5000);
        assertRequestTooManyContacts(5000);
    }

    private static void assertLessEquals(final long l1, final long l2) {
        assertTrue(l1 + " <= " + l2, l1 <= l2);
    }

    @Test
    public void testNoDelay() {
        final long begin = System.currentTimeMillis();
        assertRequestContactListSuccess(TestUser.U2, 1);
        final long elapsed = System.currentTimeMillis() - begin;
        assertLessEquals(elapsed, 500);
    }

    @Test
    public void testOneSecondDelay() {
        final long begin = System.currentTimeMillis();
        assertRequestContactListSuccess(TestUser.U3, 6000);
        final long elapsed = System.currentTimeMillis() - begin;
        assertLessEquals(1000, elapsed);
        assertLessEquals(elapsed, 2000);
    }
}
