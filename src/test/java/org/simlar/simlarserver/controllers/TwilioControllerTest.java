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
import org.simlar.simlarserver.database.models.SmsSentLog;
import org.simlar.simlarserver.database.repositories.SmsSentLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Instant;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.simlar.simlarserver.helper.Asserts.assertAlmostEquals;

@SuppressWarnings("ALL")
@RunWith(SpringRunner.class)
public final class TwilioControllerTest extends BaseControllerTest {
    @SuppressWarnings("CanBeFinal")
    @Autowired
    private SmsSentLogRepository smsSentLogRepository;

    /*private <T> T postDeliveryReport(final Class<T> responseClass,
                                     final String messageSid,
                                     final String to,
                                     final String messageStatus,
                                     final String errorCode) {
        return postRequest(responseClass, TwilioController.REQUEST_PATH, createParameters(new String[][] {
                { "MessageSid", messageSid },
                { "To", to },
                { "MessageStatus", messageStatus },
                { "ErrorCode", errorCode }
        }));
    }*/

    private void postDeliveryReportSuccess(final String smsSid,
                                           final String smsStatus,
                                           final String messageStatus,
                                           final String to,
                                           final String messageSid,
                                           final String accountSid,
                                           final String from,
                                           final String apiVersion) {
        assertNull(postRequest(TwilioController.REQUEST_PATH, createParameters(new String[][] {
                { "SmsSid", smsSid },
                { "SmsStatus", smsStatus },
                { "MessageStatus", messageStatus },
                { "To", to },
                { "MessageSid", messageSid },
                { "AccountSid", accountSid},
                { "From", from },
                { "ApiVersion", apiVersion }
        })));
    }

    private void postDeliveryReportError(final String errorCode,
                                         final String smsSid,
                                         final String smsStatus,
                                         final String messageStatus,
                                         final String to,
                                         final String messageSid,
                                         final String accountSid,
                                         final String from,
                                         final String apiVersion) {
        assertNull(postRequest(TwilioController.REQUEST_PATH, createParameters(new String[][] {
                { "ErrorCode", errorCode },
                { "SmsSid", smsSid },
                { "SmsStatus", smsStatus },
                { "MessageStatus", messageStatus },
                { "To", to },
                { "MessageSid", messageSid },
                { "AccountSid", accountSid},
                { "From", from },
                { "ApiVersion", apiVersion }
        })));
    }

    @Test
    public void testPostDeliveryReportNoSentEntryInDB() {
        final String telephoneNumber = "991";
        postDeliveryReportSuccess(
                "SM5dbcbffd029d4eb18de4068b58e31234",
                "delivered",
                "delivered",
                telephoneNumber,
                "SM5dbcbffd029d4eb18de4068b58e31234",
                "ACfegg76bace9937efaa9932aabbcc1122",
                "+15005550006",
                "2010-04-01"
        );

        assertNull(smsSentLogRepository.findByTelephoneNumber(telephoneNumber));
    }

    @Test
    public void testPostDeliveryReportSmsSuccess() {
        final String telephoneNumber = "992";
        final String sid             = "y2390ß1jc";
        final String message         = "sms text success";
        final String twilioStatus    = "delivered";

        assertNotNull(smsSentLogRepository.save(new SmsSentLog(telephoneNumber, sid, "queued", message)));

        postDeliveryReportSuccess(
                sid,
                twilioStatus,
                twilioStatus,
                telephoneNumber,
                sid,
                "ACfegg76bace9937efaa9932aabbcc1122",
                "+15005550006",
                "2010-04-01"
        );

        assertAlmostEquals(message,
                new SmsSentLog(telephoneNumber, sid, twilioStatus, message, Instant.now()),
                smsSentLogRepository.findByTelephoneNumber(telephoneNumber));
    }

    @Test
    public void testPostDeliveryReport() {
        postDeliveryReportSuccess(
                "SM5dbcbffd029d4eb18de4068b58e31234",
                "sent",
                "sent",
                "+15005550006",
                "SM5dbcbffd029d4eb18de4068b58e31234",
                "ACfegg76bace9937efaa9932aabbcc1122",
                "+15005550006",
                "2010-04-01"
        );

        postDeliveryReportSuccess(
                "SM5dbcbffd029d4eb18de4068b58e31234",
                "delivered",
                "delivered",
                "+15005550006",
                "SM5dbcbffd029d4eb18de4068b58e31234",
                "ACfegg76bace9937efaa9932aabbcc1122",
                "+15005550006",
                "2010-04-01"
        );

        postDeliveryReportError(
                "30008",
                "SM5dbcbffd029d4eb18de4068b58e31234",
                "delivered",
                "delivered",
                "+15005550006",
                "SM5dbcbffd029d4eb18de4068b58e31234",
                "ACfegg76bace9937efaa9932aabbcc1122",
                "+15005550006",
                "2010-04-01"
        );
    }

    /// Todos
    // wrong parameter
    // more parameter
}
