/*
 * Copyright (C) 2019 The Simlar Authors.
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

package org.simlar.simlarserver.services.pushnotification;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.simlar.simlarserver.data.DeviceType;
import org.simlar.simlarserver.database.models.PushNotification;
import org.simlar.simlarserver.database.repositories.PushNotificationsRepository;
import org.simlar.simlarserver.services.pushnotification.apple.ApplePushNotificationService;
import org.simlar.simlarserver.services.pushnotification.apple.ApplePushServer;
import org.simlar.simlarserver.services.pushnotification.google.GooglePushNotificationService;
import org.simlar.simlarserver.utils.SimlarId;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;

@AllArgsConstructor
@Slf4j
@Component
public final class PushNotificationService {
    private final PushNotificationsRepository pushNotificationsRepository;
    private final ApplePushNotificationService applePushNotificationService;
    private final GooglePushNotificationService googlePushNotificationService;

    @Nullable
    public String sendPushNotification(final SimlarId simlarId) {
        if (simlarId == null) {
            return null;
        }

        final PushNotification pushNotification = pushNotificationsRepository.findBySimlarId(simlarId.get());
        final DeviceType deviceType = pushNotification == null ? null : pushNotification.getDeviceType();
        if (deviceType == null) {
            log.error("no device type found for simlarId '{}'", simlarId);
            return null;
        }

        //noinspection SwitchStatement
        switch (deviceType) {
            case ANDROID:
                return googlePushNotificationService.requestPushNotification(pushNotification.getPushId());
            case IOS_VOIP:
                return applePushNotificationService.requestVoipPushNotification(ApplePushServer.PRODUCTION, null, pushNotification.getPushId());
            case IOS_VOIP_DEVELOPMENT:
                return applePushNotificationService.requestVoipPushNotification(ApplePushServer.SANDBOX, null, pushNotification.getPushId());
            case IOS:
            case IOS_DEVELOPMENT:
            default:
                log.error("unsupported device type '{}'", deviceType);
                return null;
        }
    }
}
