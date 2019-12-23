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

package org.simlar.simlarserver.services.twilio;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@AllArgsConstructor
@Getter
@ToString
@ConstructorBinding
@ConfigurationProperties(prefix = "twilio")
final class TwilioSettings {
    private final String smsSourceNumber;
    private final String sid;
    private final String authToken;
    private final String callbackUser;
    private final String callbackPassword;

    public boolean isConfigured() {
        return StringUtils.isNoneEmpty(
                smsSourceNumber,
                sid,
                authToken,
                callbackUser,
                callbackPassword);
    }

    public String getUrl() {
        return String.format("https://api.twilio.com/2010-04-01/Accounts/%s/", sid);
    }
}
