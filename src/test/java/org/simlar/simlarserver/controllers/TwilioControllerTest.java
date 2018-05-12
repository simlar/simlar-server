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
import org.simlar.simlarserver.data.TwilioRequestType;
import org.simlar.simlarserver.database.models.SmsProviderLog;
import org.simlar.simlarserver.database.repositories.SmsProviderLogRepository;
import org.simlar.simlarserver.services.twilio.TwilioSmsService;
import org.simlar.simlarserver.xml.XmlError;
import org.simlar.simlarserver.xml.XmlTwilioCallResponse;
import org.simlar.simlarserver.xml.XmlTwilioSay;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.MultiValueMap;

import java.time.Instant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.simlar.simlarserver.helper.Asserts.assertAlmostEquals;

@TestPropertySource(properties = "domain = sip.simlar.org") // domain is an essential part of the callback url
@SuppressWarnings("ALL")
@RunWith(SpringRunner.class)
public final class TwilioControllerTest extends BaseControllerTest {
    @Autowired
    private SmsProviderLogRepository smsProviderLogRepository;

    private void assertPostDeliveryReportSuccess(final String smsSid,
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

    private void assertPostDeliveryReportError(final String errorCode,
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
        assertPostDeliveryReportSuccess(
                "SM5dbcbffd029d4eb18de4068b58e31234",
                "delivered",
                "delivered",
                telephoneNumber,
                "SM5dbcbffd029d4eb18de4068b58e31234",
                "ACfegg76bace9937efaa9932aabbcc1122",
                "+15005550006",
                "2010-04-01"
        );

        assertNull(smsProviderLogRepository.findByTelephoneNumber(telephoneNumber));
    }

    @Test
    public void testPostDeliveryReportSmsSuccess() {
        final String telephoneNumber = "992";
        final String sid             = "y2390ß1jc";
        final String message         = "sms text success";
        final String status1         = "sent";
        final String status2         = "delivered";

        assertNotNull(smsProviderLogRepository.save(new SmsProviderLog(TwilioRequestType.CALL, telephoneNumber, sid, "queued", message)));

        assertPostDeliveryReportSuccess(
                sid,
                status1,
                status1,
                telephoneNumber,
                sid,
                "ACfegg76bace9937efaa9932aabbcc1122",
                "+15005550006",
                "2010-04-01"
        );

        assertAlmostEquals(message,
                new SmsProviderLog(TwilioRequestType.SMS, telephoneNumber, sid, status1, message, Instant.now()),
                smsProviderLogRepository.findByTelephoneNumber(telephoneNumber));

        assertPostDeliveryReportSuccess(
                sid,
                status2,
                status2,
                telephoneNumber,
                sid,
                "ACfegg76bace9937efaa9932aabbcc1122",
                "+15005550006",
                "2010-04-01"
        );

        assertAlmostEquals(message,
                new SmsProviderLog(TwilioRequestType.SMS, telephoneNumber, sid, status2, message, Instant.now()),
                smsProviderLogRepository.findByTelephoneNumber(telephoneNumber));
    }

    @Test
    public void testPostDeliveryReportSmsSuccessMinimalValues() {
        final String telephoneNumber = "993";
        final String sid             = "yxyxcvyxc";
        final String message         = "sms text success";
        final String status          = "delivered";

        assertNotNull(smsProviderLogRepository.save(new SmsProviderLog(TwilioRequestType.SMS, telephoneNumber, sid, "queued", message)));

        assertNull(postRequest(TwilioSmsService.REQUEST_PATH_DELIVERY, createParameters(new String[][] {
                { "MessageStatus", status },
                { "To", telephoneNumber },
                { "MessageSid", sid }
        })));

        assertAlmostEquals(message,
                new SmsProviderLog(TwilioRequestType.SMS, telephoneNumber, sid, status, message, Instant.now()),
                smsProviderLogRepository.findByTelephoneNumber(telephoneNumber));
    }

    @Test
    public void testPostDeliveryReportSmsSuccessMultilpeSids() {
        final String telephoneNumber = "994";
        final String sid1            = "yxyxcvyxc1";
        final String sid2            = "yxyxcvyxc2";
        final String message         = "sms text success";
        final String status1         = "delivered";
        final String status2         = "sent";

        assertNotNull(smsProviderLogRepository.save(new SmsProviderLog(TwilioRequestType.SMS, telephoneNumber, sid1, status1, message, Instant.now())));
        assertNotNull(smsProviderLogRepository.save(new SmsProviderLog(TwilioRequestType.SMS, telephoneNumber, sid2, "queued", message)));

        assertPostDeliveryReportSuccess(
                sid2,
                status2,
                status2,
                telephoneNumber,
                sid2,
                "ACfegg76bace9937efaa9932aabbcc1122",
                "+15005550006",
                "2010-04-01"
        );

        assertAlmostEquals(message,
                new SmsProviderLog(TwilioRequestType.SMS, telephoneNumber, sid1, status1, message, Instant.now()),
                smsProviderLogRepository.findBySessionId(sid1));

        assertAlmostEquals(message,
                new SmsProviderLog(TwilioRequestType.SMS, telephoneNumber, sid2, status2, message, Instant.now()),
                smsProviderLogRepository.findBySessionId(sid2));
    }

    @Test
    public void testPostDeliveryReportSmsSuccessDifferentTelefoneNumbers() {
        final String telephoneNumber1 = "995";
        final String telephoneNumber2 = "996";
        final String sid              = "sdfster57";
        final String message          = "sms text success";
        final String status           = "sent";

        assertNotNull(smsProviderLogRepository.save(new SmsProviderLog(TwilioRequestType.SMS, telephoneNumber1, sid, "queued", message)));

        assertPostDeliveryReportSuccess(
                sid,
                status,
                status,
                telephoneNumber2,
                sid,
                "ACfegg76bace9937efaa9932aabbcc1122",
                "+15005550006",
                "2010-04-01"
        );

        assertAlmostEquals(message,
                new SmsProviderLog(TwilioRequestType.SMS, telephoneNumber1, sid, status, message, Instant.now()),
                smsProviderLogRepository.findBySessionId(sid));
    }

    @Test
    public void testPostDeliveryReportSmsError() {
        final String telephoneNumber = "997";
        final String sid             = "SM861a2b3299db4af1996d4ff9cb07f4cc";
        final String message         = "sms text error";
        final String status          = "undelivered";

        assertNotNull(smsProviderLogRepository.save(new SmsProviderLog(TwilioRequestType.SMS, telephoneNumber, sid, "queued", message)));

        assertPostDeliveryReportError(
                "30009",
                sid,
                status,
                status,
                telephoneNumber,
                sid,
                "ACfegg76bace9937efaa9932aabbcc1122",
                "+32460202070",
                "2010-04-01");

        assertAlmostEquals(message,
                new SmsProviderLog(TwilioRequestType.SMS, telephoneNumber, sid, status, "30009 - Missing segment", message, Instant.now()),
                smsProviderLogRepository.findBySessionId(sid));
    }

    @Test
    public void testPostDeliveryReport() {
        assertPostDeliveryReportSuccess(
                "SM5dbcbffd029d4eb18de4068b58e31234",
                "sent",
                "sent",
                "+15005550006",
                "SM5dbcbffd029d4eb18de4068b58e31234",
                "ACfegg76bace9937efaa9932aabbcc1122",
                "+15005550006",
                "2010-04-01"
        );

        assertPostDeliveryReportSuccess(
                "SM5dbcbffd029d4eb18de4068b58e31234",
                "delivered",
                "delivered",
                "+15005550006",
                "SM5dbcbffd029d4eb18de4068b58e31234",
                "ACfegg76bace9937efaa9932aabbcc1122",
                "+15005550006",
                "2010-04-01"
        );

        assertPostDeliveryReportError(
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
        final String telephoneNumber = "+49163123456";
        final String sid             = "CA852dcbc6945b13213dfaa7f808724e74";
        final String status          = "ringing";

        assertNotNull(smsProviderLogRepository.save(new SmsProviderLog(TwilioRequestType.CALL, telephoneNumber, sid, "queued", null)));

        postCallStatusSuccess(
                telephoneNumber,
                null,
                "US",
                "outbound-api",
                "Thu, 04 Jan 2018 10:55:35 +0000",
                "call-progress-events",
                "MD",
                "0",
                null,
                telephoneNumber,
                sid,
                "DE",
                "21229",
                null,
                "2010-04-01",
                status,
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

        assertAlmostEquals("callStatus",
                new SmsProviderLog(TwilioRequestType.CALL, telephoneNumber, sid, status, null, null, Instant.now()),
                smsProviderLogRepository.findBySessionId(sid));
    }

    private static MultiValueMap<String, String> createPostCallParameters(final String called,
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
        return createParameters(new String[][] {
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
        });
    }

    private static MultiValueMap<String, String> createPostCallParameters(final String callSid, final String to) {
        return createPostCallParameters(
                to,
                null,
                "US",
                "outbound-api",
                "MD",
                null,
                callSid,
                to,
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

    private XmlTwilioCallResponse postCallSuccess(final String callSid, final String to) {
        return postRequest(XmlTwilioCallResponse.class, TwilioSmsService.REQUEST_PATH_CALL, createPostCallParameters(callSid, to));
    }

    private void postCallError(final String callSid, final String to) {
        final XmlError response = postRequest(XmlError.class, TwilioSmsService.REQUEST_PATH_CALL, createPostCallParameters(callSid, to));
        assertNotNull(response);
        assertEquals(64, response.getId());
    }

    @Test
    public void testPostCall() {
        final String telephoneNumber = "+49163123457";
        final String callSid         = "XYZ123457999AAECRTGT";
        final String message         = "Welcome to simlar! Your registration code is 654321";

        postCallError(callSid, telephoneNumber);

        assertNotNull(smsProviderLogRepository.save(new SmsProviderLog(TwilioRequestType.CALL, telephoneNumber, callSid, "ringing", message)));

        final XmlTwilioCallResponse response = postCallSuccess(callSid, telephoneNumber);

        assertNotNull(response);
        final XmlTwilioSay say = response.getSay();
        assertNotNull(say);
        assertEquals(message, say.getMessage());
    }
}
