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

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * TODO: Once spring boot 2.2 releases, use constructor binding and make this class immutable.
 */
@Getter
@Setter
@ToString
@Component
@ConfigurationProperties(prefix = "push")
class PushNotificationSettingsService {
    private String appleVoipCertificatePath;
    private String appleVoipCertificatePassword;
    private String appleVoipCertificatePinning;
    private String applePushProtocol;
    private String appleVoipTestDeviceToken;

    public final boolean isConfigured() {
        return StringUtils.isNotEmpty(appleVoipCertificatePath) &&
                StringUtils.isNotEmpty(appleVoipCertificatePassword) &&
                StringUtils.isNotEmpty(applePushProtocol);
    }
}
