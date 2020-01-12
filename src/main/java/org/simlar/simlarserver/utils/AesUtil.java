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
 */

package org.simlar.simlarserver.utils;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.annotation.Nullable;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;

@SuppressWarnings("UtilityClass")
public final class AesUtil {
    private AesUtil() {
        throw new AssertionError("This class was not meant to be instantiated");
    }

    public static String generateInitializationVector() {
        return RandomStringUtils.randomAlphanumeric(16);
    }

    private static byte[] aes(final int mode, final byte[] message, final String initializationVector, final String password) {
        try {
            final AlgorithmParameterSpec ivParameterSpec = new IvParameterSpec(initializationVector.getBytes(StandardCharsets.UTF_8));
            final Key secretKeySpec = new SecretKeySpec(password.getBytes(StandardCharsets.UTF_8), "AES");

            final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(mode, secretKeySpec, ivParameterSpec);
            return cipher.doFinal(message);
        } catch (final NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
            throw new AesUtilException(ExceptionUtils.getRootCauseMessage(e), e);
        }
    }

    @Nullable
    public static String encrypt(final String message, final String initializationVector, final String password){
        return Base64.encodeBase64String(aes(Cipher.ENCRYPT_MODE, message.getBytes(StandardCharsets.UTF_8), initializationVector, password));
    }

    public static String decrypt(final String message, final String initializationVector, final String password){
        return new String(aes(Cipher.DECRYPT_MODE, Base64.decodeBase64(message), initializationVector, password), StandardCharsets.UTF_8);
    }
}
