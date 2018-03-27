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
 */

package org.simlar.simlarserver.utils;

@SuppressWarnings("UtilityClass")
public final class CallText {

    private CallText() {
        throw new AssertionError("This class was not meant to be instantiated");
    }

    public static String format(final String registrationCode) {
        return "Welcome to Simlar! Your registration code is: " + formatRegistrationCodeForCall(registrationCode);
    }

    static String formatRegistrationCodeForCall(final String code) {
        return String.format("%s %s %s .. %s %s %s", code.charAt(0), code.charAt(1), code.charAt(2), code.charAt(3), code.charAt(4), code.charAt(5));
    }
}
