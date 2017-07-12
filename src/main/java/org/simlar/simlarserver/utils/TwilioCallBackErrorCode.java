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

package org.simlar.simlarserver.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("UtilityClass")
public final class TwilioCallBackErrorCode {
    private static final Map<String, String> KNOWN_ERROR_CODES = initKnownErrorCodes();

    private static Map<String, String> initKnownErrorCodes() {
        final Map<String, String> knownErrorCodes = new HashMap<>(10);
        knownErrorCodes.put("30001", "Queue overflow");
        knownErrorCodes.put("30002", "Account suspended");
        knownErrorCodes.put("30003", "Unreachable destination handset");
        knownErrorCodes.put("30004", "Message blocked");
        knownErrorCodes.put("30005", "Unknown destination handset");
        knownErrorCodes.put("30006", "Landline or unreachable carrier");
        knownErrorCodes.put("30007", "Carrier violation");
        knownErrorCodes.put("30008", "Unknown error");
        knownErrorCodes.put("30009", "Missing segment");
        knownErrorCodes.put("30010", "Message price exceeds max price");
        return Collections.unmodifiableMap(knownErrorCodes);
    }

    private TwilioCallBackErrorCode() {
        throw new AssertionError("This class was not meant to be instantiated");
    }

    public static String createString(final String errorCode) {
        return KNOWN_ERROR_CODES.containsKey(errorCode)
                ? errorCode + " - " + KNOWN_ERROR_CODES.get(errorCode)
                : errorCode;
    }
}
