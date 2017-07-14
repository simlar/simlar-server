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

import org.simlar.simlarserver.services.smsservice.SmsService;
import org.simlar.simlarserver.utils.Password;
import org.simlar.simlarserver.utils.SimlarId;
import org.simlar.simlarserver.xml.XmlSuccessCreateAccountRequest;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorFailedToSendSmsException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorInvalidTelephoneNumberException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorUnknownStructureException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.logging.Logger;

@RestController
final class CreateAccountController {
    public  static final String REQUEST_PATH    = "/create-account.xml";
    public  static final String COMMAND_REQUEST = "request";
    private static final Logger LOGGER          = Logger.getLogger(CreateAccountController.class.getName());

    private final SmsService smsService;

    @Autowired
    private CreateAccountController(final SmsService smsService) {
        this.smsService = smsService;
    }

    /**
     * This method handles http post requests. You may test it with:
     * <blockquote>
     * curl --data "command=request&telephoneNumber=%2b1111&smsText=ios-en" http://localhost:8080/create-account.xml
     * </blockquote>
     *
     * @param command
     *            needs tp be "request"
     * @param telephoneNumber
     *            the telephone number identifying the account
     * @param smsText
     *            ios-en
     *            android-en
     *            android-de
     *            android-es
     *
     * @return XmlError or XmlSuccessPushNotification
     *            error message or success message containing deviceType and pushId
     */
    @SuppressWarnings("SpellCheckingInspection")
    @RequestMapping(value = REQUEST_PATH, method = RequestMethod.POST, produces = MediaType.APPLICATION_XML_VALUE)
    public XmlSuccessCreateAccountRequest createAccountRequest(@RequestParam final String command, @RequestParam final String telephoneNumber, @RequestParam final String smsText) {
        LOGGER.info(REQUEST_PATH + " requested with command=\"" + command + '\"');

        if (!Objects.equals(command, COMMAND_REQUEST)) {
            throw new XmlErrorUnknownStructureException("create account request with command: " + command);
        }

        final SimlarId simlarId = SimlarId.createWithTelephoneNumber(telephoneNumber);
        if (simlarId == null) {
            throw new XmlErrorInvalidTelephoneNumberException("invalid telephone number: " + telephoneNumber);
        }

        if (!smsService.sendSms(telephoneNumber, smsText)) {
            throw new XmlErrorFailedToSendSmsException("failed to send sms to '" + telephoneNumber + "' with text: " + smsText);
        }

        return new XmlSuccessCreateAccountRequest(simlarId.get(), Password.generate());
    }
}
