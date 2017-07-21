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

import java.util.regex.Pattern;

@SuppressWarnings("EnumeratedClassNamingConvention")
public enum SmsText {
    ANDROID_EN("Welcome to Simlar! If the app asks for a registration code, use: *CODE*. Otherwise you do not need this SMS."),
    @SuppressWarnings("SpellCheckingInspection")
    ANDROID_DE("Willkommen bei Simlar! Falls die App bei der Anmeldung nach einem Code fragt, benutze: *CODE*. Sonst brauchst du diese SMS nicht."),
    IOS_EN("Welcome to Simlar! When the app asks for a registration code, use: *CODE*.");

    private static final Pattern REGEX_PATTERN_CODE = Pattern.compile("\\*CODE\\*");

    private final String text;

    SmsText(final String text) {
        this.text = text;
    }

    String format(final String registrationCode) {
        return REGEX_PATTERN_CODE.matcher(text).replaceAll(registrationCode);
    }
}
