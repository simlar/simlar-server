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

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import java.util.Collections;
import java.util.List;

@SuppressWarnings({"ClassWithTooManyFields", "ClassWithTooManyMethods"})
@AllArgsConstructor
@Getter
@ConstructorBinding
@ConfigurationProperties(prefix = "create.account")
public final class CreateAccountSettingsService {
    @Value("${alertSmsNumbers:}")
    private final String[] alertSmsNumbers;

    @Value("${maxRequestsPerSimlarIdPerDay:10}")
    private final int maxRequestsPerSimlarIdPerDay;

    @Value("${maxRequestsPerIpPerHour:60}")
    private final int maxRequestsPerIpPerHour;

    @Value("${maxRequestsTotalPerHour:220}")
    private final int maxRequestsTotalPerHour;

    @Value("${maxRequestsTotalPerDay:1440}")
    private final int maxRequestsTotalPerDay;

    @Value("${maxConfirms:10}")
    private final int maxConfirms;

    @Value("${maxCalls:3}")
    private final int maxCalls;

    @Value("${callDelaySecondsMin:90}")
    private final int callDelaySecondsMin;

    @Value("${callDelaySecondsMax:600}")
    private final int callDelaySecondsMax;

    @SuppressWarnings("FieldNamingConvention")
    @Value("${registrationCodeExpirationMinutes:15}")
    private final int registrationCodeExpirationMinutes;

    private final List<RegionalSettings> regionalSettings;

    public List<String> getAlertSmsNumbers() {
        return List.of(alertSmsNumbers);
    }

    public List<RegionalSettings> getRegionalSettings() {
        return regionalSettings == null ? Collections.emptyList() : Collections.unmodifiableList(regionalSettings);
    }
}
