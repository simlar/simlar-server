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

import lombok.AllArgsConstructor;
import lombok.extern.java.Log;
import org.simlar.simlarserver.services.createaccountservice.AccountRequest;
import org.simlar.simlarserver.services.createaccountservice.CreateAccountService;
import org.simlar.simlarserver.xml.XmlSuccessCreateAccountConfirm;
import org.simlar.simlarserver.xml.XmlSuccessCreateAccountRequest;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorUnknownStructureException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

@AllArgsConstructor(onConstructor = @__(@Autowired))
@Log
@RestController
final class CreateAccountController {
    public  static final String REQUEST_PATH    = "/create-account.xml";
    public  static final String COMMAND_REQUEST = "request";
    public  static final String COMMAND_CONFIRM = "confirm";

    private final CreateAccountService createAccountService;

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
     *            error message or success message containing simlarId and password
     */
    @SuppressWarnings("SpellCheckingInspection")
    @RequestMapping(value = REQUEST_PATH, method = RequestMethod.POST, produces = MediaType.APPLICATION_XML_VALUE)
    public XmlSuccessCreateAccountRequest createAccountRequest(final HttpServletRequest request, @RequestParam final String command, @RequestParam final String telephoneNumber, @RequestParam final String smsText) {
        log.info(REQUEST_PATH + " requested with command=\'" + command + "\' and User-Agent: " + request.getHeader("User-Agent"));

        if (!Objects.equals(command, COMMAND_REQUEST)) {
            throw new XmlErrorUnknownStructureException("create account request with command: " + command);
        }

        final AccountRequest accountRequest = createAccountService.createAccountRequest(telephoneNumber, smsText, request.getRemoteAddr());

        return new XmlSuccessCreateAccountRequest(accountRequest.getSimlarId().get(), accountRequest.getPassword());
    }


    /**
     * This method handles http post requests. You may test it with:
     * <blockquote>
     * curl --data "command=confirm&simlarId=*1111*&registrationCode=123456"" http://localhost:8080/create-account.xml
     * </blockquote>
     *
     * @param command
     *            needs tp be "confirm"
     * @param simlarId
     *            the simlarId to be confirmed
     * @param registrationCode
     *            the code sent by sms
     *
     * @return XmlError or XmlSuccessCreateAccountConfirm
     *            error message or success message containing simlarId and registrationCode
     */
    @RequestMapping(value = REQUEST_PATH, method = RequestMethod.POST, produces = MediaType.APPLICATION_XML_VALUE, params = { "command", "simlarId", "registrationCode"  })
    public XmlSuccessCreateAccountConfirm confirmAccount(final HttpServletRequest request, @RequestParam final String command, @RequestParam final String simlarId, @RequestParam final String registrationCode) {
        log.info(REQUEST_PATH + " confirm with command=\'" + command + "\' and User-Agent: " + request.getHeader("User-Agent"));

        if (!Objects.equals(command, COMMAND_CONFIRM)) {
            throw new XmlErrorUnknownStructureException("confirm account request with command: " + command);
        }

        createAccountService.confirmAccount(simlarId, registrationCode);

        return new XmlSuccessCreateAccountConfirm(simlarId, registrationCode);
    }
}
