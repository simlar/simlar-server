package org.simlar.simlarserver.services.createaccountservice;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@SuppressWarnings("unused")
@AllArgsConstructor
@Getter
@ConstructorBinding
@ConfigurationProperties
final class TestAccount {
    private final String simlarId;
    private final String registrationCode;
}
