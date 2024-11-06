package org.simlar.simlarserver.services.accountservice;

import org.springframework.boot.context.properties.ConfigurationProperties;

@SuppressWarnings("unused")
@ConfigurationProperties
public record RegionalSettings(
        String regionCode,
        int maxRequestsPerHour) {
}
