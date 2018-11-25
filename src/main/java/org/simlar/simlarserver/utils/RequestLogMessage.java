/*
 * Copyright (C) 2018 The Simlar Authors.
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

package org.simlar.simlarserver.utils;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.AllArgsConstructor;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;

@AllArgsConstructor
public final class RequestLogMessage {
    private final HttpServletRequest request;
    private final boolean logUrl;

    public RequestLogMessage(final HttpServletRequest request) {
        this(request, true);
    }

    @SuppressFBWarnings("SERVLET_HEADER_USER_AGENT")
    @Override
    public String toString() {
        if (request == null) {
            return "no request object";
        }

        return (logUrl
                ? "URL='" + request.getRequestURL() + "' "
                : "")
                + "IP='" + request.getRemoteAddr() + "' User-Agent='" + request.getHeader("User-Agent") + "' parameters='" + serializeParameters(request) + '\'';
    }

    private static String serializeParameters(final ServletRequest request) {
        return request.getParameterMap().entrySet().stream()
                .map(e -> e.getKey() + "=\"" + String.join(" ", e.getValue()) + '"')
                .collect(Collectors.joining(", "));
    }
}
