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

package org.simlar.simlarserver.services.createaccountservice;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "create.account")
public final class CreateAccountSettingsService {
    @Value("${alertSmsNumbers:}")
    private String[] alertSmsNumbers;

    @Value("${maxRequestsPerSimlarIdPerDay:10}")
    private int maxRequestsPerSimlarIdPerDay;

    @Value("${maxRequestsPerIpPerHour:60}")
    private int maxRequestsPerIpPerHour;

    @Value("${maxRequestsTotalPerHour:220}")
    private int maxRequestsTotalPerHour;

    @Value("${maxRequestsTotalPerDay:1440}")
    private int maxRequestsTotalPerDay;

    @Value("${maxConfirms:10}")
    private int maxConfirms;

    @Value("${maxCalls:3}")
    private int maxCalls;

    @Value("${callDelaySecondsMin:90}")
    private int callDelaySecondsMin;

    @Value("${callDelaySecondsMax:600}")
    private int callDelaySecondsMax;
}
