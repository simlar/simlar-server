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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.simlar.simlarserver.Application;
import org.simlar.simlarserver.database.models.SmsSentLog;
import org.simlar.simlarserver.database.repositories.SmsSentLogRepository;
import org.simlar.simlarserver.services.settingsservice.SettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

@TestPropertySource(properties = "domain = sip.simlar.org") // domain is an essential part of the callback url
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public final class TwilioSmsServiceTest {
    @SuppressWarnings("CanBeFinal")
    @Autowired
    private TwilioSmsService twilioSmsService;

    @SuppressWarnings("CanBeFinal")
    @Autowired
    private TwilioSettingsService twilioSettingsService;

    @SuppressWarnings("CanBeFinal")
    @Autowired
    private SmsSentLogRepository smsSentLogRepository;

    @SuppressWarnings("CanBeFinal")
    @Autowired
    private SettingsService settingsService;

    @Before
    public void verifyConfiguration() {
        assumeTrue("This test needs a Twilio configuration with Twilio test credentials", twilioSettingsService.isConfigured());
        assertEquals("Twilio test credentials", "+15005550006", twilioSettingsService.getSmsSourceNumber());
    }

    @Test
    public void testSendSmsSuccess() {
        final String telephoneNumber = "+15005550006";
        final String message         = "Test success";
        assertTrue(twilioSmsService.sendSms(telephoneNumber, message));
        final SmsSentLog logEntry = smsSentLogRepository.findByTelephoneNumber(telephoneNumber);
        assertNotNull(logEntry);
        assertNotNull(logEntry.getTimestamp());
        assertEquals(telephoneNumber, logEntry.getTelephoneNumber());
        assertEquals(message, logEntry.getMessage());
        assertNotNull(logEntry.getDlrNumber());
        assertEquals("queued", logEntry.getTwilioStatus());
        assertNull(logEntry.getTwilioError());
        assertEquals(-2, logEntry.getSmsTradeStatus());
    }

    @Test
    public void testSendSmsNoConfig() {
        final String telephoneNumber = "+0000000001";
        final String message         = "Test not configured";

        final TwilioSettingsService twilioSettings = new TwilioSettingsService("", "", "", "", "", "");
        final TwilioSmsService service = new TwilioSmsService(settingsService, twilioSettings, smsSentLogRepository);
        assertFalse(service.sendSms(telephoneNumber, message));
    }

    @Test
    public void testSendSmsNoNetwork() {
        final String telephoneNumber = "+0000000002";
        final String message         = "Test no network";

        final TwilioSettingsService twilioSettings = new TwilioSettingsService("https://no.example.com", "+1", "007", "secret", "user", "password");
        final TwilioSmsService service = new TwilioSmsService(settingsService, twilioSettings, smsSentLogRepository);

        assertFalse(service.sendSms(telephoneNumber, message));
        final SmsSentLog logEntry = smsSentLogRepository.findByTelephoneNumber(telephoneNumber);
        assertNotNull(logEntry);
        assertNotNull(logEntry.getTimestamp());
        assertEquals(telephoneNumber, logEntry.getTelephoneNumber());
        assertEquals(message, logEntry.getMessage());
        assertNull(logEntry.getDlrNumber());
        assertNotNull(logEntry.getTwilioStatus());
        assertNotNull(logEntry.getTwilioError());
        assertEquals(-2, logEntry.getSmsTradeStatus());
    }

    @Test
    public void testSendSmsInvalidNumber() {
        final String telephoneNumber = "+15005550001";
        final String message         = "Test invalid number";
        assertFalse(twilioSmsService.sendSms(telephoneNumber, message));
        final SmsSentLog logEntry = smsSentLogRepository.findByTelephoneNumber(telephoneNumber);
        assertNotNull(logEntry);
        assertNotNull(logEntry.getTimestamp());
        assertEquals(telephoneNumber, logEntry.getTelephoneNumber());
        assertEquals(message, logEntry.getMessage());
        assertNull(logEntry.getDlrNumber());
        assertNotNull(logEntry.getTwilioStatus());
        assertNotNull(logEntry.getTwilioError());
        assertEquals(-2, logEntry.getSmsTradeStatus());
    }
}
