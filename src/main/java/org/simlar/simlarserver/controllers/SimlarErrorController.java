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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.extern.slf4j.Slf4j;
import org.simlar.simlarserver.utils.RequestLogMessage;
import org.simlar.simlarserver.xml.XmlError;
import org.simlar.simlarserver.xmlerrorexceptionclientresponse.XmlErrorExceptionClientResponse;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorException;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

@Slf4j
@ControllerAdvice
@Controller
final class SimlarErrorController implements ErrorController {
    private static final String ERROR_PATH = "/error";

    private static ResponseEntity<XmlError> createXmlError(final HttpStatus status, final XmlErrorExceptionClientResponse response) {
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_XML).body(new XmlError(response.getId(), response.getMessage()));
    }

    // TODO: wait until spring removes deprecated function
    // see: https://github.com/spring-projects/spring-boot/issues/19844
    @SuppressWarnings({"deprecation", "RedundantSuppression"})
    @Override
    public String getErrorPath() {
        return ERROR_PATH;
    }

    @SuppressFBWarnings("SPRING_CSRF_UNRESTRICTED_REQUEST_MAPPING")
    @RequestMapping(path = ERROR_PATH)
    public static ResponseEntity<XmlError> whiteLabelErrorPage(final HttpServletRequest request) {
        final String    uri        = (String)    request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        final Integer   statusCode = (Integer)   request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        final String    message    = (String)    request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        final Throwable exception  = (Throwable) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);

        log.warn("white label error on uri = '{}' with statusCode '{}' and message '{}'", uri, statusCode, message, exception);
        return createXmlError(HttpStatus.INTERNAL_SERVER_ERROR, XmlErrorExceptionClientResponse.UNKNOWN_STRUCTURE);
    }

    @SuppressFBWarnings("SPRING_CSRF_UNRESTRICTED_REQUEST_MAPPING")
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

    @ExceptionHandler(Throwable.class)
    public static ResponseEntity<XmlError> handleException(final HttpServletRequest request, final Throwable throwable) {
        log.error("unhandled '{}' with request='{}'", throwable.getClass().getSimpleName(), new RequestLogMessage(request), throwable);
        return createXmlError(HttpStatus.INTERNAL_SERVER_ERROR, XmlErrorExceptionClientResponse.UNKNOWN_ERROR);
    }
}
