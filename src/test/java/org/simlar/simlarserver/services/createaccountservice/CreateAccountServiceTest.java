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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.simlar.simlarserver.SimlarServer;
import org.simlar.simlarserver.database.models.AccountCreationRequestCount;
import org.simlar.simlarserver.database.repositories.AccountCreationRequestCountRepository;
import org.simlar.simlarserver.services.smsservice.SmsService;
import org.simlar.simlarserver.utils.SimlarId;
import org.simlar.simlarserver.utils.SimlarIdHelper;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorCallNotAllowedAtTheMomentException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorFailedToSendSmsException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorInvalidTelephoneNumberException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorNoIpException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorTooManyConfirmTriesException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorTooManyRequestTriesException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorWrongRegistrationCodeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@SuppressWarnings({"PMD.AvoidUsingHardCodedIP", "ClassWithTooManyMethods", "ObjectAllocationInLoop"})
@RunWith(SpringRunner.class)
@SpringBootTest(classes = SimlarServer.class, properties = {
        "create.account.alertSmsNumbers = 1234, 5678",
        "create.account.maxRequestsPerIpPerHour = 12",
        "create.account.maxRequestsTotalPerHour = 15",
        "create.account.maxRequestsTotalPerDay = 30",
        "create.account.maxRequestsPerSimlarIdPerDay = 7",
        "create.account.regionalSettings[0].regionCode = 160",
        "create.account.regionalSettings[0].maxRequestsPerHour=4",
        "create.account.registrationCodeExpirationMinutes=30",
        "create.account.testAccounts[0].simlarId = *1*",
        "create.account.testAccounts[0].registrationCode = 123456",
        "create.account.testAccounts[1].simlarId = *2*",
        "create.account.testAccounts[1].registrationCode = 654321" })
public final class CreateAccountServiceTest {
    @Autowired
    private CreateAccountService createAccountService;

    @Autowired
    private SmsService smsService;

    @Autowired
    private CreateAccountSettings createAccountSettings;

    @Autowired
    private AccountCreationRequestCountRepository accountCreationRepository;


    @SuppressFBWarnings("UTAO_JUNIT_ASSERTION_ODDITIES_NO_ASSERT")
    @Test
    public void testCreateAccountRequestWithInvalidNumber() {
        final String message = assertThrows(XmlErrorInvalidTelephoneNumberException.class, () ->
                createAccountService.createAccountRequest("NO-NUMBER", "", "192.168.1.1")
        ).getMessage();

        assertNotNull(message);
        assertTrue(message.contains("NO-NUMBER"));
        assertFalse(message.contains("libphonenumber"));
    }

    @SuppressFBWarnings("UTAO_JUNIT_ASSERTION_ODDITIES_NO_ASSERT")
    @Test
    public void testCreateAccountRequestWithInvalidNumberLibphonenumber() {
        final String message = assertThrows(XmlErrorInvalidTelephoneNumberException.class, () ->
                createAccountService.createAccountRequest("+49163123456", "", "192.168.1.1")
        ).getMessage();

        assertNotNull(message);
        assertTrue(message.contains("+49163123456"));
        assertTrue(message.contains("libphonenumber"));
    }

    @SuppressFBWarnings("UTAO_JUNIT_ASSERTION_ODDITIES_NO_ASSERT")
    @Test
    public void testCreateAccountRequestWithFailedSms() {
        assertThrows(XmlErrorFailedToSendSmsException.class, () ->
                createAccountService.createAccountRequest("+15005550006", "", "192.168.1.1")
        );
    }

    @SuppressFBWarnings("UTAO_JUNIT_ASSERTION_ODDITIES_NO_ASSERT")
    @Test
    public void testCreateAccountRequestWithIpEmpty() {
        assertThrows(XmlErrorNoIpException.class, () ->
                createAccountService.createAccountRequest("+15005550006", "", "")
        );
    }

    @SuppressFBWarnings("UTAO_JUNIT_ASSERTION_ODDITIES_NO_ASSERT")
    @Test
    public void testCreateAccountRequestWithIpNull() {
        assertThrows(XmlErrorNoIpException.class, () ->
                createAccountService.createAccountRequest("+15005550006", "", null)
        );
    }

