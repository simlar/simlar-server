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

package org.simlar.simlarserver.data;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings("SerializableDeserializableClassInSecureContext")
public enum DeviceType {
    ANDROID(1),
    IOS(2),
    IOS_DEVELOPMENT(3),
    IOS_VOIP(4),
    IOS_VOIP_DEVELOPMENT(5);

    private static final Map<Integer, DeviceType> INTEGER_DEVICE_TYPE_MAP =
            Arrays.stream(DeviceType.values()).collect(Collectors.toMap(DeviceType::toInt, type -> type));

    private final int id;

    DeviceType(final int id) {
        this.id = id;
    }

    public static DeviceType fromInt(final int id) {
        return INTEGER_DEVICE_TYPE_MAP.get(id);
    }

    public int toInt() {
        return id;
    }

    public boolean isIos() {
        return this != ANDROID;
    }
}
