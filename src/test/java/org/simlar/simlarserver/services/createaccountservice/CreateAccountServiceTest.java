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
import org.simlar.simlarserver.SimlarServer;
import org.simlar.simlarserver.database.models.AccountCreationRequestCount;
import org.simlar.simlarserver.database.repositories.AccountCreationRequestCountRepository;
import org.simlar.simlarserver.services.smsservice.SmsService;
import org.simlar.simlarserver.utils.SimlarIdHelper;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorCallNotAllowedAtTheMomentException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorFailedToSendSmsException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorInvalidTelephoneNumberException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorNoIpException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorTooManyRequestTriesException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalAmount;
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

@TestPropertySource(properties = {
        "create.account.alertSmsNumbers = 1234, 5678",
        "create.account.maxRequestsPerIpPerHour = 12",
        "create.account.maxRequestsTotalPerHour = 15",
        "create.account.maxRequestsTotalPerDay = 30",
        "create.account.regionals[0].regionCode = 160",
        "create.account.regionals[0].maxRequestsPerHour=4"})
@SuppressWarnings({"PMD.AvoidUsingHardCodedIP", "ClassWithTooManyMethods"})
@RunWith(SpringRunner.class)
@SpringBootTest(classes = SimlarServer.class)
public final class CreateAccountServiceTest {
    @Autowired
    private CreateAccountService createAccountService;

    @Autowired
    private SmsService smsService;

    @Autowired
    private CreateAccountSettingsService settingsService;

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

    private AccountRequest assertCreateAccountRequestSuccess(final String telephoneNumber, final String ip, final Instant timestamp) {
        when(smsService.sendSms(eq(telephoneNumber), anyString())).thenReturn(Boolean.TRUE);
        final AccountRequest accountRequest = createAccountService.createAccountRequest(telephoneNumber, "", ip, timestamp);
        verify(smsService).sendSms(eq(telephoneNumber), anyString());
        assertNotNull(accountRequest);
        assertNotNull(accountRequest.getSimlarId());
        assertNotNull(accountRequest.getPassword());

        reset(smsService);

        return accountRequest;
    }

    private void assertCreateAccountRequestSuccess(final String telephoneNumber) {
        assertCreateAccountRequestSuccess(telephoneNumber, "192.168.1.1", Instant.now());
    }

    private void assertCreateAccountRequestSuccess(final String telephoneNumber, final String ip) {
        assertCreateAccountRequestSuccess(telephoneNumber, ip, Instant.now());
    }

    @SuppressWarnings("SameParameterValue")
    private AccountRequest assertCreateAccountRequestSuccess(final String telephoneNumber, final Instant timestamp) {
        return assertCreateAccountRequestSuccess(telephoneNumber, "192.168.1.1", timestamp);
    }

    @SuppressWarnings("MethodWithMultipleLoops")
    private void assertCreateAccountRequestSuccessWithSmsAlert(final String telephoneNumber, final String ip) {
        for (final String alertNumber: settingsService.getAlertSmsNumbers()) {
            when(smsService.sendSms(eq(alertNumber), anyString())).thenReturn(Boolean.TRUE);
        }
        when(smsService.sendSms(eq(telephoneNumber), anyString())).thenReturn(Boolean.TRUE);

        createAccountService.createAccountRequest(telephoneNumber, "", ip);

        for (final String alertNumber: settingsService.getAlertSmsNumbers()) {
            verify(smsService).sendSms(eq(alertNumber), anyString());
        }
        verify(smsService).sendSms(eq(telephoneNumber), anyString());
    }

    @DirtiesContext
    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    @Test
    public void testCreateAccountRequestSuccess() {
        assertCreateAccountRequestSuccess("+15005510001");
    }

    @SuppressWarnings("PMD.DoNotUseThreads")
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

