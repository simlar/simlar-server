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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.simlar.simlarserver.Application;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
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

    @Test
    public void testSendSms() {
        assumeTrue("This test needs a Twilio configuration with Twilio test credentials", twilioSettingsService.isConfigured());
        assertEquals("Twilio test credentials", "+15005550006", twilioSettingsService.getSmsSourceNumber());
        twilioSmsService.sendSms("+15005550006", "Test");
    }
}
