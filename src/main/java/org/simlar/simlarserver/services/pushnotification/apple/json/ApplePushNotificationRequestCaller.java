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

package org.simlar.simlarserver.services.pushnotification.apple.json;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.simlar.simlarserver.utils.AesUtil;

@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
public final class ApplePushNotificationRequestCaller {
    private final String initializationVector;
    private final String encryptedSimlarId;

    public static ApplePushNotificationRequestCaller create(final String callerSimlarId, final String calleePasswordHash) {
        final String initializationVector = AesUtil.generateInitializationVector();
        return new ApplePushNotificationRequestCaller(initializationVector, AesUtil.encrypt(callerSimlarId, initializationVector, calleePasswordHash));
    }
}