    @DirtiesContext
    @Test
    public void testCreateAccountRequestTelephoneNumberLimit() {
        final String telephoneNumber = "+15005023024";

        final int max = settingsService.getMaxRequestsPerSimlarIdPerDay();
        for (int i = 0; i < max; i++) {
            reset(smsService);
            if ((i & 1) == 0) {
                //noinspection ObjectAllocationInLoop
                assertException(XmlErrorFailedToSendSmsException.class, () -> createAccountService.createAccountRequest(telephoneNumber, "", "192.168.1.1"));
            } else {
                assertCreateAccountRequestSuccess(telephoneNumber);
            }
        }

        final String simlarId = SimlarIdHelper.createSimlarId(telephoneNumber);
        final AccountCreationRequestCount before = accountCreationRepository.findBySimlarId(simlarId);

        assertException(XmlErrorTooManyRequestTriesException.class, () -> createAccountService.createAccountRequest(telephoneNumber, "", "192.168.1.1"));

        final AccountCreationRequestCount after = accountCreationRepository.findBySimlarId(simlarId);
        assertEquals(before.getConfirmTries(), after.getConfirmTries());
        assertEquals(before.getRequestTries() + 1, after.getRequestTries());
        assertEquals(settingsService.getMaxRequestsPerSimlarIdPerDay() + 1, after.getRequestTries());
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

    private void reduceAccountCreationTimestamp(final String simlarId, final TemporalAmount minus) {
        final AccountCreationRequestCount after = accountCreationRepository.findBySimlarId(simlarId);
        assertNotNull(after.getTimestamp());
        after.setTimestamp(after.getTimestamp().minus(minus));
        accountCreationRepository.save(after);
    }

    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    @DirtiesContext
    @Test
    public void testCreateAccountRequestIpLimitWithinOneHour() {
        final String ip = "192.168.23.42";

        final int max = settingsService.getMaxRequestsPerIpPerHour();
        for (int i = 0; i < max; i++) {
            final String telephoneNumber = "+1500502304" + i % 10;
            reset(smsService);
            if ((i & 1) == 0) {
                //noinspection ObjectAllocationInLoop
                assertException(XmlErrorFailedToSendSmsException.class, () -> createAccountService.createAccountRequest(telephoneNumber, "", ip));
            } else {
                assertCreateAccountRequestSuccess(telephoneNumber, ip);
            }
        }

        final String telephoneNumber = "+15005023049";
        assertException(XmlErrorTooManyRequestTriesException.class, () -> createAccountService.createAccountRequest(telephoneNumber, "", ip));


        /// check limit reset after an hour
        reduceAccountCreationTimestamp("*15005023040*", Duration.ofMinutes(61));
        reset(smsService);
        assertCreateAccountRequestSuccess(telephoneNumber, ip);
    }

    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    @DirtiesContext
    @Test
    public void testCreateAccountRequestTotalLimitWithinOneHour() {
        final int max = settingsService.getMaxRequestsTotalPerHour();
        for (int i = 0; i < max; i++) {
            final String telephoneNumber = "+1500502214" + i % 10;
            final String ip = "192.168.42." + (i % 255 + 1);
            reset(smsService);
            if (i == max / 2 - 1) {
                assertCreateAccountRequestSuccessWithSmsAlert(telephoneNumber, ip);
            } else {
                if ((i & 1) == 0) {
                    //noinspection ObjectAllocationInLoop
                    assertException(XmlErrorFailedToSendSmsException.class, () -> createAccountService.createAccountRequest(telephoneNumber, "", ip));
                } else {
                    assertCreateAccountRequestSuccess(telephoneNumber, ip);
                }
            }
        }

        final String telephoneNumber = "+15005022149";
        assertException(XmlErrorTooManyRequestTriesException.class, () -> createAccountService.createAccountRequest(telephoneNumber, "", "192.168.1.23"));


        /// check limit reset after an hour
        reduceAccountCreationTimestamp("*15005022141*", Duration.ofMinutes(61));
        reset(smsService);
        assertCreateAccountRequestSuccess(telephoneNumber, "192.168.1.23");
    }

    @DirtiesContext
    @Test
    public void testCreateAccountRequestTotalLimitWithinOneDay() {
        final int max = settingsService.getMaxRequestsTotalPerDay();
        for (int i = 0; i < max; i++) {
            reset(smsService);
            final String number = "1500501201" + i % 10;
            final String telephoneNumber = '+' + number;
            final String ip = "192.168.42." + (i % 255 + 1);

            if (i == max / 2 - 1) {
                assertCreateAccountRequestSuccessWithSmsAlert(telephoneNumber, ip);
            } else {
                if ((i & 1) == 0) {
                    //noinspection ObjectAllocationInLoop
                    assertException(XmlErrorFailedToSendSmsException.class, () -> createAccountService.createAccountRequest(telephoneNumber, "", ip));
                } else {
                    assertCreateAccountRequestSuccess(telephoneNumber, ip);
                }
            }

            // spread entries over the day
            final AccountCreationRequestCount after = accountCreationRepository.findBySimlarId('*' + number + '*');
            assertNotNull(after.getTimestamp());
            after.setTimestamp(after.getTimestamp().minus(Duration.ofMinutes((i % 24) * 60L)));
            accountCreationRepository.save(after);
        }

        final String telephoneNumber = "+15005012149";
        assertException(XmlErrorTooManyRequestTriesException.class, () -> createAccountService.createAccountRequest(telephoneNumber, "", "192.168.1.23"));


        /// check limit reset after a day
        reduceAccountCreationTimestamp("*15005012011*", Duration.ofHours(25));
        reset(smsService);
        assertCreateAccountRequestSuccess(telephoneNumber, "192.168.1.23");
    }

    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    @DirtiesContext
    @Test
    public void testCreateAccountRequestTotalLimitWithinOneHourRegionalLimit() {
        final int max = settingsService.getRegionals().get(0).getMaxRequestsPerHour();
        for (int i = 0; i < max; i++) {
            final String telephoneNumber = "+1600502214" + i % 10;
            final String ip = "192.168.23." + (i % 255 + 1);
            reset(smsService);

            if ((i & 1) == 0) {
                //noinspection ObjectAllocationInLoop
                assertException(XmlErrorFailedToSendSmsException.class, () -> createAccountService.createAccountRequest(telephoneNumber, "", ip));
            } else {
                assertCreateAccountRequestSuccess(telephoneNumber, ip);
            }
        }

        final String telephoneNumber = "+16005022140";
        assertException(XmlErrorTooManyRequestTriesException.class, () -> createAccountService.createAccountRequest(telephoneNumber, "", "192.168.1.23"));

        /// check other numbers work
        assertCreateAccountRequestSuccess("+15005022149", "192.168.1.23");

        /// check limit reset after an hour
        reduceAccountCreationTimestamp("*16005022141*", Duration.ofMinutes(61));
        reduceAccountCreationTimestamp("*16005022142*", Duration.ofMinutes(61));
        reset(smsService);
        assertCreateAccountRequestSuccess(telephoneNumber, "192.168.1.23");
    }

    @SuppressWarnings("SameParameterValue")
    private void assertCreateAccountCallSuccess(final String telephoneNumber, final String password, final Instant timestamp) {
        when(smsService.call(eq(telephoneNumber), anyString())).thenReturn(Boolean.TRUE);

        createAccountService.call(telephoneNumber, password, timestamp);

        verify(smsService).call(eq(telephoneNumber), anyString());
    }

    @SuppressWarnings("SameParameterValue")
    private void assertCreateAccountCallError(final Class<? extends XmlErrorException> error, final String telephoneNumber, final String password, final Instant timestamp) {
        assertException(error, () -> createAccountService.call(telephoneNumber, password, timestamp));
    }

    @DirtiesContext
    @Test
    public void testTooManyCalls() {
        final String telephoneNumber = "+15005012150";
        Instant now = Instant.now();

        final int max = settingsService.getMaxCalls();
        for (int i = 0; i < max; ++i) {
            final AccountRequest accountRequest = assertCreateAccountRequestSuccess(telephoneNumber, now);
            now = now.plusSeconds(settingsService.getCallDelaySecondsMin() + 2);
            assertCreateAccountCallSuccess(telephoneNumber, accountRequest.getPassword(), now);
        }

        final AccountRequest accountRequest = assertCreateAccountRequestSuccess(telephoneNumber, now);
        now = now.plusSeconds(settingsService.getCallDelaySecondsMin() + 2);
        assertCreateAccountCallError(XmlErrorCallNotAllowedAtTheMomentException.class, telephoneNumber, accountRequest.getPassword(), now);


        // wait 12 hours and try again
        now = now.plus(Duration.ofHours(12)).plusSeconds(2);
        final AccountRequest accountRequest2 = assertCreateAccountRequestSuccess(telephoneNumber, now);
        now = now.plusSeconds(settingsService.getCallDelaySecondsMin() + 2);
        assertCreateAccountCallError(XmlErrorCallNotAllowedAtTheMomentException.class, telephoneNumber, accountRequest2.getPassword(), now);

        // wait 12 hours and try again with success
        now = now.plus(Duration.ofHours(12)).plusSeconds(2);
        final AccountRequest accountRequest3 = assertCreateAccountRequestSuccess(telephoneNumber, now);
        now = now.plusSeconds(settingsService.getCallDelaySecondsMin() + 2);
        assertCreateAccountCallSuccess(telephoneNumber, accountRequest3.getPassword(), now);
    }
}
