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
import lombok.extern.slf4j.Slf4j;
import org.simlar.simlarserver.services.accountservice.AccountRequest;
import org.simlar.simlarserver.services.accountservice.AccountService;
import org.simlar.simlarserver.utils.SimlarId;
import org.simlar.simlarserver.xml.XmlSuccessCreateAccountConfirm;
import org.simlar.simlarserver.xml.XmlSuccessCreateAccountRequest;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorUnknownStructureException;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.ServletRequest;
import java.util.Objects;

@AllArgsConstructor
@Slf4j
@RestController
final class AccountController {
    public static final String REQUEST_PATH      = "/create-account.xml";
    public static final String REQUEST_PATH_CALL = "/create-account-call.xml";
    public static final String COMMAND_REQUEST   = "request";
    public static final String COMMAND_CONFIRM   = "confirm";

    private final AccountService accountService;

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
     * @return XmlError or XmlSuccessCreateAccountRequest
     *            error message or success message containing simlarId and password
     */
    @PostMapping(value = REQUEST_PATH, produces = MediaType.APPLICATION_XML_VALUE)
    public XmlSuccessCreateAccountRequest createAccountRequest(final ServletRequest request, @RequestParam final String command, @RequestParam final String telephoneNumber, @RequestParam final String smsText) {
        log.info("'{}' requested with command= '{}'", REQUEST_PATH, command);

        if (!Objects.equals(command, COMMAND_REQUEST)) {
            throw new XmlErrorUnknownStructureException("create account request with command: " + command);
        }

        final AccountRequest accountRequest = accountService.createAccountRequest(telephoneNumber, smsText, request.getRemoteAddr());

        return new XmlSuccessCreateAccountRequest(accountRequest.simlarId().get(), accountRequest.password());
    }

    /**
     * This method handles http post requests. You may test it with:
     * <blockquote>
     * curl --data "command=request&telephoneNumber=%2b1111&smsText=ios-en" http://localhost:8080/create-account-call.xml
     * </blockquote>
     *
     * @param telephoneNumber
     *            the telephone number identifying the account
     * @param password
     *            the password you received with create account request
     *
     * @return XmlError or XmlSuccessCreateAccountRequest
     *            error message or success message containing simlarId and password
     */
    @PostMapping(value = REQUEST_PATH_CALL, produces = MediaType.APPLICATION_XML_VALUE)
    public XmlSuccessCreateAccountRequest createAccountCall(@RequestParam final String telephoneNumber, @RequestParam final String password) {
        log.info("'{}' requested with telephoneNumber= '{}'", REQUEST_PATH_CALL, telephoneNumber);

        final SimlarId simlarId = accountService.call(telephoneNumber, password);

        return new XmlSuccessCreateAccountRequest(simlarId.get(), password);
    }

    /**
     * This method handles http post requests. You may test it with:
     * <blockquote>
     * curl --data "command=confirm&simlarId=*1111*&registrationCode=123456" http://localhost:8080/create-account.xml
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
    @PostMapping(value = REQUEST_PATH, produces = MediaType.APPLICATION_XML_VALUE, params = { "command", "simlarId", "registrationCode"  })
    public XmlSuccessCreateAccountConfirm confirmAccount(@RequestParam final String command, @RequestParam final String simlarId, @RequestParam final String registrationCode) {
        log.info("'{}' confirm with command='{}'", REQUEST_PATH, command);

        if (!Objects.equals(command, COMMAND_CONFIRM)) {
            throw new XmlErrorUnknownStructureException("confirm account request with command: " + command);
        }

        accountService.confirmAccount(simlarId, registrationCode);

        return new XmlSuccessCreateAccountConfirm(simlarId, registrationCode);
    }
}
