/*
 * Copyright (C) 2020 The Simlar Authors.
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

public final class AesUtilTest {
    @Test
    public void testGenerateInitializationVector() {
        assertEquals(16, AesUtil.generateInitializationVector().length());
    }

    @Test (expected = AesUtilException.class)
    public void testInitializationVectorToShort() {
        AesUtil.encrypt("*491761234567*", "", "ab977b3a12eff330413a17c263ae3115");
    }

    @Test (expected = AesUtilException.class)
    public void testPasswordToShort() {
        AesUtil.decrypt("*491761234567*", "1234567890123456", "12");
    }

    @SuppressWarnings("SpellCheckingInspection")
    @Test
    public void testEncrypt() {
        assertEquals("+w50s3gTRjT0Qiv/QxlTLQ==", AesUtil.encrypt("*491761234567*", "1234567890123456", "ab977b3a12eff330413a17c263ae3115"));
        assertEquals("H8sDzqzLjhIRdenAYZsx5Q==", AesUtil.encrypt("*0*", "1234567890123456", "somePasswordHash"));
        assertEquals("uKSO2MwGWDrloOhLwPa50g==", AesUtil.encrypt("*491761234567*", "1234567890123456", "somePasswordHash"));
    }

    @SuppressWarnings("SpellCheckingInspection")
    @Test
    public void testDecrypt() {
        assertEquals("*491761234567*", AesUtil.decrypt("+w50s3gTRjT0Qiv/QxlTLQ==", "1234567890123456", "ab977b3a12eff330413a17c263ae3115"));
        assertEquals("*0*", AesUtil.decrypt("H8sDzqzLjhIRdenAYZsx5Q==", "1234567890123456", "somePasswordHash"));
        assertEquals("*491761234567*", AesUtil.decrypt("uKSO2MwGWDrloOhLwPa50g==", "1234567890123456", "somePasswordHash"));
    }

    @Test
    public void testEnAndDecrypt() {
        final String initializationVector = AesUtil.generateInitializationVector();
        final String password = "pASSword12345678";
        final String message = "some message";

        final String encrypted = AesUtil.encrypt(message, initializationVector, password);
        assertEquals(message, AesUtil.decrypt(encrypted, initializationVector, password));
    }
}
