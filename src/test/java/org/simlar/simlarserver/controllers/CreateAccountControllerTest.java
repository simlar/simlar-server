/*
 * Copyright (C) 2017 The Simlar Authors.
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
import org.simlar.simlarserver.xml.XmlError;
import org.simlar.simlarserver.xml.XmlSuccessCreateAccountRequest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@SuppressFBWarnings("PRMC_POSSIBLY_REDUNDANT_METHOD_CALLS")
@TestPropertySource(properties = "domain = sip.simlar.org") // domain is an essential part of the callback url
@RunWith(SpringRunner.class)
public final class CreateAccountControllerTest extends BaseControllerTest {
    private <T> T postCreateAccount(final Class<T> responseClass, final String command, final String telephoneNumber, final String smsText) {
        return postRequest(responseClass, CreateAccountController.REQUEST_PATH, createParameters(new String[][] {
                { "command", command },
                { "telephoneNumber", telephoneNumber },
                { "smsText", smsText }
        }));
    }

    @Test
    public void testRequestSuccess() {
        final XmlSuccessCreateAccountRequest success = postCreateAccount(XmlSuccessCreateAccountRequest.class, "request","+15005550006", "android-en");
        assertNotNull(success);
        //assertEquals("*15005550006*", success.getSimlarId()); /// TODO
        assertNotNull(success.getPassword());
        assertEquals("password '" + success.getPassword() + "' does not match expected size",12, success.getPassword().length());
    }

    @Test
    public void testRequestFailedToSendSms() {
        final XmlError response = postCreateAccount(XmlError.class, "request","+15005550001", "android-ios");
        assertNotNull(response);
        assertEquals(24, response.getId());
    }
}
