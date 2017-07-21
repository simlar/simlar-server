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

import java.security.SecureRandom;

@SuppressWarnings("UtilityClass")
public final class Password {
    private static final int DEFAULT_LENGTH = 12;
    @SuppressWarnings("SpellCheckingInspection")
    private static final String ALPHABET = "abcdefghjkmnpqrstuvwxyzABCDEFGHJKMNPQRSTUVWXY23456789 _-+#*?$()[]<>/%";

    private Password() {
        throw new AssertionError("This class was not meant to be instantiated");
    }

    public static String generate() {
        return generate(DEFAULT_LENGTH);
    }

    static String generate(final int length) {
        return new SecureRandom().ints(length, 0, ALPHABET.length())
                .mapToObj(ALPHABET::charAt)
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
    }

    public static String generateRegistrationCode() {
        return new SecureRandom().ints(6, 0, 10)
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
    }
}
