/*
 * Copyright (C) 2016 The Simlar Authors.
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
import org.springframework.util.StringUtils;

public final class ApplePushId {

    private final String pushId;

    private ApplePushId(final String pushId) {
        this.pushId = pushId;
    }

    public static ApplePushId create(final String pushId) {
        if (!check(pushId)) {
            return null;
        }

        return new ApplePushId(pushId);
    }

    public String get() {
        return pushId;
    }

    @SuppressFBWarnings("OPM_OVERLY_PERMISSIVE_METHOD") // false positive
    public static boolean check(final String str) {
        return StringUtils.hasText(str) && str.matches("^[0-9a-fA-F]{64}$");
    }
}
