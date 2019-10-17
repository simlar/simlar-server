package org.simlar.simlarserver.services.createaccountservice;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
final class RegionalSettings {
    private final String regionCode;
    private final int maxRequestsPerHour;
}
