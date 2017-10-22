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
import org.simlar.simlarserver.database.models.AccountCreationRequestCount;
import org.simlar.simlarserver.database.repositories.AccountCreationRequestCountRepository;
import org.simlar.simlarserver.services.settingsservice.SettingsService;
import org.simlar.simlarserver.services.smsservice.SmsService;
import org.simlar.simlarserver.utils.SimlarId;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorFailedToSendSmsException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorInvalidTelephoneNumberException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorNoIpException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorTooManyRequestTriesException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Duration;
import java.util.Objects;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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

    @Autowired
    private AccountCreationRequestCountRepository accountCreationRepository;

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();


    @SuppressFBWarnings("UTAO_JUNIT_ASSERTION_ODDITIES_NO_ASSERT")
    @Test
    public void testCreateAccountRequestWithInvalidNumber() {
        expectedException.expect(XmlErrorInvalidTelephoneNumberException.class);
        expectedException.expectMessage("NO-NUMBER");
        expectedException.expectMessage(not(containsString("libphonenumber")));
        createAccountService.createAccountRequest("NO-NUMBER", "", "192.168.1.1");
    }

    @SuppressFBWarnings("UTAO_JUNIT_ASSERTION_ODDITIES_NO_ASSERT")
    @Test
    public void testCreateAccountRequestWithInvalidNumberLibphonenumber() {
        expectedException.expect(XmlErrorInvalidTelephoneNumberException.class);
        expectedException.expectMessage("+49163123456");
        expectedException.expectMessage("libphonenumber");
        createAccountService.createAccountRequest("+49163123456", "", "192.168.1.1");
    }

    @SuppressFBWarnings("UTAO_JUNIT_ASSERTION_ODDITIES_NO_ASSERT")
    @Test
    public void testCreateAccountRequestWithFailedSms() {
        expectedException.expect(XmlErrorFailedToSendSmsException.class);
        createAccountService.createAccountRequest("+15005550006", "", "192.168.1.1");
    }

    @SuppressFBWarnings("UTAO_JUNIT_ASSERTION_ODDITIES_NO_ASSERT")
    @Test
    public void testCreateAccountRequestWithIpEmpty() {
        expectedException.expect(XmlErrorNoIpException.class);
        createAccountService.createAccountRequest("+15005550006", "", "");
    }

    @SuppressFBWarnings("UTAO_JUNIT_ASSERTION_ODDITIES_NO_ASSERT")
    @Test
    public void testCreateAccountRequestWithIpNull() {
        expectedException.expect(XmlErrorNoIpException.class);
        createAccountService.createAccountRequest("+15005550006", "", null);
    }

    @SuppressFBWarnings("PRMC_POSSIBLY_REDUNDANT_METHOD_CALLS")
    private void assertCreateAccountRequestSuccess(final String telephoneNumber, final String ip) {
        when(smsService.sendSms(eq(telephoneNumber), anyString())).thenReturn(Boolean.TRUE);
        createAccountService.createAccountRequest(telephoneNumber, "", ip);
        verify(smsService).sendSms(eq(telephoneNumber), anyString());
    }

    private void assertCreateAccountRequestSuccess(final String telephoneNumber) {
        assertCreateAccountRequestSuccess(telephoneNumber, "192.168.1.1");
    }

    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    @Test
    public void testCreateAccountRequestSuccess() {
        assertCreateAccountRequestSuccess("+15005510001");
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
                assertException(XmlErrorFailedToSendSmsException.class, () -> createAccountService.createAccountRequest(telephoneNumber, "", "192.168.1.1"));
            } else {
                assertCreateAccountRequestSuccess(telephoneNumber);
            }
        }

        final String simlarId = SimlarId.createWithTelephoneNumber(telephoneNumber).get();
        final AccountCreationRequestCount before = accountCreationRepository.findBySimlarId(simlarId);

        assertException(XmlErrorTooManyRequestTriesException.class, () -> createAccountService.createAccountRequest(telephoneNumber, "", "192.168.1.1"));

        final AccountCreationRequestCount after = accountCreationRepository.findBySimlarId(simlarId);
        assertEquals(before.getConfirmTries(), after.getConfirmTries());
        assertEquals(before.getRequestTries() + 1, after.getRequestTries());
        assertEquals(settingsService.getAccountCreationMaxRequestsPerSimlarIdPerDay() + 1, after.getRequestTries());
        assertEquals(before.getRegistrationCode(), after.getRegistrationCode());
        assertEquals(before.getPassword(), after.getPassword());

        /// check limit reset after a day
        assertNotNull(after.getTimestamp());
        after.setTimestamp(after.getTimestamp().minus(Duration.ofHours(25)));
        accountCreationRepository.save(after);

        reset(smsService);
        assertCreateAccountRequestSuccess(telephoneNumber);

        assertEquals(1, accountCreationRepository.findBySimlarId(simlarId).getRequestTries());
    }

    @SuppressFBWarnings("PRMC_POSSIBLY_REDUNDANT_METHOD_CALLS")
    @Test
    public void testCreateAccountRequestIpLimitWithinOneHour() {
        final String ip = "192.168.23.42";

        final int max = settingsService.getAccountCreationMaxRequestsPerIpPerHour();
        for (int i = 0; i < max; i++) {
            final String telephoneNumber = "+1500502304" + i % 10;
            reset(smsService);
            if ((i & 1) == 0) {
                assertException(XmlErrorFailedToSendSmsException.class, () -> createAccountService.createAccountRequest(telephoneNumber, "", ip));
            } else {
                assertCreateAccountRequestSuccess(telephoneNumber, ip);
            }
        }

        final String telephoneNumber = "+15005023049";
        assertException(XmlErrorTooManyRequestTriesException.class, () -> createAccountService.createAccountRequest(telephoneNumber, "", ip));


        /// check limit reset after an hour
        final AccountCreationRequestCount after = accountCreationRepository.findBySimlarId("*15005023048*");
        assertNotNull(after.getTimestamp());
        after.setTimestamp(after.getTimestamp().minus(Duration.ofMinutes(61)));
        accountCreationRepository.save(after);

        reset(smsService);
        assertCreateAccountRequestSuccess(telephoneNumber, ip);
    }

    @SuppressFBWarnings("PRMC_POSSIBLY_REDUNDANT_METHOD_CALLS")
    @Test
    public void testCreateAccountRequestTotalLimitWithinOneHour() {
        final int max = settingsService.getAccountCreationMaxRequestsTotalPerHour();
        for (int i = 0; i < max; i++) {
            final String telephoneNumber = i % 100 < 10 ? "+1500502214" + i % 10 : "+150050220" + i % 100;
            final String ip = "192.168.42." + i % 256;
            reset(smsService);
            if ((i & 1) == 0) {
                assertException(XmlErrorFailedToSendSmsException.class, () -> createAccountService.createAccountRequest(telephoneNumber, "", ip));
            } else {
                assertCreateAccountRequestSuccess(telephoneNumber, ip);
            }
        }

        final String telephoneNumber = "+15005022149";
        assertException(XmlErrorTooManyRequestTriesException.class, () -> createAccountService.createAccountRequest(telephoneNumber, "", "192.168.1.23"));


        /// check limit reset after an hour
        final AccountCreationRequestCount after = accountCreationRepository.findBySimlarId("*15005022148*");
        assertNotNull(after.getTimestamp());
        after.setTimestamp(after.getTimestamp().minus(Duration.ofMinutes(61)));
        accountCreationRepository.save(after);

        reset(smsService);
        assertCreateAccountRequestSuccess(telephoneNumber, "192.168.1.23");
    }
}
