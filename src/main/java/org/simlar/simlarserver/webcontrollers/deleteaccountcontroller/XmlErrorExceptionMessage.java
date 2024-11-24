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


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorFailedToSendSmsException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorInvalidTelephoneNumberException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorNoRegistrationCodeException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorNoSimlarIdException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorTooManyConfirmTriesException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorTooManyRequestTriesException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorWrongRegistrationCodeException;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public enum XmlErrorExceptionMessage {
        UNKNOWN_ERROR              (null,                                          "unknown error"),
        INVALID_TELEPHONE_NUMBER   (XmlErrorInvalidTelephoneNumberException.class, "invalid telephone number"),
        TOO_MANY_REQUEST_TRIES     (XmlErrorTooManyRequestTriesException.class,    "too many account deletion requests"),
        FAILED_TO_SEND_SMS         (XmlErrorFailedToSendSmsException.class,        "failed to send sms"),
        TOO_MANY_CONFIRM_TRIES     (XmlErrorTooManyConfirmTriesException.class,    "too many confirm tries"),
        WRONG_REGISTRATION_CODE    (XmlErrorWrongRegistrationCodeException.class,  "wrong deletion code"),
        NO_SIMLAR_ID               (XmlErrorNoSimlarIdException.class,             "no simlarId"),
        NO_REGISTRATION_CODE       (XmlErrorNoRegistrationCodeException.class,     "no deletion code"),
    ;


    @Getter(AccessLevel.PRIVATE)
    private final Class<? extends XmlErrorException> exceptionClass;

    private final String message;

    @SuppressWarnings("StaticCollection")
    private static final Map<Class<? extends XmlErrorException>, XmlErrorExceptionMessage> EXCEPTION_CLIENT_RESPONSE_MAP =
            Collections.unmodifiableMap(Arrays.stream(values()).collect(Collectors.toMap(XmlErrorExceptionMessage::getExceptionClass, Function.identity(), (c1, c2) -> c1)));

    public static XmlErrorExceptionMessage fromException(final Class<? extends XmlErrorException> exceptionClass) {
        return EXCEPTION_CLIENT_RESPONSE_MAP.getOrDefault(exceptionClass, UNKNOWN_ERROR);
    }
}
