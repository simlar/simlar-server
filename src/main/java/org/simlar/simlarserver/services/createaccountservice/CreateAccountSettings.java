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
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.Collections;
import java.util.List;

@SuppressWarnings("ClassWithTooManyFields")
@Getter
@ConfigurationProperties(prefix = "create.account")
public final class CreateAccountSettings {
    private final List<String> alertSmsNumbers;

    private final int maxRequestsPerSimlarIdPerDay;

    private final int maxRequestsPerIpPerHour;

    private final int maxRequestsTotalPerHour;

    private final int maxRequestsTotalPerDay;

    private final int maxConfirms;

    private final int maxCalls;

    private final int callDelaySecondsMin;

    private final int callDelaySecondsMax;

    @SuppressWarnings("FieldNamingConvention")
    private final int registrationCodeExpirationMinutes;

    private final List<RegionalSettings> regionalSettings;

    private final List<TestAccount> testAccounts;


    @SuppressWarnings({"ConstructorWithTooManyParameters", "unused"})
    @ConstructorBinding
    public CreateAccountSettings(
            @DefaultValue("") final String[] alertSmsNumbers,
            @DefaultValue("10") final int maxRequestsPerSimlarIdPerDay,
            @DefaultValue("60") final int maxRequestsPerIpPerHour,
            @DefaultValue("220") final int maxRequestsTotalPerHour,
            @DefaultValue("1440") final int maxRequestsTotalPerDay,
            @DefaultValue("10") final int maxConfirms,
            @DefaultValue("3") final int maxCalls,
            @DefaultValue("90") final int callDelaySecondsMin,
            @DefaultValue("600") final int callDelaySecondsMax,
            @SuppressWarnings("MethodParameterNamingConvention")
            @DefaultValue("15") final int registrationCodeExpirationMinutes,
            final List<RegionalSettings> regionalSettings,
            final List<TestAccount> testAccounts) {
        this.alertSmsNumbers = List.of(alertSmsNumbers);
        this.maxRequestsPerSimlarIdPerDay = maxRequestsPerSimlarIdPerDay;
        this.maxRequestsPerIpPerHour = maxRequestsPerIpPerHour;
        this.maxRequestsTotalPerHour = maxRequestsTotalPerHour;
        this.maxRequestsTotalPerDay = maxRequestsTotalPerDay;
        this.maxConfirms = maxConfirms;
        this.maxCalls = maxCalls;
        this.callDelaySecondsMin = callDelaySecondsMin;
        this.callDelaySecondsMax = callDelaySecondsMax;
        this.registrationCodeExpirationMinutes = registrationCodeExpirationMinutes;
        this.regionalSettings = regionalSettings == null ? Collections.emptyList() : Collections.unmodifiableList(regionalSettings);
        this.testAccounts = testAccounts == null ? Collections.emptyList() : Collections.unmodifiableList(testAccounts);
    }
}
