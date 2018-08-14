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

import org.simlar.simlarserver.xml.XmlError;
import org.simlar.simlarserver.xmlerrorexceptionclientresponse.XmlErrorExceptionClientResponse;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorException;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

@ControllerAdvice
@RestController
final class ErrorController {
    private static final Logger LOGGER = Logger.getLogger(ErrorController.class.getName());

    private static void log(final Level level, final String prefix, final HttpServletRequest request, final Exception exception) {
        final String message = prefix + (request == null ? " no request object" :
                " URL='" + request.getRequestURL() + "' IP='" + request.getRemoteAddr() + "' User-Agent='" + request.getHeader("User-Agent") + '\'');

        LOGGER.log(level, message, exception);
    }

    @RequestMapping(path = "*", produces = MediaType.APPLICATION_XML_VALUE)
    public static XmlError handle(final HttpServletRequest request) {
        log(Level.WARNING, "Request Error:", request, null);
        return createXmlError(XmlErrorExceptionClientResponse.UNKNOWN_STRUCTURE);
    }

    private static XmlError createXmlError(final XmlErrorExceptionClientResponse response) {
        return new XmlError(response.getId(), response.getMessage());
    }

    private static String createXmlErrorString(final XmlErrorExceptionClientResponse response) {
        final StringWriter writer = new StringWriter();

        try {
            JAXBContext.newInstance(XmlError.class).createMarshaller().marshal(createXmlError(response), writer);
        } catch (final JAXBException e) {
            LOGGER.log(Level.SEVERE, "xmlParse error: ", e);
        }

        return writer.toString();
    }

    // in order to handle html request errors we have to return a String here
    @ExceptionHandler(XmlErrorException.class)
    public static String handleXmlErrorException(final HttpServletRequest request, final XmlErrorException xmlErrorException) {
        final XmlErrorExceptionClientResponse response = XmlErrorExceptionClientResponse.fromException(xmlErrorException);
        if (response == null) {
            log(Level.SEVERE, "XmlErrorException with no XmlErrorExceptionClientResponse found for: " + xmlErrorException.getClass().getSimpleName(), request, xmlErrorException);
        } else {
            log(Level.WARNING, "XmlError(" + response.getId() + ") " + response.getMessage() + ": " + xmlErrorException.getMessage(), request, null);
        }

        return createXmlErrorString(response);
    }

    // in order to handle html request errors we have to return a String here
    @ExceptionHandler(Exception.class)
    public static String handleException(final HttpServletRequest request, final Exception exception) {
        log(Level.SEVERE, "unhandled exception:", request, exception);

        return createXmlErrorString(XmlErrorExceptionClientResponse.UNKNOWN_STRUCTURE);
    }
}
