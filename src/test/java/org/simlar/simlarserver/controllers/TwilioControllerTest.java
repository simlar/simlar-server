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
import org.simlar.simlarserver.services.twilio.TwilioSmsService;
import org.simlar.simlarserver.xml.XmlTwilioCallResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Instant;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.simlar.simlarserver.helper.Asserts.assertAlmostEquals;

@SuppressWarnings("ALL")
@RunWith(SpringRunner.class)
public final class TwilioControllerTest extends BaseControllerTest {
    @Autowired
    private SmsSentLogRepository smsSentLogRepository;

    private void postDeliveryReportSuccess(final String smsSid,
                                           final String smsStatus,
                                           final String messageStatus,
                                           final String to,
                                           final String messageSid,
                                           final String accountSid,
                                           final String from,
                                           final String apiVersion) {
        assertNull(postRequest(TwilioSmsService.REQUEST_PATH_DELIVERY, createParameters(new String[][] {
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
        assertNull(postRequest(TwilioSmsService.REQUEST_PATH_DELIVERY, createParameters(new String[][] {
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
        final String twilioStatus1   = "sent";
        final String twilioStatus2   = "delivered";

        assertNotNull(smsSentLogRepository.save(new SmsSentLog(telephoneNumber, sid, "queued", message)));

        postDeliveryReportSuccess(
                sid,
                twilioStatus1,
                twilioStatus1,
                telephoneNumber,
                sid,
                "ACfegg76bace9937efaa9932aabbcc1122",
                "+15005550006",
                "2010-04-01"
        );

        assertAlmostEquals(message,
                new SmsSentLog(telephoneNumber, sid, twilioStatus1, message, Instant.now()),
                smsSentLogRepository.findByTelephoneNumber(telephoneNumber));

        postDeliveryReportSuccess(
                sid,
                twilioStatus2,
                twilioStatus2,
                telephoneNumber,
                sid,
                "ACfegg76bace9937efaa9932aabbcc1122",
                "+15005550006",
                "2010-04-01"
        );

        assertAlmostEquals(message,
                new SmsSentLog(telephoneNumber, sid, twilioStatus2, message, Instant.now()),
                smsSentLogRepository.findByTelephoneNumber(telephoneNumber));
    }

    @Test
    public void testPostDeliveryReportSmsSuccessMinimalValues() {
        final String telephoneNumber = "993";
        final String sid             = "yxyxcvyxc";
        final String message         = "sms text success";
        final String twilioStatus    = "delivered";

        assertNotNull(smsSentLogRepository.save(new SmsSentLog(telephoneNumber, sid, "queued", message)));

        assertNull(postRequest(TwilioSmsService.REQUEST_PATH_DELIVERY, createParameters(new String[][] {
                { "MessageStatus", twilioStatus },
                { "To", telephoneNumber },
                { "MessageSid", sid }
        })));

        assertAlmostEquals(message,
                new SmsSentLog(telephoneNumber, sid, twilioStatus, message, Instant.now()),
                smsSentLogRepository.findByTelephoneNumber(telephoneNumber));
    }

    @Test
    public void testPostDeliveryReportSmsSuccessMultilpeSids() {
        final String telephoneNumber = "994";
        final String sid1            = "yxyxcvyxc1";
        final String sid2            = "yxyxcvyxc2";
        final String message         = "sms text success";
        final String twilioStatus1   = "delivered";
        final String twilioStatus2   = "sent";

        assertNotNull(smsSentLogRepository.save(new SmsSentLog(telephoneNumber, sid1, twilioStatus1, message, Instant.now())));
        assertNotNull(smsSentLogRepository.save(new SmsSentLog(telephoneNumber, sid2, "queued", message)));

        postDeliveryReportSuccess(
                sid2,
                twilioStatus2,
                twilioStatus2,
                telephoneNumber,
                sid2,
                "ACfegg76bace9937efaa9932aabbcc1122",
                "+15005550006",
                "2010-04-01"
        );

        assertAlmostEquals(message,
                new SmsSentLog(telephoneNumber, sid1, twilioStatus1, message, Instant.now()),
                smsSentLogRepository.findByDlrNumber(sid1));

        assertAlmostEquals(message,
                new SmsSentLog(telephoneNumber, sid2, twilioStatus2, message, Instant.now()),
                smsSentLogRepository.findByDlrNumber(sid2));
    }

    @Test
    public void testPostDeliveryReportSmsSuccessDifferentTelefoneNumbers() {
        final String telephoneNumber1 = "995";
        final String telephoneNumber2 = "996";
        final String sid              = "sdfster57";
        final String message          = "sms text success";
        final String twilioStatus     = "sent";

        assertNotNull(smsSentLogRepository.save(new SmsSentLog(telephoneNumber1, sid, "queued", message)));

        postDeliveryReportSuccess(
                sid,
                twilioStatus,
                twilioStatus,
                telephoneNumber2,
                sid,
                "ACfegg76bace9937efaa9932aabbcc1122",
                "+15005550006",
                "2010-04-01"
        );

        assertAlmostEquals(message,
                new SmsSentLog(telephoneNumber1, sid, twilioStatus, message, Instant.now()),
                smsSentLogRepository.findByDlrNumber(sid));
    }

    @Test
    public void testPostDeliveryReportSmsError() {
        final String telephoneNumber = "997";
        final String sid             = "SM861a2b3299db4af1996d4ff9cb07f4cc";
        final String message         = "sms text error";
        final String twilioStatus    = "undelivered";

        assertNotNull(smsSentLogRepository.save(new SmsSentLog(telephoneNumber, sid, "queued", message)));

        postDeliveryReportError(
                "30009",
                sid,
                twilioStatus,
                twilioStatus,
                telephoneNumber,
                sid,
                "ACfegg76bace9937efaa9932aabbcc1122",
                "+32460202070",
                "2010-04-01");

        assertAlmostEquals(message,
                new SmsSentLog(telephoneNumber, sid, twilioStatus, "30009 - Missing segment", message, Instant.now()),
                smsSentLogRepository.findByDlrNumber(sid));
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

    private void postCallStatusSuccess(final String called,
                                       final String toState,
                                       final String callerCountry,
                                       final String direction,
                                       final String timestamp,
                                       final String callbackSource,
                                       final String callerState,
                                       final String toZip,
                                       final String sequenceNumber,
                                       final String to,
                                       final String callSid,
                                       final String toCountry,
                                       final String callerZip,
                                       final String calledZip,
                                       final String apiVersion,
                                       final String callStatus,
                                       final String calledCity,
                                       final String duration,
                                       final String from,
                                       final String callDuration,
                                       final String accountSid,
                                       final String calledCountry,
                                       final String callerCity,
                                       final String toCity,
                                       final String fromCountry,
                                       final String caller,
                                       final String fromCity,
                                       final String calledState,
                                       final String fromZip,
                                       final String fromState) {
        assertNull(postRequest(TwilioSmsService.REQUEST_PATH_CALL_STATUS, createParameters(new String[][] {
                { "Called", called },
                { "ToState", toState },
                { "CallerCountry", callerCountry },
                { "Direction", direction },
                { "Timestamp", timestamp },
                { "CallbackSource", callbackSource },
                { "CallerState", callerState },
                { "ToZip", toZip },
                { "SequenceNumber", sequenceNumber },
                { "To", to },
                { "CallSid", callSid },
                { "ToCountry", toCountry },
                { "CallerZip", callerZip },
                { "CalledZip", calledZip },
                { "ApiVersion", apiVersion },
                { "CallStatus", callStatus },
                { "CalledCity", calledCity },
                { "Duration", duration },
                { "From", from },
                { "CallDuration", callDuration },
                { "AccountSid", accountSid },
                { "CalledCountry", calledCountry },
                { "CallerCity", callerCity },
                { "ToCity", toCity },
                { "FromCountry", fromCountry },
                { "Caller", caller },
                { "FromCity", fromCity },
                { "CalledState", calledState },
                { "FromZip", fromZip },
                { "FromState", fromState }
        })));
    }

    @Test
    public void testPostCallStatus() {
        postCallStatusSuccess(
                "+49163123456",
                null,
                "US",
                "outbound-api",
                "Thu, 04 Jan 2018 10:55:35 +0000",
                "call-progress-events",
                "MD",
                "0",
                null,
                "+49163123456",
                "CA852dcbc6945b13213dfaa7f808724e74",
                "DE",
                "21229",
                null,
                "2010-04-01",
                "completed",
                null,
                "1",
                "+14102042044",
                "2",
                "ACfegg76bace9937efaa9932aabbcc1122",
                "DE",
                "ARBUTUS",
                null,
                "US",
                "+14102042044",
                "ARBUTUS",
                null,
                "21229",
                "MD");
    }

    private void postCallSuccess(final String called,
                                 final String toState,
                                 final String callerCountry,
                                 final String direction,
                                 final String callerState,
                                 final String toZip,
                                 final String callSid,
                                 final String to,
                                 final String callerZip,
                                 final String toCountry,
                                 final String apiVersion,
                                 final String calledZip,
                                 final String calledCity,
                                 final String callStatus,
                                 final String from,
                                 final String accountSid,
                                 final String calledCountry,
                                 final String callerCity,
                                 final String caller,
                                 final String fromCountry,
                                 final String toCity,
                                 final String fromCity,
                                 final String calledState,
                                 final String fromZip,
                                 final String fromState) {
        final XmlTwilioCallResponse response = postRequest(XmlTwilioCallResponse.class, TwilioSmsService.REQUEST_PATH_CALL, createParameters(new String[][] {
                { "Called", called },
                { "ToState", toState },
                { "CallerCountry", callerCountry },
                { "Direction", direction },
                { "CallerState", callerState },
                { "ToZip", toZip },
                { "CallSid", callSid },
                { "To", to },
                { "CallerZip", callerZip },
                { "ToCountry", toCountry },
                { "ApiVersion", apiVersion },
                { "CalledZip", calledZip },
                { "CalledCity", calledCity },
                { "CallStatus", callStatus },
                { "From", from },
                { "AccountSid", accountSid },
                { "CalledCountry", calledCountry },
                { "CallerCity", callerCity },
                { "Caller", caller },
                { "FromCountry", fromCountry },
                { "ToCity", toCity },
                { "FromCity", fromCity },
                { "CalledState", calledState },
                { "FromZip", fromZip },
                { "FromState", fromState }
        }));

        assertNotNull(response);
        assertNotNull(response.getSay());
    }

    @Test
    public void testPostCall() {
        postCallSuccess(
                "+49163123456",
                null,
                "US",
                "outbound-api",
                "MD",
                null,
                "CA852dcbc6945b13213dfaa7f808724e74",
                "+49163123456",
                "21229",
                "DE",
                "2010-04-01",
                null,
                null,
                "in-progress",
                "+14102042044",
                "ACfegg76bace9937efaa9932aabbcc1122",
                "DE",
                "ARBUTUS",
                "+14102042044",
                "US",
                null,
                "ARBUTUS",
                null,
                "21229",
                "MD");
    }
}
