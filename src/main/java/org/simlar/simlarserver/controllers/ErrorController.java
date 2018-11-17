/*
 * Copyright (C) 2015 The Simlar Authors.
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

import lombok.extern.slf4j.Slf4j;
import org.simlar.simlarserver.utils.RequestLogMessage;
import org.simlar.simlarserver.xml.XmlError;
import org.simlar.simlarserver.xmlerrorexceptionclientresponse.XmlErrorExceptionClientResponse;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@ControllerAdvice
@Controller
final class ErrorController {
    private static ResponseEntity<XmlError> createXmlError(final HttpStatus status, final XmlErrorExceptionClientResponse response) {
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_XML).body(new XmlError(response.getId(), response.getMessage()));
    }

    @RequestMapping(path = "*")
    public static ResponseEntity<XmlError> handle(final HttpServletRequest request) {
        log.warn("Request Error with request='{}'", new RequestLogMessage(request));
        return createXmlError(HttpStatus.NOT_FOUND, XmlErrorExceptionClientResponse.UNKNOWN_STRUCTURE);
    }

    @ExceptionHandler(XmlErrorException.class)
    public static ResponseEntity<XmlError> handleXmlErrorException(final HttpServletRequest request, final XmlErrorException xmlErrorException) {
        final Class<? extends XmlErrorException> exceptionClass = xmlErrorException.getClass();
        final XmlErrorExceptionClientResponse response = XmlErrorExceptionClientResponse.fromException(exceptionClass);
        if (response == null) {
            log.error("XmlErrorException with no XmlErrorExceptionClientResponse found for '{}' with request='{}'", exceptionClass.getSimpleName(), new RequestLogMessage(request), xmlErrorException);
            return createXmlError(HttpStatus.INTERNAL_SERVER_ERROR, XmlErrorExceptionClientResponse.UNKNOWN_ERROR);
        }

        log.warn("'{}' => XmlError('{}', '{}') {} with request='{}'", xmlErrorException.getClass().getSimpleName(), response.getId(), response.getMessage(), xmlErrorException.getMessage(), new RequestLogMessage(request));
        return createXmlError(HttpStatus.OK, response);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public static ResponseEntity<XmlError> handleMissingParameterException(final HttpServletRequest request, final MissingServletRequestParameterException exception) {
        log.error("{}: {} with request='{}'", exception.getClass().getSimpleName(), exception.getMessage(), new RequestLogMessage(request), exception);
        return createXmlError(HttpStatus.OK, XmlErrorExceptionClientResponse.UNKNOWN_STRUCTURE);
    }

    @ExceptionHandler(Exception.class)
    public static ResponseEntity<XmlError> handleException(final HttpServletRequest request, final Exception exception) {
        log.error("unhandled '{}' with request='{}'", exception.getClass().getSimpleName(), new RequestLogMessage(request), exception);
        return createXmlError(HttpStatus.INTERNAL_SERVER_ERROR, XmlErrorExceptionClientResponse.UNKNOWN_ERROR);
    }
}
