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

package org.simlar.simlarserver.services.twilio;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.simlar.simlarserver.SimlarServer;
import org.simlar.simlarserver.data.TwilioRequestType;
import org.simlar.simlarserver.database.models.SmsProviderLog;
import org.simlar.simlarserver.database.repositories.SmsProviderLogRepository;
import org.simlar.simlarserver.services.SharedSettings;
import org.simlar.simlarserver.services.smsservice.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.simlar.simlarserver.helper.Asserts.assertAlmostEquals;
import static org.simlar.simlarserver.helper.Asserts.assertAlmostEqualsContainsError;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SimlarServer.class)
public final class TwilioSmsServiceTest {
    private SharedSettings sharedSettings;

    private TwilioSmsService twilioSmsService;

    @Autowired
    private TwilioSettings twilioSettings;

    @Autowired
    private SmsProviderLogRepository smsProviderLogRepository;

    @Before
    public void setup() {
        assumeTrue("This test needs a Twilio configuration with Twilio test credentials", twilioSettings.isConfigured());
        assertEquals("Twilio test credentials", "+15005550006", twilioSettings.smsSourceNumber());

        sharedSettings = new SharedSettings("sip.simlar.org", (short)6161);
        twilioSmsService = new TwilioSmsService(sharedSettings, twilioSettings, smsProviderLogRepository);
    }

    @Test
    public void testSendSmsSuccess() {
        final String telephoneNumber = "+15005550006";
        final String message         = "Test success";
        assertTrue(twilioSmsService.sendSms(telephoneNumber, message));
        final SmsProviderLog logEntry = smsProviderLogRepository.findByTelephoneNumber(telephoneNumber);
        assertNotNull(logEntry);
        assertAlmostEquals("sms success", new SmsProviderLog(TwilioRequestType.SMS, telephoneNumber, "xxx", "queued", message), logEntry);
    }

    @Test
    public void testSendSmsNoConfig() {
        final String telephoneNumber = "+0000000001";
        final String message         = "Test not configured";

        final TwilioSettings settings = new TwilioSettings(null, null, null, null, null);
        final SmsService service = new TwilioSmsService(sharedSettings, settings, smsProviderLogRepository);

        assertFalse(service.sendSms(telephoneNumber, message));
        assertAlmostEquals(message,
                new SmsProviderLog(TwilioRequestType.SMS, telephoneNumber, null, "SimlarServerException", "twilio not configured", message),
                smsProviderLogRepository.findByTelephoneNumber(telephoneNumber));
    }

    @SuppressFBWarnings("HARD_CODE_PASSWORD")
    @SuppressWarnings("TooBroadScope")
    @Test
    public void testSendSmsNoNetwork() {
        final String telephoneNumber = "+0000000002";
        final String message         = "Test no network";

        final TwilioSettings mockedSettings = mock(TwilioSettings.class);
        when(mockedSettings.isConfigured()).thenReturn(Boolean.TRUE);
        when(mockedSettings.getUrl()).thenReturn("https://no.example.com/index");
        when(mockedSettings.smsSourceNumber()).thenReturn("+1");
        when(mockedSettings.sid()).thenReturn("007");
        when(mockedSettings.authToken()).thenReturn("secret");
        when(mockedSettings.callbackUser()).thenReturn("user");
        when(mockedSettings.callbackPassword()).thenReturn("password");

        final SmsService service = new TwilioSmsService(sharedSettings, mockedSettings, smsProviderLogRepository);
        assertFalse(service.sendSms(telephoneNumber, message));
        assertAlmostEqualsContainsError(message,
                new SmsProviderLog(TwilioRequestType.SMS, telephoneNumber, null, "SimlarServerException", "UnknownHostException: no.example.com:", message),
                smsProviderLogRepository.findByTelephoneNumber(telephoneNumber));
    }

    @Test
    public void testSendSmsInvalidNumber() {
        final String telephoneNumber = "+15005550001";
        final String message         = "Test invalid number";
        assertFalse(twilioSmsService.sendSms(telephoneNumber, message));
        assertAlmostEquals(message,
                new SmsProviderLog(TwilioRequestType.SMS, telephoneNumber, null, "400", "null - The 'To' number " + telephoneNumber + " is not a valid phone number.", message),
                smsProviderLogRepository.findByTelephoneNumber(telephoneNumber));
    }

    @Test
    public void testSendSmsNotReachableNumber() {
        final String telephoneNumber = "+15005550002";
        final String message         = "Number not reachable";
        assertFalse(twilioSmsService.sendSms(telephoneNumber, message));
        assertAlmostEquals(message,
                new SmsProviderLog(TwilioRequestType.SMS, telephoneNumber, null, "400", "null - The 'To' phone number: " + telephoneNumber + ", is not currently reachable using the 'From' phone number: +15005550006 via SMS.", message),
                smsProviderLogRepository.findByTelephoneNumber(telephoneNumber));
    }

    @Test
    public void testHandleResponseNoStatus() {
        final String telephoneNumber = "4711";
        final String message         = "Number no status";
        final String response        = "{\"sid\": 21211}";
        assertFalse(twilioSmsService.handleResponse(TwilioRequestType.SMS, telephoneNumber, message, response));
        assertAlmostEquals(message,
                new SmsProviderLog(TwilioRequestType.SMS, telephoneNumber, null, "SimlarServerException", "not parsable response: " + response, message),
                smsProviderLogRepository.findByTelephoneNumber(telephoneNumber));
    }

    @Test
    public void testHandleResponseNoJson() {
        final String telephoneNumber = "23";
        final String message         = "Number no json";
        final String response        = "\"sid\": 21211";
        assertFalse(twilioSmsService.handleResponse(TwilioRequestType.SMS, telephoneNumber, message, response));
        assertAlmostEquals(message,
                new SmsProviderLog(TwilioRequestType.SMS, telephoneNumber, null, "SimlarServerException", "not parsable response: " + response, message),
                smsProviderLogRepository.findByTelephoneNumber(telephoneNumber));
    }

    @Test
    public void testCallSuccess() {
        final String telephoneNumber = "+15005550006";
        final String message         = "Welcome to simlar! Your registration code is 123456";

        assertTrue(twilioSmsService.call(telephoneNumber, message));
    }
}
