package org.simlar.simlarserver.services.createaccountservice;

import org.springframework.boot.context.properties.ConfigurationProperties;

@SuppressWarnings("unused")
@ConfigurationProperties
public record TestAccount(
        String simlarId,
        String registrationCode) {
}
