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

package org.simlar.simlarserver.database.repositories;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.simlar.simlarserver.SimlarServer;
import org.simlar.simlarserver.data.TwilioRequestType;
import org.simlar.simlarserver.database.models.SmsProviderLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SimlarServer.class)
public final class SmsProviderLogTest {
    @Autowired
    private SmsProviderLogRepository smsProviderLogRepository;

    @Test
    public void testCallBackTimestampNull() {
        final String telephoneNumber = "+123456789";
        final String message         = "Test invalid number";
        final String sessionId       = "error127";
        final String status          = "42";
        final String error           = "1234567890123456789012345678901234567890123456789012345678901234567890123456789";

        smsProviderLogRepository.save(new SmsProviderLog(TwilioRequestType.SMS, telephoneNumber, sessionId, status, error, message));
        final SmsProviderLog logEntry = smsProviderLogRepository.findByTelephoneNumber(telephoneNumber);
        assertNotNull(logEntry);
        assertNotNull(logEntry.getTimestamp());
        assertEquals(telephoneNumber, logEntry.getTelephoneNumber());
        assertEquals(message, logEntry.getMessage());
        assertEquals(sessionId, logEntry.getSessionId());
        assertEquals(status, logEntry.getStatus());
        assertEquals(StringUtils.left(error, 64), logEntry.getError());
        assertNull(logEntry.getCallbackTimestamp());
    }
}
