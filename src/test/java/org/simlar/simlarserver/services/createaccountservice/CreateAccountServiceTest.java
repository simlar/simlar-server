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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.simlar.simlarserver.Application;
import org.simlar.simlarserver.services.settingsservice.SettingsService;
import org.simlar.simlarserver.services.smsservice.SmsService;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorFailedToSendSmsException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorInvalidTelephoneNumberException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorTooManyRequestTriesException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Objects;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public final class CreateAccountServiceTest {
    @Autowired
    private CreateAccountService createAccountService;

    @Autowired
    private SmsService smsService;

    @Autowired
    private SettingsService settingsService;

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();


    @SuppressFBWarnings("UTAO_JUNIT_ASSERTION_ODDITIES_NO_ASSERT")
    @Test
    public void testCreateAccountRequestWithInvalidNumber() {
        expectedException.expect(XmlErrorInvalidTelephoneNumberException.class);
        expectedException.expectMessage("NO-NUMBER");
        expectedException.expectMessage(not(containsString("libphonenumber")));
        createAccountService.createAccountRequest("NO-NUMBER", "", "");
    }

    @SuppressFBWarnings("UTAO_JUNIT_ASSERTION_ODDITIES_NO_ASSERT")
    @Test
    public void testCreateAccountRequestWithInvalidNumberLibphonenumber() {
        expectedException.expect(XmlErrorInvalidTelephoneNumberException.class);
        expectedException.expectMessage("+49163123456");
        expectedException.expectMessage("libphonenumber");
        createAccountService.createAccountRequest("+49163123456", "", "");
    }

    @SuppressFBWarnings("UTAO_JUNIT_ASSERTION_ODDITIES_NO_ASSERT")
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

    private static void assertException(final Class<? extends Exception> expected, final Runnable runnable) {
        //noinspection OverlyBroadCatchBlock
        try {
            runnable.run();
            fail("expected exception not thrown: " + expected.getSimpleName());
        } catch (final Exception exception) {
            if (!Objects.equals(expected, exception.getClass())) {
                throw new AssertionError("expected exception '" + expected.getSimpleName() + "' but '" + exception.getClass().getSimpleName() + "' was thrown", exception);
            }
        }
    }

    @Test
    public void testCreateAccountRequestTelephoneNumberLimit() {
        final String telephoneNumber = "+15005023024";

        final int max = settingsService.getAccountCreationMaxRequestsPerSimlarIdPerDay();
        for (int i = 0; i < max; i++) {
            reset(smsService);
            if ((i & 1) == 0) {
                assertException(XmlErrorFailedToSendSmsException.class, () -> createAccountService.createAccountRequest(telephoneNumber, "", ""));
            } else {
                when(smsService.sendSms(eq(telephoneNumber), anyString())).thenReturn(Boolean.TRUE);
                createAccountService.createAccountRequest(telephoneNumber, "", "");
                verify(smsService).sendSms(eq(telephoneNumber), anyString());
            }
        }

        expectedException.expect(XmlErrorTooManyRequestTriesException.class);
        createAccountService.createAccountRequest(telephoneNumber, "", "");
    }
}
