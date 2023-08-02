/*
 * Copyright (C) 2015 The Simlar Authors.
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

package org.simlar.simlarserver.database.models;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.simlar.simlarserver.data.DeviceType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@NoArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
@Table(name = "simlar_push_notifications")
public class PushNotification {
    @Id
    @Column(nullable = false, length = 64)
    private String simlarId;

    @Column(nullable = false, columnDefinition = "int(10) unsigned")
    private int deviceType;

    @Column(nullable = false, columnDefinition = "text")
    private String pushId;

    public PushNotification(final String simlarId, final DeviceType deviceType, final String pushId) {
        this.simlarId = simlarId;
        this.deviceType = deviceType.toInt();
        this.pushId = pushId;
    }

    public final DeviceType getDeviceType() {
        return DeviceType.fromInt(deviceType);
    }
}
