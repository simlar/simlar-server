/*
 * Copyright (C) The Simlar Authors.
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

package org.simlar.simlarserver.webcontrollers.deleteaccountcontroller;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.simlar.simlarserver.services.accountservice.AccountService;
import org.simlar.simlarserver.utils.RequestLogMessage;
import org.simlar.simlarserver.webcontrollers.deleteaccountcontroller.models.DeleteAccountConfirm;
import org.simlar.simlarserver.webcontrollers.deleteaccountcontroller.models.DeleteAccountRequest;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@SuppressWarnings({"SameReturnValue", "ClassWithTooManyTransitiveDependencies"})
@AllArgsConstructor
@Slf4j
@Controller
final class DeleteAccountController {
    public static final String REQUEST_PATH_REQUEST = "/delete-account/request";
    public static final String REQUEST_PATH_CONFIRM = "/delete-account/confirm";
    public static final String REQUEST_PATH_RESULT  = "/delete-account/result";

    private final AccountService accountService;

    @GetMapping(REQUEST_PATH_REQUEST)
    public static String requestForm(final Model model) {
        model.addAttribute("request", new DeleteAccountRequest());
        return "delete-account/request";
    }

    @PostMapping(REQUEST_PATH_CONFIRM)
    public String requestSubmit(final ServletRequest request, @ModelAttribute final DeleteAccountRequest deleteAccountRequest, final Model model) {
        log.info("account deletion request with telephoneNumber '{}'", deleteAccountRequest.getTelephoneNumber());
        accountService.deleteAccountRequest(deleteAccountRequest.getTelephoneNumber(), request.getRemoteAddr());
        model.addAttribute("confirm", new DeleteAccountConfirm(deleteAccountRequest.getTelephoneNumber(), null));
        return "delete-account/confirm";
    }

    @PostMapping(REQUEST_PATH_RESULT)
    public String confirmSubmit(@ModelAttribute final DeleteAccountConfirm deleteAccountConfirm, final Model model) {
        log.info("account deletion confirm with telephoneNumber '{}' and code '{}'", deleteAccountConfirm.getTelephoneNumber(), deleteAccountConfirm.getDeletionCode());
        accountService.confirmAccountDeletion(deleteAccountConfirm.getTelephoneNumber(), deleteAccountConfirm.getDeletionCode());
        model.addAttribute("telephoneNumber", deleteAccountConfirm.getTelephoneNumber());
        return "delete-account/result";
    }

    @ExceptionHandler(XmlErrorException.class)
    public static String handleXmlErrorException(final HttpServletRequest request, final XmlErrorException xmlErrorException, final Model model) {
        final String message = XmlErrorExceptionMessage.fromException(xmlErrorException.getClass()).getMessage();
        log.warn("'{}' => XmlError('{}') {} with request='{}'", xmlErrorException.getClass().getSimpleName(), message, xmlErrorException.getMessage(), new RequestLogMessage(request));
        model.addAttribute("message", message);
        return "delete-account/error";
    }
}
