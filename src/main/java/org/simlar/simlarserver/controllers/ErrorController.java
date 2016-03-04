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
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

@ControllerAdvice
@Controller
final class ErrorController {
    private static final Logger LOGGER = Logger.getLogger(ErrorController.class.getName());

    private static void log(final HttpServletRequest request, final Exception exception) {
        final StringBuilder message = new StringBuilder();
        message.append("Request Error:");

        if (request == null) {
            message.append(" no request object");
        } else {
            message.append(" URL='").append(request.getRequestURL()).append('\'');
            message.append(" IP='").append(request.getRemoteAddr()).append('\'');
            message.append(" User-Agent='").append(request.getHeader("User-Agent")).append('\'');
        }

        LOGGER.log(Level.SEVERE, message.toString(), exception);
    }

    @RequestMapping(path = "*", produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public XmlError handle(final HttpServletRequest request) {
        log(request, null);
        return XmlError.unknownStructure();
    }

    // in order to handle html request errors we have to return a String here
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public String handleException(final HttpServletRequest request, final Exception exception) {
        log(request, exception);

        final StringWriter writer = new StringWriter();
        try {
            JAXBContext.newInstance(XmlError.class).createMarshaller().marshal(XmlError.unknownStructure(), writer);
        } catch (final JAXBException e) {
            LOGGER.log(Level.SEVERE, "xmlParse error: ", e);
        }
        return writer.toString();
    }
}
