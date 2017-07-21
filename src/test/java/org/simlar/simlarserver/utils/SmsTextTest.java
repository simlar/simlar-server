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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public final class SmsTextTest {
    @Test
    public void testFormat() {
        for (int i = 0; i < 10; ++i) {
            final String code = Password.generateRegistrationCode();

            assertEquals("Welcome to Simlar! If the app asks for a registration code, use: " + code + ". Otherwise you do not need this SMS.", SmsText.ANDROID_EN.format(code));
            //noinspection SpellCheckingInspection
            assertEquals("Willkommen bei Simlar! Falls die App bei der Anmeldung nach einem Code fragt, benutze: " + code + ". Sonst brauchst du diese SMS nicht.", SmsText.ANDROID_DE.format(code));
            assertEquals("Welcome to Simlar! When the app asks for a registration code, use: " + code + '.', SmsText.IOS_EN.format(code));
        }
    }
}
