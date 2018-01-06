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
import org.simlar.simlarserver.services.twilio.TwilioSmsService;
import org.simlar.simlarserver.utils.RequestLogMessage;
import org.simlar.simlarserver.xml.XmlTwilioCallResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@AllArgsConstructor(onConstructor = @__(@Autowired))
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
    @RequestMapping(value = TwilioSmsService.REQUEST_PATH_DELIVERY, method = RequestMethod.POST)
    public void deliveryReport(final HttpServletRequest request,
            @RequestParam(name = "MessageSid")                  final String messageSid,
            @RequestParam(name = "To")                          final String to,
            @RequestParam(name = "MessageStatus")               final String messageStatus,
            @RequestParam(name = "ErrorCode", required = false) final String errorCode
    ) {
        log.debug("'{}' request='{}'", TwilioSmsService.REQUEST_PATH_DELIVERY, new RequestLogMessage(request, false));
        log.info("'{}' requested with messageSid MessageSid='{}' To='{}' MessageStatus='{}' ErrorCode='{}'", TwilioSmsService.REQUEST_PATH_DELIVERY, messageSid, to, messageStatus, errorCode);

        twilioSmsService.handleDeliveryReport(to, messageSid, messageStatus, errorCode);
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
    @RequestMapping(value = TwilioSmsService.REQUEST_PATH_CALL_STATUS, method = RequestMethod.POST)
    public void callStatus(final HttpServletRequest request,
                           @RequestParam(name = "CallSid")    final String callSid,
                           @RequestParam(name = "To")         final String to,
                           @RequestParam(name = "CallStatus") final String callStatus
    ) {
        log.debug("'{}' request='{}'", TwilioSmsService.REQUEST_PATH_CALL_STATUS, new RequestLogMessage(request, false));
        log.info("'{}' requested with callSid='{}' To='{}' MessageStatus='{}'", TwilioSmsService.REQUEST_PATH_CALL_STATUS, callSid, to, callStatus);
    }

    /**
     * This method handles http post requests. You may test it with:
     * <blockquote>
     * curl --data "CallSid=12345678&To=123&MessageStatus=queued" http://localhost:8080/twilio/call.json
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
    @RequestMapping(value = TwilioSmsService.REQUEST_PATH_CALL, method = RequestMethod.POST)
    public XmlTwilioCallResponse call(final HttpServletRequest request,
                                      @RequestParam(name = "CallSid")    final String callSid,
                                      @RequestParam(name = "To")         final String to,
                                      @RequestParam(name = "CallStatus") final String callStatus
    ) {
        log.debug("'{}' request='{}'", TwilioSmsService.REQUEST_PATH_CALL, new RequestLogMessage(request, false));
        log.info("'{}' requested with callSid='{}' To='{}' MessageStatus='{}'", TwilioSmsService.REQUEST_PATH_CALL, callSid, to, callStatus);

        return new XmlTwilioCallResponse("Welcome to simlar! Your registration number is: 123456");
    }
}
