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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

@SuppressWarnings("EnumeratedClassNamingConvention")
public enum SmsText {
    ANDROID_EN(
            "Welcome to Simlar! If the app asks for a registration code, use: *CODE*. Otherwise you do not need this SMS.",
            "Welcome to Simlar! If the app asks for a registration code, use: *CODE*. Otherwise you don't need this SMS.",
            "Simlar Registration Code:"),
    @SuppressWarnings("SpellCheckingInspection")
    ANDROID_DE(
            "Willkommen bei Simlar! Falls die App bei der Anmeldung nach einem Code fragt, benutze: *CODE*. Sonst brauchst du diese SMS nicht.",
            "Willkommen bei Simlar! Falls die App bei der Anmeldung nach einem Code fragt, benutze: *CODE*. Sonst benötigst du diese SMS nicht."),
    IOS_EN("Welcome to Simlar! When the app asks for a registration code, use: *CODE*.");

    private static final Pattern REGEX_PATTERN_CODE = Pattern.compile("\\*CODE\\*");

    private final List<String> texts;

    SmsText(final String... texts) {
        this.texts = Collections.unmodifiableList(Arrays.asList(texts));
    }

    String format(final String registrationCode) {
        return REGEX_PATTERN_CODE.matcher(texts.get(0)).replaceAll(registrationCode);
    }

    @SuppressFBWarnings("DLC_DUBIOUS_LIST_COLLECTION")
    static SmsText fromString(final String input) {
        if (StringUtils.isEmpty(input)) {
            return ANDROID_EN;
        }

        for (final SmsText value: values()) {
            if (value.toString().equals(input) || value.texts.contains(input)) {
                return value;
            }
        }

        return ANDROID_EN;
    }

    public static String create(final String text, final String registrationCode) {
        return fromString(text).format(registrationCode);
    }
}
