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

package org.simlar.simlarserver.controllers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.simlar.simlarserver.data.TwilioRequestType;
import org.simlar.simlarserver.services.twilio.TwilioSmsService;
import org.simlar.simlarserver.utils.RequestLogMessage;
import org.simlar.simlarserver.xml.XmlTwilioCallResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

@AllArgsConstructor
@Slf4j
@RestController
final class TwilioController {

    private final TwilioSmsService twilioSmsService;

    /**
     * This method handles http post requests. You may test it with:
     * <blockquote>
     * curl --data "MessageSid=12345678&MessageStatus=Queued&To=123&ErrorCode=30008" http://localhost:8080/twilio/delivery-report.json
     * </blockquote>
     *
     * @param messageSid
     *            Twilio's message id
     * @param to
     *            receipient's telephone number
     * @param messageStatus
     *            e.g. failed, undelivered, queued, sent, delivered
     * @param errorCode
     *            Twilio error code
     */
    @SuppressWarnings("SpellCheckingInspection")
    @PostMapping(TwilioSmsService.REQUEST_PATH_DELIVERY)
    public void deliveryReport(final HttpServletRequest request,
            @RequestParam(name = "MessageSid")                  final String messageSid,
            @RequestParam(name = "To")                          final String to,
            @RequestParam(name = "MessageStatus")               final String messageStatus,
            @RequestParam(name = "ErrorCode", required = false) final String errorCode
    ) {
        log.debug("'{}' request='{}'", TwilioSmsService.REQUEST_PATH_DELIVERY, new RequestLogMessage(request, false));
        log.info("'{}' requested with messageSid MessageSid='{}' To='{}' MessageStatus='{}' ErrorCode='{}'", TwilioSmsService.REQUEST_PATH_DELIVERY, messageSid, to, messageStatus, errorCode);

        twilioSmsService.handleStatusReport(TwilioRequestType.SMS, to, messageSid, messageStatus, errorCode);
    }

    /**
     * This method handles http post requests. You may test it with:
     * <blockquote>
     * curl --data "CallSid=12345678&To=123&MessageStatus=queued" http://localhost:8080/twilio/call-status.json
     * </blockquote>
     *
     * @param callSid
     *            Twilio's call id
     * @param to
     *            receipient's telephone number
     * @param callStatus
     *            e.g. queued, ringing, in-progress, completed, busy, failed, no-answer, canceled
     */
    @SuppressWarnings("SpellCheckingInspection")
    @PostMapping(TwilioSmsService.REQUEST_PATH_CALL_STATUS)
    public void callStatus(final HttpServletRequest request,
                           @RequestParam(name = "CallSid")    final String callSid,
                           @RequestParam(name = "To")         final String to,
                           @RequestParam(name = "CallStatus") final String callStatus
    ) {
        log.debug("'{}' request='{}'", TwilioSmsService.REQUEST_PATH_CALL_STATUS, new RequestLogMessage(request, false));
        log.info("'{}' requested with callSid='{}' To='{}' MessageStatus='{}'", TwilioSmsService.REQUEST_PATH_CALL_STATUS, callSid, to, callStatus);

        twilioSmsService.handleStatusReport(TwilioRequestType.CALL, to, callSid, callStatus, null);
    }

    /**
     * This method handles http post requests. You may test it with:
     * <blockquote>
     * curl --data "CallSid=12345678&To=123&MessageStatus=queued" http://localhost:8080/twilio/call.xml
     * </blockquote>
     *
     * @param callSid
     *            Twilio's call id
     * @param to
     *            receipient's telephone number
     * @param callStatus
     *            e.g. queued, ringing, in-progress, completed, busy, failed, no-answer, canceled
     */
    @SuppressWarnings("SpellCheckingInspection")
    @PostMapping(value = TwilioSmsService.REQUEST_PATH_CALL, produces = MediaType.APPLICATION_XML_VALUE)
    public XmlTwilioCallResponse call(final HttpServletRequest request,
                                      @RequestParam(name = "CallSid")    final String callSid,
                                      @RequestParam(name = "To")         final String to,
                                      @RequestParam(name = "CallStatus") final String callStatus
    ) {
        log.debug("'{}' request='{}'", TwilioSmsService.REQUEST_PATH_CALL, new RequestLogMessage(request, false));
        log.info("'{}' requested with callSid='{}' To='{}' MessageStatus='{}'", TwilioSmsService.REQUEST_PATH_CALL, callSid, to, callStatus);

        return twilioSmsService.handleCall(callSid, to, callStatus);
    }
}
