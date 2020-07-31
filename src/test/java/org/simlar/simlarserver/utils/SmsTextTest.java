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
    @SuppressWarnings("ObjectAllocationInLoop")
    @Test
    public void testFormat() {
        for (int i = 0; i < 10; ++i) {
            final String code = Password.generateRegistrationCode();

            assertEquals("Welcome to Simlar! When the app asks for a registration code, use: " + code + '.', SmsText.EN.format(code));
            assertEquals("Willkommen bei Simlar! Wenn die App bei der Anmeldung nach einem Code fragt, benutze: " + code + '.', SmsText.DE.format(code));
        }
    }

    @Test
    public void testFromStringEmpty() {
        assertEquals(SmsText.EN, SmsText.fromString(null));
        assertEquals(SmsText.EN, SmsText.fromString(""));
    }

    @Test
    public void testFromStringBasic() {
        assertEquals(SmsText.EN, SmsText.fromString("IOS_EN"));
        assertEquals(SmsText.DE, SmsText.fromString("ANDROID_DE"));
        assertEquals(SmsText.EN, SmsText.fromString("ANDROID_EN"));
        assertEquals(SmsText.EN, SmsText.fromString("ios_en"));
        assertEquals(SmsText.EN, SmsText.fromString("iOS_En"));
        assertEquals(SmsText.DE, SmsText.fromString("android_de"));
        assertEquals(SmsText.EN, SmsText.fromString("android_en"));
    }

    @Test
    public void testFromStringEnglish() {
        assertEquals(SmsText.EN, SmsText.fromString("Welcome to Simlar! When the app asks for a registration code, use: *CODE*."));
        assertEquals(SmsText.EN, SmsText.fromString("Welcome to Simlar! If the app asks for a registration code, use: *CODE*. Otherwise you do not need this SMS."));
        assertEquals(SmsText.EN, SmsText.fromString("Welcome to Simlar! If the app asks for a registration code, use: *CODE*. Otherwise you don't need this SMS."));
        assertEquals(SmsText.EN, SmsText.fromString("Simlar Registration Code:"));
    }

    @Test
    public void testFromStringGerman() {
        assertEquals(SmsText.DE, SmsText.fromString("Willkommen bei Simlar! Falls die App bei der Anmeldung nach einem Code fragt, benutze: *CODE*. Sonst brauchst du diese SMS nicht."));
        assertEquals(SmsText.DE, SmsText.fromString("Willkommen bei Simlar! Falls die App bei der Anmeldung nach einem Code fragt, benutze: *CODE*. Sonst benötigst du diese SMS nicht."));
    }

    @Test
    public void testFromStringNotExactlyEqual() {
        assertEquals(SmsText.EN, SmsText.fromString(" Simlar Registration Code: "));
        assertEquals(SmsText.EN, SmsText.fromString("xxxSimlarXXXRegistrationXXXCode:XXX"));
        assertEquals(SmsText.EN, SmsText.fromString("XXXWelcome to Simlar! When!the app asks<>for a registration code, use: *CODE*.<>??0"));
    }
}
