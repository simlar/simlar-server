package org.simlar.simlarserver.services.createaccountservice;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
final class RegionalSettings {
    private String regionCode;
    private int maxRequestsPerHour;
}
