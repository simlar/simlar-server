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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.simlar.simlarserver.database.models.AccountCreationRequestCount;
import org.simlar.simlarserver.database.repositories.AccountCreationRequestCountRepository;
import org.simlar.simlarserver.services.settingsservice.SettingsService;
import org.simlar.simlarserver.services.smsservice.SmsService;
import org.simlar.simlarserver.services.subscriberservice.SubscriberService;
import org.simlar.simlarserver.utils.SimlarId;
import org.simlar.simlarserver.xml.XmlError;
import org.simlar.simlarserver.xml.XmlSuccessCreateAccountConfirm;
import org.simlar.simlarserver.xml.XmlSuccessCreateAccountRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Duration;
import java.time.Instant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@SuppressWarnings({"PMD.AvoidUsingHardCodedIP", "ClassWithTooManyMethods"})
@RunWith(SpringRunner.class)
public final class CreateAccountControllerTest extends BaseControllerTest {
    @Autowired
    private SmsService smsService;

    @Autowired
    private AccountCreationRequestCountRepository accountCreationRepository;

    @Autowired
    private SettingsService settingsService;

    @Autowired
    private SubscriberService subscriberService;

    @SuppressWarnings("MethodWithTooManyParameters")
    private <T> T postCreateAccount(final Class<T> responseClass, final boolean callSmsService, final boolean sendSmsResult, final String command, final String telephoneNumber, final String smsText) {
        reset(smsService);

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

    private String assertPostCreateAccountSuccess(final String expectedSimlarId, final String telephoneNumber, final String smsText) {
        final XmlSuccessCreateAccountRequest success = postCreateAccount(XmlSuccessCreateAccountRequest.class, true, true, CreateAccountController.COMMAND_REQUEST,telephoneNumber, smsText);
        assertNotNull(success);
        assertEquals(expectedSimlarId, success.getSimlarId());
        assertNotNull(success.getPassword());
        assertEquals("password '" + success.getPassword() + "' does not match expected size",14, success.getPassword().length());
        return success.getPassword();
    }

    @Test
    public void testRequestSuccess() {
        final String simlarId = "*15005550006*";
        final String password = assertPostCreateAccountSuccess(simlarId, "+15005550006", "android-en");

        final AccountCreationRequestCount count = accountCreationRepository.findBySimlarId(simlarId);
        assertNotNull(count);
        assertEquals(password, count.getPassword());
        assertNotNull(count.getRegistrationCode());
        assertEquals("password '" + count.getRegistrationCode() + "' does not match expected size",6, count.getRegistrationCode().length());
        assertEquals(1, count.getRequestTries());
        assertEquals(0, count.getConfirmTries());
        assertEquals("127.0.0.1", count.getIp());//NOPMD.AvoidUsingHardCodedIP
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
        assertPostCreateAccountError(22, false, CreateAccountController.COMMAND_REQUEST, "+49163123456", "android-de");
    }

    @Test
    public void testRequestTelephoneNumberLimit() {
        final String telephoneNumber = "+15005023024";

        final int max = settingsService.getAccountCreationMaxRequestsPerSimlarIdPerDay();
        for (int i = 0; i < max; i++) {
            if ((i & 1) == 0) {
                assertPostCreateAccountError(24, true, CreateAccountController.COMMAND_REQUEST, telephoneNumber, "android-de");
            } else {
                assertPostCreateAccountSuccess("*15005023024*", telephoneNumber, "android-en");
            }
        }

        final String simlarId = SimlarId.createWithTelephoneNumber(telephoneNumber).get();
        final AccountCreationRequestCount before = accountCreationRepository.findBySimlarId(simlarId);

        assertPostCreateAccountError(23, false, CreateAccountController.COMMAND_REQUEST, telephoneNumber, "android-de");

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
        assertPostCreateAccountSuccess("*15005023024*", telephoneNumber, "android-en");
        assertEquals(1, accountCreationRepository.findBySimlarId(simlarId).getRequestTries());
    }

    private <T> T postCall(final Class<T> responseClass, final boolean callSmsService, final boolean smsServiceResult, final String telephoneNumber, final String password) {
        reset(smsService);

        if (callSmsService) {
            when(smsService.call(eq(telephoneNumber), anyString())).thenReturn(smsServiceResult);
        }

        final T result = postRequest(responseClass, CreateAccountController.REQUEST_PATH_CALL, createParameters(new String[][] {
                { "telephoneNumber", telephoneNumber },
                { "password", password }
        }));

        if (callSmsService) {
            verify(smsService).call(eq(telephoneNumber), anyString());
        }

        verifyNoMoreInteractions(smsService);

        return result;
    }

    private void assertPostCallError(final int expectedErrorId, final boolean callSmsService, final boolean smsServiceResult, final String telephoneNumber, final String password) {
        final XmlError response = postCall(XmlError.class, callSmsService, smsServiceResult, telephoneNumber, password);
        assertNotNull(response);
        assertEquals(expectedErrorId, response.getId());
    }

    private void assertPostCallErrorBecauseOfFailedTrigger(final String telephoneNumber, final String password) {
        assertPostCallError(65, true, false, telephoneNumber, password);
    }

    private void assertPostCallError(final int expectedErrorId, final String telephoneNumber, final String password) {
        assertPostCallError(expectedErrorId, false, true, telephoneNumber, password);
    }

    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    @Test
    public void testCallWithInvalidTelephoneNumber() {
        assertPostCallError(22, "NO-NUMBER", "password1");
        assertPostCallError(22, "+49163123456", "password2");
    }

    @SuppressWarnings("JUnitTestMethodWithNoAssertions")
    @Test
    public void testCallWithInvalidCredentials() {
        final String telephoneNumber = "+15005023025";
        assertPostCallError(10, telephoneNumber, "password1");
        assertPostCallError(10, telephoneNumber, "password2");

        assertPostCreateAccountSuccess("*15005023025*", telephoneNumber, "ios-en");
        assertPostCallError(10, telephoneNumber, "password2");
    }

    @Test
    public void testCallTriggerFails() {
        final String telephoneNumber = "+15005023027";
        final String simlarId = "*15005023027*";
        final String password = assertPostCreateAccountSuccess(simlarId, telephoneNumber, "ios-en");

        final AccountCreationRequestCount requestCount = accountCreationRepository.findBySimlarId(simlarId);
        assertNotNull(requestCount.getTimestamp());
        requestCount.setTimestamp(requestCount.getTimestamp().minusSeconds(settingsService.getAccountCreationCallDelaySecondsMin() + 2));
        accountCreationRepository.save(requestCount);

        assertPostCallErrorBecauseOfFailedTrigger(telephoneNumber, password);
    }

    @Test
    public void testCallSuccess() {
        final String telephoneNumber = "+15005023026";
        final String simlarId = "*15005023026*";
        final String password = assertPostCreateAccountSuccess(simlarId, telephoneNumber, "ios-en");

        final AccountCreationRequestCount requestCount = accountCreationRepository.findBySimlarId(simlarId);
        assertNotNull(requestCount.getTimestamp());
        requestCount.setTimestamp(requestCount.getTimestamp().minusSeconds(settingsService.getAccountCreationCallDelaySecondsMin() + 2));
        accountCreationRepository.save(requestCount);

        final XmlSuccessCreateAccountRequest response = postCall(XmlSuccessCreateAccountRequest.class, true, true, telephoneNumber, password);
        assertNotNull(response);
    }

    @Test
    public void testCallDelay() {
        final String telephoneNumber = "+15005023028";
        final String simlarId = "*15005023028*";
        final String password = assertPostCreateAccountSuccess(simlarId, telephoneNumber, "ios-en");

        assertPostCallError(68, telephoneNumber, password);

        final AccountCreationRequestCount requestCount = accountCreationRepository.findBySimlarId(simlarId);
        final Instant originalTimestamp = requestCount.getTimestamp();
        assertNotNull(originalTimestamp);

        requestCount.setTimestamp(originalTimestamp.plusSeconds(settingsService.getAccountCreationCallDelaySecondsMax() + 2));
        accountCreationRepository.save(requestCount);
        assertPostCallError(68, telephoneNumber, password);

        requestCount.setTimestamp(originalTimestamp.minusSeconds(settingsService.getAccountCreationCallDelaySecondsMax() + 2));
        accountCreationRepository.save(requestCount);
        assertPostCallError(68, telephoneNumber, password);

        requestCount.setTimestamp(originalTimestamp.plusSeconds(settingsService.getAccountCreationCallDelaySecondsMin() + 2));
        accountCreationRepository.save(requestCount);
        assertPostCallError(68, telephoneNumber, password);

        requestCount.setTimestamp(originalTimestamp.minusSeconds(settingsService.getAccountCreationCallDelaySecondsMin() + 2));
        accountCreationRepository.save(requestCount);
        final XmlSuccessCreateAccountRequest response = postCall(XmlSuccessCreateAccountRequest.class, true, true, telephoneNumber, password);
        assertNotNull(response);
    }

    private <T> T postConfirmAccount(final Class<T> responseClass, final String command, final String simlarId, final String registrationCode) {
        return postRequest(responseClass, CreateAccountController.REQUEST_PATH, createParameters(new String[][] {
                { "command", command },
                { "simlarId", simlarId },
                { "registrationCode", registrationCode }
        }));
    }

    private void assertPostConfirmAccountError(final int expectedErrorId, final String command, final String simlarId, final String registrationCode) {
        final XmlError response = postConfirmAccount(XmlError.class, command, simlarId, registrationCode);
        assertNotNull(response);
        assertEquals(expectedErrorId, response.getId());
    }

    @Test
    public void testConfirmWithWrongCommand() {
        assertPostConfirmAccountError(1, "xyz", "*15005550006*", "123456");
        assertPostConfirmAccountError(1, "request", "*15005550006*", "234561");
        assertPostConfirmAccountError(1, null, "*15005550006*", "345612");
    }

    @Test
    public void testConfirmWithNoSimlarId() {
        assertPostConfirmAccountError(27, "confirm", null, "123456");
        assertPostConfirmAccountError(27, "confirm", "*as005550006*", "234561");
        assertPostConfirmAccountError(27, "confirm", "*1500555000..", "345612");
    }

    @Test
    public void testConfirmWithNoRegistrationCode() {
        assertPostConfirmAccountError(28, "confirm", "*15005550006*", null);
        assertPostConfirmAccountError(28, "confirm", "*15005550006*", "23456");
        assertPostConfirmAccountError(28, "confirm", "*15005550006*", "2345618");
        assertPostConfirmAccountError(28, "confirm", "*15005550006*", "345x12");
    }

    @Test
    public void testConfirmWithNotRequestedSimlarId() {
        final String simlarId = "*42002300001*";

        assertNull(accountCreationRepository.findBySimlarId(simlarId));
        assertPostConfirmAccountError(27, CreateAccountController.COMMAND_CONFIRM, simlarId, "234561");
    }

    @Test
    public void testConfirmWithWrongRegistrationCode() {
        final String simlarId = "*42002300002*";

        final AccountCreationRequestCount before = new AccountCreationRequestCount(simlarId, "V3RY-5AF3", "627130", "127.0.0.1");
        accountCreationRepository.save(before);
        assertPostConfirmAccountError(26, CreateAccountController.COMMAND_CONFIRM, simlarId, "234561");
        final AccountCreationRequestCount after = accountCreationRepository.findBySimlarId(simlarId);
        assertEquals(before.getConfirmTries() + 1, after.getConfirmTries());
        assertEquals(before.getRequestTries(), after.getRequestTries());
        assertEquals(before.getRegistrationCode(), after.getRegistrationCode());
        assertEquals(before.getIp(), after.getIp());
        assertEquals(before.getPassword(), after.getPassword());
    }

    @Test
    public void testConfirmWithTooManyRetries() {
        final String simlarId = "*42002300003*";
        final String registrationCode = "432516";

        final AccountCreationRequestCount before = new AccountCreationRequestCount(simlarId, "V3RY-5AF3", registrationCode, "127.0.0.1");
        before.setConfirmTries(settingsService.getAccountCreationMaxConfirms());
        accountCreationRepository.save(before);
        assertPostConfirmAccountError(25, CreateAccountController.COMMAND_CONFIRM, simlarId, registrationCode);
        final AccountCreationRequestCount after = accountCreationRepository.findBySimlarId(simlarId);
        assertEquals(before.getConfirmTries() + 1, after.getConfirmTries());
        assertEquals(before.getRequestTries(), after.getRequestTries());
        assertEquals(before.getRegistrationCode(), after.getRegistrationCode());
        assertEquals(before.getIp(), after.getIp());
        assertEquals(before.getPassword(), after.getPassword());
    }

    @Test
    public void testConfirmWithoutRequest() {
        final String simlarId = "*42002300005*";

        assertNull(accountCreationRepository.findBySimlarId(simlarId));
        assertPostConfirmAccountError(27, CreateAccountController.COMMAND_CONFIRM, simlarId, "654321");
        assertNull(accountCreationRepository.findBySimlarId(simlarId));
    }

    @Test
    public void testConfirmSuccess() {
        final String simlarId = "*42002300004*";
        final String registrationCode = "432517";

        final AccountCreationRequestCount before = new AccountCreationRequestCount(simlarId, "V3RY-5AF3", registrationCode, "127.0.0.1");
        accountCreationRepository.save(before);

        final XmlSuccessCreateAccountConfirm response = postConfirmAccount(XmlSuccessCreateAccountConfirm.class, CreateAccountController.COMMAND_CONFIRM, simlarId, registrationCode);
        assertEquals(simlarId, response.getSimlarId());
        assertEquals(registrationCode, response.getRegistrationCode());

        final AccountCreationRequestCount after = accountCreationRepository.findBySimlarId(simlarId);
        assertEquals(before.getConfirmTries() + 1, after.getConfirmTries());
        assertEquals(before.getRequestTries(), after.getRequestTries());
        assertEquals(before.getRegistrationCode(), after.getRegistrationCode());
        assertEquals(before.getIp(), after.getIp());
        assertEquals(before.getPassword(), after.getPassword());

        assertTrue(subscriberService.checkCredentials(simlarId, "c42214dc9d5eb6f7093b7589937e41cf"));
    }

    @Test
    public void testCompleteAccountCreation() {
        final SimlarId simlarId = SimlarId.create("*15005042023*");
        assertNotNull(simlarId);

        // request
        final String password = assertPostCreateAccountSuccess(simlarId.get(), "+15005042023", "android-en");
        final AccountCreationRequestCount dbEntry = accountCreationRepository.findBySimlarId(simlarId.get());
        assertNotNull(dbEntry);
        assertNotNull(dbEntry.getRegistrationCode());

        // confirm
        final XmlSuccessCreateAccountConfirm response = postConfirmAccount(XmlSuccessCreateAccountConfirm.class, CreateAccountController.COMMAND_CONFIRM, simlarId.get(), dbEntry.getRegistrationCode());
        assertEquals(simlarId.get(), response.getSimlarId());
        assertEquals(dbEntry.getRegistrationCode(), response.getRegistrationCode());

        // check
        assertTrue(subscriberService.checkCredentials(simlarId.get(), subscriberService.createHashHa1(simlarId, password)));
    }
}
