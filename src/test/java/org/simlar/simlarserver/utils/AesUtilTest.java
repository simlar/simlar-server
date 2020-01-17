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

import org.apache.commons.codec.binary.Base64;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public final class AesUtilTest {
    @Test
    public void testGenerateInitializationVector() {
        assertEquals(16, Base64.decodeBase64(AesUtil.generateInitializationVector()).length);
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
        assertEquals("S5UdMzzRg28+ldRVlNOYboTGoEKGUmfTHMOV3lUz", AesUtil.encrypt("*491761234567*", "s1i9Y10Y1GH3SyiqzDJt4A==", "ab977b3a12eff330413a17c263ae3115"));
        assertEquals("HY+HoJb4sOOMBiCrKVCw2I6ulQ==", AesUtil.encrypt("*0*", "qUsR756nX6zQEVhYPm6MoA==", "somePasswordHash"));
        assertEquals("SqPbPo379YtXNq41uyOIVQpWrg28cLPZBejCH3VI", AesUtil.encrypt("*491761234567*", "HP8Lfz+Hg4V7+tHsJwSiXA==", "somePasswordHash"));
    }

    @SuppressWarnings("SpellCheckingInspection")
    @Test
    public void testDecrypt() {
        assertEquals("*491761234567*", AesUtil.decrypt("S5UdMzzRg28+ldRVlNOYboTGoEKGUmfTHMOV3lUz", "s1i9Y10Y1GH3SyiqzDJt4A==", "ab977b3a12eff330413a17c263ae3115"));
        assertEquals("*0*", AesUtil.decrypt("HY+HoJb4sOOMBiCrKVCw2I6ulQ==", "qUsR756nX6zQEVhYPm6MoA==", "somePasswordHash"));
        assertEquals("*491761234567*", AesUtil.decrypt("SqPbPo379YtXNq41uyOIVQpWrg28cLPZBejCH3VI", "HP8Lfz+Hg4V7+tHsJwSiXA==", "somePasswordHash"));
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
