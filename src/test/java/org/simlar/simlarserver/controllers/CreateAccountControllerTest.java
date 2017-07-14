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
import org.simlar.simlarserver.services.smsservice.SmsService;
import org.simlar.simlarserver.xml.XmlError;
import org.simlar.simlarserver.xml.XmlSuccessCreateAccountRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@SuppressFBWarnings("PRMC_POSSIBLY_REDUNDANT_METHOD_CALLS")
@RunWith(SpringRunner.class)
public final class CreateAccountControllerTest extends BaseControllerTest {
    @SuppressWarnings("CanBeFinal")
    @Autowired
    private SmsService smsService;

    @SuppressWarnings("MethodWithTooManyParameters")
    private <T> T postCreateAccount(final Class<T> responseClass, final boolean callSmsService, final boolean sendSmsResult, final String command, final String telephoneNumber, final String smsText) {
        if (callSmsService) {
            when(smsService.sendSms(eq(telephoneNumber), anyString())).thenReturn(sendSmsResult);
        }

        final T result = postRequest(responseClass, CreateAccountController.REQUEST_PATH, createParameters(new String[][] {
                { "command", command },
                { "telephoneNumber", telephoneNumber },
                { "smsText", smsText }
        }));

        if (callSmsService) {
            verify(smsService).sendSms(eq(telephoneNumber), anyString());
        }

        verifyNoMoreInteractions(smsService);

        return result;
    }

    private void assertPostCreateAccountError(final int expectedErrorId, final boolean callSmsService, final String command, final String telephoneNumber, final String smsText) {
        final XmlError response = postCreateAccount(XmlError.class, callSmsService, false, command,telephoneNumber, smsText);
        assertNotNull(response);
        assertEquals(expectedErrorId, response.getId());
    }

    @Test
    public void testRequestSuccess() {
        final XmlSuccessCreateAccountRequest success = postCreateAccount(XmlSuccessCreateAccountRequest.class, true, true, CreateAccountController.COMMAND_REQUEST,"+15005550006", "android-en");
        assertNotNull(success);
        assertEquals("*15005550006*", success.getSimlarId());
        assertNotNull(success.getPassword());
        assertEquals("password '" + success.getPassword() + "' does not match expected size",12, success.getPassword().length());
    }

    @Test
    public void testRequestFailedToSendSms() {
        assertPostCreateAccountError(24, true, CreateAccountController.COMMAND_REQUEST, "+15005550001", "android-de");
    }

    @Test
    public void testRequestWithWrongCommand() {
        assertPostCreateAccountError(1, false, "xyz", "+15005550006", "android-en");
        assertPostCreateAccountError(1, false, "confirm", "+15005550006", "android-en");
        assertPostCreateAccountError(1, false, null, "+15005550006", "android-en");
    }

    @Test
    public void testRequestWithInvalidTelephoneNumber() {
        assertPostCreateAccountError(22, false, CreateAccountController.COMMAND_REQUEST, "NO-NUMBER", "android-de");
    }
}