    private AccountRequest assertCreateAccountRequestSuccess(final String telephoneNumber, final String ip, final Instant timestamp) {
        when(smsService.sendSms(eq(telephoneNumber), anyString())).thenReturn(Boolean.TRUE);
        final AccountRequest accountRequest = createAccountService.createAccountRequest(telephoneNumber, "", ip, timestamp);
        verify(smsService).sendSms(eq(telephoneNumber), anyString());
        assertNotNull(accountRequest);
        assertNotNull(accountRequest.simlarId());
        assertNotNull(accountRequest.password());

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
        for (final String alertNumber : createAccountSettings.getAlertSmsNumbers()) {
            when(smsService.sendSms(eq(alertNumber), anyString())).thenReturn(Boolean.TRUE);
        }
        when(smsService.sendSms(eq(telephoneNumber), anyString())).thenReturn(Boolean.TRUE);

        createAccountService.createAccountRequest(telephoneNumber, "", ip);

        for (final String alertNumber : createAccountSettings.getAlertSmsNumbers()) {
            verify(smsService).sendSms(eq(alertNumber), anyString());
        }
        verify(smsService).sendSms(eq(telephoneNumber), anyString());
    }

    @DirtiesContext
    @SuppressWarnings({"JUnitTestMethodWithNoAssertions", "TestMethodWithoutAssertion"})
    @Test
    public void testCreateAccountRequestSuccess() {
        assertCreateAccountRequestSuccess("+15005510001");
    }

