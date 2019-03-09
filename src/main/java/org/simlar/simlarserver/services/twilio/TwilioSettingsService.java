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

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@ToString
@Component
@ConfigurationProperties(prefix = "twilio")
class TwilioSettingsService {
    @Value("${url:api.twilio.com/2010-04-01/Accounts}")
    private String url;
    private String smsSourceNumber;
    private String sid;
    private String authToken;
    private String callbackUser;
    private String callbackPassword;

    public final boolean isConfigured() {
        return StringUtils.isNotEmpty(url) &&
                StringUtils.isNotEmpty(smsSourceNumber) &&
                StringUtils.isNotEmpty(sid) &&
                StringUtils.isNotEmpty(authToken) &&
                StringUtils.isNotEmpty(callbackUser) &&
                StringUtils.isNotEmpty(callbackPassword);
    }

    public final String getUrl() {
        return String.format("https://%s/%s/", url, sid);
    }
}
