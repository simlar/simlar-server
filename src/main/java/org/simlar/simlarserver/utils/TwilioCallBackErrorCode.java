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

import javax.annotation.Nullable;
import java.util.Map;

@SuppressWarnings("UtilityClass")
public final class TwilioCallBackErrorCode {
    @SuppressWarnings("StaticCollection")
    private static final Map<String, String> KNOWN_ERROR_CODES = Map.of(
            "30001", "Queue overflow",
            "30002", "Account suspended",
            "30003", "Unreachable destination handset",
            "30004", "Message blocked",
            "30005", "Unknown destination handset",
            "30006", "Landline or unreachable carrier",
            "30007", "Carrier violation",
            "30008", "Unknown error",
            "30009", "Missing segment",
            "30010", "Message price exceeds max price");

    private TwilioCallBackErrorCode() {
        throw new AssertionError("This class was not meant to be instantiated");
    }

    @Nullable
    public static String createString(final String errorCode) {
        if (errorCode == null) {
            return null;
        }

        final String knownErrorCode = KNOWN_ERROR_CODES.get(errorCode);
        return knownErrorCode != null
                ? errorCode + " - " + knownErrorCode
                : errorCode;
    }
}
