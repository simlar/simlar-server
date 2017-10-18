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

package org.simlar.simlarserver.services.createaccountservice;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.simlar.simlarserver.Application;
import org.simlar.simlarserver.services.smsservice.SmsService;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorFailedToSendSmsException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorInvalidTelephoneNumberException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public final class CreateAccountServiceTest {
    @Autowired
    private CreateAccountService createAccountService;

    @Autowired
    private SmsService smsService;

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testCreateAccountRequestWithInvalidNumber() {
        expectedException.expect(XmlErrorInvalidTelephoneNumberException.class);
        expectedException.expectMessage("NO-NUMBER");
        expectedException.expectMessage(not(containsString("libphonenumber")));
        createAccountService.createAccountRequest("NO-NUMBER", "", "");
    }

    @Test
    public void testCreateAccountRequestWithInvalidNumberLibphonenumber() {
        expectedException.expect(XmlErrorInvalidTelephoneNumberException.class);
        expectedException.expectMessage("+49163123456");
        expectedException.expectMessage("libphonenumber");
        createAccountService.createAccountRequest("+49163123456", "", "");
    }

    @Test
    public void testCreateAccountRequestWithFailedSms() {
        expectedException.expect(XmlErrorFailedToSendSmsException.class);
        createAccountService.createAccountRequest("+15005550006", "", "");
    }

    @Test
    public void testCreateAccountRequestSuccess() {
        final String telephoneNumber = "+15005510001";
        when(smsService.sendSms(eq(telephoneNumber), anyString())).thenReturn(Boolean.TRUE);
        createAccountService.createAccountRequest(telephoneNumber, "", "");
        verify(smsService).sendSms(eq(telephoneNumber), anyString());
    }
}
