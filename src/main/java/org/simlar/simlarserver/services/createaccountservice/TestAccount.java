package org.simlar.simlarserver.services.createaccountservice;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@SuppressWarnings("unused")
@AllArgsConstructor
@Getter
@ConfigurationProperties
final class TestAccount {
    private final String simlarId;
    private final String registrationCode;
}
