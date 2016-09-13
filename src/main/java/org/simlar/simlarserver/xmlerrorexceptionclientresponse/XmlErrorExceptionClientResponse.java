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

package org.simlar.simlarserver.xmlerrorexceptionclientresponse;

import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorRequestedTooManyContactsException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorUnknownApplePushIdException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorUnknownPushIdTypeException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorWrongCredentialsException;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum XmlErrorExceptionClientResponse {
        UNKNOWN_STRUCTURE          (null,                                             1, "unknown structure"),
        WRONG_CREDENTIALS          (XmlErrorWrongCredentialsException.class,         10, "wrong credentials"),
        UNKNOWN_PUSH_ID_TYPE       (XmlErrorUnknownPushIdTypeException.class,        30, "unknown push id type"),
        UNKNOWN_APPLE_PUSH_ID      (XmlErrorUnknownApplePushIdException.class,       31, "unknown apple pushId"),
        REQUESTED_TOO_MANY_CONTACTS(XmlErrorRequestedTooManyContactsException.class, 50, "requested too many contacts")
    ;


    private final Class<? extends XmlErrorException> exceptionClass;
    private final int id;
    private final String message;

    private static final Map<Class<? extends XmlErrorException>, XmlErrorExceptionClientResponse> EXCEPTION_CLIENT_RESPONSE_MAP
            = Arrays.stream(XmlErrorExceptionClientResponse.values()).collect(Collectors.toMap(response -> response.exceptionClass, response -> response));

    public static XmlErrorExceptionClientResponse fromException(final XmlErrorException exception) {
        return EXCEPTION_CLIENT_RESPONSE_MAP.get(exception.getClass());
    }

    XmlErrorExceptionClientResponse(final Class<? extends XmlErrorException> exceptionClass, final int id, final String message) {
        this.exceptionClass = exceptionClass;
        this.id = id;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public int getId() {
        return id;
    }
}