    @DirtiesContext
    @Test
    public void testCreateAccountRequestTelephoneNumberLimit() {
        final String telephoneNumber = "+15005023024";

        final int max = createAccountSettings.getMaxRequestsPerSimlarIdPerDay();
        for (int i = 0; i < max; i++) {
            reset(smsService);
            if (i % 2 == 0) {
                //noinspection ObjectAllocationInLoop
                assertThrows(XmlErrorFailedToSendSmsException.class, () -> createAccountService.createAccountRequest(telephoneNumber, "", "192.168.1.1"));
            } else {
                assertCreateAccountRequestSuccess(telephoneNumber);
            }
        }

        final String simlarId = SimlarIdHelper.createSimlarId(telephoneNumber);
        final AccountCreationRequestCount before = accountCreationRepository.findBySimlarId(simlarId);

        assertThrows(XmlErrorTooManyRequestTriesException.class, () -> createAccountService.createAccountRequest(telephoneNumber, "", "192.168.1.1"));

        final AccountCreationRequestCount after = accountCreationRepository.findBySimlarId(simlarId);
        assertEquals(before.getConfirmTries(), after.getConfirmTries());
        assertEquals(before.getRequestTries() + 1, after.getRequestTries());
        assertEquals(createAccountSettings.getMaxRequestsPerSimlarIdPerDay() + 1, after.getRequestTries());
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

    @DirtiesContext
    @Test
    public void testCreateAccountRequestIpLimitWithinOneHour() {
        final String ip = "192.168.23.42";

        final int max = createAccountSettings.getMaxRequestsPerIpPerHour();
        for (int i = 0; i < max; i++) {
            final String telephoneNumber = "+1500502304" + i % 10;
            reset(smsService);
            if (i % 2 == 0) {
                //noinspection ObjectAllocationInLoop
                assertThrows(XmlErrorFailedToSendSmsException.class, () -> createAccountService.createAccountRequest(telephoneNumber, "", ip));
            } else {
                assertCreateAccountRequestSuccess(telephoneNumber, ip);
            }
        }

        final String telephoneNumber = "+15005023049";
        assertThrows(XmlErrorTooManyRequestTriesException.class, () -> createAccountService.createAccountRequest(telephoneNumber, "", ip));


        /// check limit reset after an hour
        reduceAccountCreationTimestamp("*15005023040*", Duration.ofMinutes(61));
        reset(smsService);
        assertCreateAccountRequestSuccess(telephoneNumber, ip);
    }

    @DirtiesContext
    @Test
    public void testCreateAccountRequestTotalLimitWithinOneHour() {
        final int max = createAccountSettings.getMaxRequestsTotalPerHour();
        for (int i = 0; i < max; i++) {
            final String telephoneNumber = "+1500502214" + i % 10;
            final String ip = "192.168.42." + (i % 255 + 1);
            reset(smsService);
            if (i == max / 2) {
                assertCreateAccountRequestSuccessWithSmsAlert(telephoneNumber, ip);
            } else {
                if (i % 2 == 0) {
                    //noinspection ObjectAllocationInLoop
                    assertThrows(XmlErrorFailedToSendSmsException.class, () -> createAccountService.createAccountRequest(telephoneNumber, "", ip));
                } else {
                    assertCreateAccountRequestSuccess(telephoneNumber, ip);
                }
            }
        }

        final String telephoneNumber = "+15005022149";
        assertThrows(XmlErrorTooManyRequestTriesException.class, () -> createAccountService.createAccountRequest(telephoneNumber, "", "192.168.1.23"));


        /// check limit reset after an hour
        reduceAccountCreationTimestamp("*15005022141*", Duration.ofMinutes(61));
        reset(smsService);
        assertCreateAccountRequestSuccess(telephoneNumber, "192.168.1.23");
    }

    @DirtiesContext
    @Test
    public void testCreateAccountRequestTotalLimitWithinOneDay() {
        final int max = createAccountSettings.getMaxRequestsTotalPerDay();
        for (int i = 0; i < max; i++) {
            reset(smsService);
            final String number = "1500501201" + i % 10;
            final String telephoneNumber = '+' + number;
            final String ip = "192.168.42." + (i % 255 + 1);

            if (i == max / 2) {
                assertCreateAccountRequestSuccessWithSmsAlert(telephoneNumber, ip);
            } else {
                if (i % 2 == 0) {
                    //noinspection ObjectAllocationInLoop
                    assertThrows(XmlErrorFailedToSendSmsException.class, () -> createAccountService.createAccountRequest(telephoneNumber, "", ip));
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
        assertThrows(XmlErrorTooManyRequestTriesException.class, () -> createAccountService.createAccountRequest(telephoneNumber, "", "192.168.1.23"));


        /// check limit reset after a day
        reduceAccountCreationTimestamp("*15005012011*", Duration.ofHours(25));
        reset(smsService);
        assertCreateAccountRequestSuccess(telephoneNumber, "192.168.1.23");
    }

    @DirtiesContext
    @Test
    public void testCreateAccountRequestTotalLimitWithinOneHourRegionalLimit() {
        final int max = createAccountSettings.getRegionalSettings().get(0).maxRequestsPerHour();
        for (int i = 0; i < max; i++) {
            final String telephoneNumber = "+1600502214" + i % 10;
            final String ip = "192.168.23." + (i % 255 + 1);
            reset(smsService);

            if (i % 2 == 0) {
                //noinspection ObjectAllocationInLoop
                assertThrows(XmlErrorFailedToSendSmsException.class, () -> createAccountService.createAccountRequest(telephoneNumber, "", ip));
            } else {
                assertCreateAccountRequestSuccess(telephoneNumber, ip);
            }
        }

        final String telephoneNumber = "+16005022140";
        assertThrows(XmlErrorTooManyRequestTriesException.class, () -> createAccountService.createAccountRequest(telephoneNumber, "", "192.168.1.23"));

        /// check other numbers work
        assertCreateAccountRequestSuccess("+15005022149", "192.168.1.23");

        /// check limit reset after an hour
        reduceAccountCreationTimestamp("*16005022141*", Duration.ofMinutes(61));
        reset(smsService);
        assertCreateAccountRequestSuccess(telephoneNumber, "192.168.1.23");
    }

    private String createAccountRequestReceiveSms(final String telephoneNumber, final Instant timestamp) {
        when(smsService.sendSms(eq(telephoneNumber), anyString())).thenReturn(Boolean.TRUE);
        assertNotNull(createAccountService.createAccountRequest(telephoneNumber, "", "192.168.23.42", timestamp));
        final ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(smsService).sendSms(eq(telephoneNumber), argumentCaptor.capture());
        reset(smsService);
        return argumentCaptor.getValue();
    }

    @DirtiesContext
    @Test
    public void testRegistrationCodeStaysTheSameForAWhile() {
        final String telephoneNumber1 = "+15005012151";
        final String telephoneNumber2 = "+15005012152";
        final Instant now = Instant.now();


        final String sms1 = createAccountRequestReceiveSms(telephoneNumber1, now);
        final String sms2 = createAccountRequestReceiveSms(telephoneNumber2, now);
        assertNotEquals(sms1, sms2);

        final int expirationMinutes = createAccountSettings.getRegistrationCodeExpirationMinutes();
        @SuppressWarnings("MultiplyOrDivideByPowerOfTwo")
        final int stepSize = expirationMinutes / createAccountSettings.getMaxRequestsPerIpPerHour() * 2 + 2;
        for (int i = 1; i < expirationMinutes / stepSize; ++i) {
            final int minutes = i * stepSize;
            assertEquals("after " + minutes + 'm', sms1, createAccountRequestReceiveSms(telephoneNumber1, now.plusSeconds(minutes * 60L)));
            assertEquals("after " + minutes + 'm', sms2, createAccountRequestReceiveSms(telephoneNumber2, now.plusSeconds(minutes * 60L)));
        }

        assertNotEquals(sms1, createAccountRequestReceiveSms(telephoneNumber1, now.plusSeconds(expirationMinutes * 60L + 1)));
        assertNotEquals(sms2, createAccountRequestReceiveSms(telephoneNumber2, now.plusSeconds(expirationMinutes * 60L + 1)));
    }

    @SuppressWarnings("SameParameterValue")
    private void assertCreateAccountCallSuccess(final String telephoneNumber, final String password, final Instant timestamp) {
        when(smsService.call(eq(telephoneNumber), anyString())).thenReturn(Boolean.TRUE);

        createAccountService.call(telephoneNumber, password, timestamp);

        verify(smsService).call(eq(telephoneNumber), anyString());
    }

    @SuppressWarnings("SameParameterValue")
    private void assertCreateAccountCallError(final Class<? extends XmlErrorException> error, final String telephoneNumber, final String password, final Instant timestamp) {
        assertThrows(error, () -> createAccountService.call(telephoneNumber, password, timestamp));
    }

    @DirtiesContext
    @Test
    public void testTooManyCalls() {
        final String telephoneNumber = "+15005012150";
        Instant now = Instant.now();

        final int max = createAccountSettings.getMaxCalls();
        for (int i = 0; i < max; ++i) {
            final AccountRequest accountRequest = assertCreateAccountRequestSuccess(telephoneNumber, now);
            now = now.plusSeconds(createAccountSettings.getCallDelaySecondsMin() + 2);
            assertCreateAccountCallSuccess(telephoneNumber, accountRequest.password(), now);
        }

        final AccountRequest accountRequest = assertCreateAccountRequestSuccess(telephoneNumber, now);
        now = now.plusSeconds(createAccountSettings.getCallDelaySecondsMin() + 2);
        assertCreateAccountCallError(XmlErrorCallNotAllowedAtTheMomentException.class, telephoneNumber, accountRequest.password(), now);


        // wait 12 hours and try again
        now = now.plus(Duration.ofHours(12)).plusSeconds(2);
        final AccountRequest accountRequest2 = assertCreateAccountRequestSuccess(telephoneNumber, now);
        now = now.plusSeconds(createAccountSettings.getCallDelaySecondsMin() + 2);
        assertCreateAccountCallError(XmlErrorCallNotAllowedAtTheMomentException.class, telephoneNumber, accountRequest2.password(), now);

        // wait 12 hours and try again with success
        now = now.plus(Duration.ofHours(12)).plusSeconds(2);
        final AccountRequest accountRequest3 = assertCreateAccountRequestSuccess(telephoneNumber, now);
        now = now.plusSeconds(createAccountSettings.getCallDelaySecondsMin() + 2);
        assertCreateAccountCallSuccess(telephoneNumber, accountRequest3.password(), now);
    }

    @DirtiesContext
    @Test
    public void testTestAccounts() {
        final List<TestAccount> testAccounts = createAccountSettings.getTestAccounts();
        assertEquals(2, testAccounts.size());

        for (final TestAccount testAccount: testAccounts) {
            final String telephoneNumber = testAccount.simlarId();
            final SimlarId simlarId = SimlarId.create(telephoneNumber);

            createAccountService.createAccountRequest(simlarId, telephoneNumber, "", "192.168.23.42", Instant.now());
            assertThrows(XmlErrorWrongRegistrationCodeException.class, () ->
                    createAccountService.confirmAccount(telephoneNumber, "112233")
            );
            createAccountService.confirmAccount(telephoneNumber, testAccount.registrationCode());
        }

        verifyNoMoreInteractions(smsService);
    }

    @SuppressWarnings("MethodWithMultipleLoops")
    @DirtiesContext
    @Test
    public void testTestAccountsLimits() {
        final List<TestAccount> testAccounts = createAccountSettings.getTestAccounts();
        assertEquals(2, testAccounts.size());

        final String registrationCode = testAccounts.get(0).registrationCode();
        final String telephoneNumber = testAccounts.get(0).simlarId();
        final SimlarId simlarId = SimlarId.create(telephoneNumber);
        final Instant now = Instant.now();

        final int maxRequests = createAccountSettings.getMaxRequestsPerSimlarIdPerDay();
        final int maxConfirms = createAccountSettings.getMaxConfirms();
        for (int i = 0; i < maxRequests; i++) {
            createAccountService.createAccountRequest(simlarId, telephoneNumber, "", "192.168.23.42", now.plus(createAccountSettings.getRegistrationCodeExpirationMinutes() * i * 2L, ChronoUnit.MINUTES));

            for (int j = 0; j < maxConfirms; j++) {
                assertThrows(XmlErrorWrongRegistrationCodeException.class, () ->
                        createAccountService.confirmAccount(telephoneNumber, "112233")
                );
            }
            assertThrows(XmlErrorTooManyConfirmTriesException.class, () ->
                    createAccountService.confirmAccount(telephoneNumber, registrationCode)
            );
        }

        assertThrows(XmlErrorTooManyRequestTriesException.class, () ->
            createAccountService.createAccountRequest(simlarId, telephoneNumber, "", "192.168.23.42", now.plus(1, ChronoUnit.DAYS))
        );

        createAccountService.createAccountRequest(simlarId, telephoneNumber, "", "192.168.23.42", now.plus(3, ChronoUnit.DAYS));

        verifyNoMoreInteractions(smsService);
    }
}
