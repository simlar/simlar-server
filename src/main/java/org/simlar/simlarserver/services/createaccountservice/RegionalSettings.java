package org.simlar.simlarserver.services.createaccountservice;

import lombok.Getter;
import lombok.Setter;

/**
 * TODO: Once spring boot 2.2 releases, use constructor binding and make this class immutable.
 */
@Setter
@Getter
final class RegionalSettings {
    private String regionCode;
    private int maxRequestsPerHour;
}
