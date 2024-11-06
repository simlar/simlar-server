/*
 * Copyright (C) 2017 The Simlar Authors.
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

package org.simlar.simlarserver.services.accountservice;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.simlar.simlarserver.database.models.AccountCreationRequestCount;
import org.simlar.simlarserver.database.repositories.AccountCreationRequestCountRepository;
import org.simlar.simlarserver.database.repositories.PushNotificationsRepository;
import org.simlar.simlarserver.services.smsservice.SmsService;
import org.simlar.simlarserver.services.subscriberservice.SubscriberService;
import org.simlar.simlarserver.utils.CallText;
import org.simlar.simlarserver.utils.LibPhoneNumber;
import org.simlar.simlarserver.utils.Password;
import org.simlar.simlarserver.utils.SimlarId;
import org.simlar.simlarserver.utils.SmsText;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorCallNotAllowedAtTheMomentException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorFailedToSendSmsException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorFailedToTriggerCallException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorInvalidTelephoneNumberException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorNoIpException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorNoRegistrationCodeException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorNoSimlarIdException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorTooManyConfirmTriesException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorTooManyRequestTriesException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorWrongCredentialsException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorWrongRegistrationCodeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.regex.Pattern;

@SuppressWarnings("ClassWithTooManyDependencies")
@Slf4j
@Component
public final class AccountService {
    private static final Duration WARN_TIMEOUT = Duration.ofSeconds(180);
    private static final Pattern REGEX_REGISTRATION_CODE = Pattern.compile("\\d{" + Password.REGISTRATION_CODE_LENGTH + '}');

    private final SmsService smsService;
    private final CreateAccountSettings createAccountSettings;
    private final AccountCreationRequestCountRepository accountCreationRepository;
    private final SubscriberService subscriberService;
    private final TransactionTemplate transactionTemplate;
    private final TaskScheduler taskScheduler;
    private final PushNotificationsRepository pushNotificationsRepository;

    @SuppressWarnings("ConstructorWithTooManyParameters")
    @Autowired // fix IntelliJ inspection warning unused
    private AccountService(final SmsService smsService, final CreateAccountSettings createAccountSettings, final AccountCreationRequestCountRepository accountCreationRepository, final SubscriberService subscriberService, final PlatformTransactionManager transactionManager, final TaskScheduler taskScheduler, final PushNotificationsRepository pushNotificationsRepository) {
        this.smsService = smsService;
        this.createAccountSettings = createAccountSettings;
        this.accountCreationRepository = accountCreationRepository;
        this.subscriberService = subscriberService;
        transactionTemplate = new TransactionTemplate(transactionManager);
        this.taskScheduler = taskScheduler;
        this.pushNotificationsRepository = pushNotificationsRepository;

        for (final RegionalSettings regional : createAccountSettings.getRegionalSettings()) {
            log.info("regional setting region code '{}' with max requests per hour '{}'", regional.regionCode(), regional.maxRequestsPerHour());
        }

        log.info("alert sms numbers '{}'", String.join(", ", createAccountSettings.getAlertSmsNumbers()));
    }

    public AccountRequest createAccountRequest(final String telephoneNumber, final String smsText, final String ip) {
        return createAccountRequest(telephoneNumber, smsText, ip, Instant.now());
    }

    AccountRequest createAccountRequest(final String telephoneNumber, final String smsText, final String ip, final Instant now) {
        return createAccountRequest(createValidatedSimlarId(telephoneNumber), telephoneNumber, smsText, ip, now);
    }

    AccountRequest createAccountRequest(final SimlarId simlarId, final String telephoneNumber, final String smsText, final String ip, final Instant now) {
        if (StringUtils.isEmpty(ip)) {
            throw new XmlErrorNoIpException("request account creation with empty ip for telephone number:  " + telephoneNumber);
        }

        final Instant anHourAgo = now.minus(Duration.ofHours(1));
        checkRequestTriesLimit(accountCreationRepository.sumRequestTries(ip, anHourAgo), createAccountSettings.getMaxRequestsPerIpPerHour(),
                String.format("too many create account requests for ip '%s' ", ip));

        checkRequestTriesLimitWithAlert(accountCreationRepository.sumRequestTries(anHourAgo), createAccountSettings.getMaxRequestsTotalPerHour(),
                "too many total create account requests within one hour");

        checkRequestTriesLimitWithAlert(accountCreationRepository.sumRequestTries(now.minus(Duration.ofDays(1))),
                createAccountSettings.getMaxRequestsTotalPerDay(),
                "too many total create account requests within one day");

        for (final RegionalSettings regional : createAccountSettings.getRegionalSettings()) {
            //noinspection ObjectAllocationInLoop
            final String regionCode = '*' + regional.regionCode();
            if (StringUtils.isNotEmpty(regionCode) && simlarId.get().startsWith(regionCode)) {
                //noinspection ObjectAllocationInLoop
                checkRequestTriesLimit(accountCreationRepository.sumRequestTriesForRegion(regionCode + '%', anHourAgo), regional.maxRequestsPerHour(),
                        String.format("too many create account requests for region '%s' ", regional.regionCode()));
            }
        }

        //noinspection LocalVariableNamingConvention
        final String testAccountRegistrationCode = searchTestAccountRegistrationCode(simlarId.get());
        final AccountCreationRequestCount dbEntry = updateRequestTries(simlarId, ip, now, testAccountRegistrationCode);
        checkRequestTriesLimit(dbEntry.getRequestTries() - 1, createAccountSettings.getMaxRequestsPerSimlarIdPerDay(),
                String.format("too many create account requests with number '%s'", telephoneNumber));

        if (Duration.between(dbEntry.getRegistrationCodeTimestamp().plus(Duration.ofMinutes(createAccountSettings.getRegistrationCodeExpirationMinutes())), now).compareTo(Duration.ZERO) > 0) {
            dbEntry.setRegistrationCode(StringUtils.isNotEmpty(testAccountRegistrationCode) ? testAccountRegistrationCode : Password.generateRegistrationCode());
            dbEntry.setRegistrationCodeTimestamp(now);
            dbEntry.setConfirmTries(0);
        }

        if (StringUtils.isEmpty(testAccountRegistrationCode)) {
            final String smsMessage = SmsText.create(smsText, dbEntry.getRegistrationCode());
            if (!smsService.sendSms(telephoneNumber, smsMessage)) {
                throw new XmlErrorFailedToSendSmsException("failed to send sms to '" + telephoneNumber + "' with text: " + smsMessage);
            }
        }

        dbEntry.setPassword(Password.generate());
        accountCreationRepository.save(dbEntry);

        taskScheduler.schedule(() -> {
            if (!subscriberService.checkCredentials(dbEntry.getSimlarId(), dbEntry.getPassword())) {
                log.warn("no confirmation after '{}' for number '{}'", WARN_TIMEOUT, telephoneNumber);
            }
        }, now.plus(WARN_TIMEOUT));

        log.info("created account request for simlarId '{}'", simlarId);
        return new AccountRequest(simlarId, dbEntry.getPassword());
    }

    private String searchTestAccountRegistrationCode(final CharSequence simlarId) {
        for (final TestAccount testAccount : createAccountSettings.getTestAccounts()) {
            if (StringUtils.equals(simlarId, testAccount.simlarId())) {
                return testAccount.registrationCode();
            }
        }

        return null;
    }

    private static SimlarId createValidatedSimlarId(final String telephoneNumber) {
        final SimlarId simlarId = SimlarId.createWithTelephoneNumber(telephoneNumber);
        if (simlarId == null) {
            throw new XmlErrorInvalidTelephoneNumberException("invalid telephone number: " + telephoneNumber);
        }

        if (!LibPhoneNumber.isValid(telephoneNumber)) {
            throw new XmlErrorInvalidTelephoneNumberException("libphonenumber invalidates telephone number: " + telephoneNumber);
        }

        return simlarId;
    }

    private AccountCreationRequestCount updateRequestTries(final SimlarId simlarId, final String ip, final Instant now, final String registrationCode) {
        return transactionTemplate.execute(status -> {
            final AccountCreationRequestCount dbEntry = accountCreationRepository.findBySimlarIdForUpdate(simlarId.get());
            if (dbEntry == null) {
                return accountCreationRepository.save(new AccountCreationRequestCount(
                        simlarId,
                        Password.generate(),
                        StringUtils.isNotEmpty(registrationCode) ? registrationCode : Password.generateRegistrationCode(),
                        now,
                        ip));
            }

            final Instant savedTimestamp = dbEntry.getTimestamp();
            if (savedTimestamp != null && Duration.between(savedTimestamp.plus(Duration.ofDays(1)), now).compareTo(Duration.ZERO) > 0) {
                dbEntry.setRequestTries(1);
            } else {
                dbEntry.incrementRequestTries();
            }
            dbEntry.setTimestamp(now);
            dbEntry.setIp(ip);
            return accountCreationRepository.save(dbEntry);
        });
    }

    @SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
    private static void checkRequestTriesLimit(final Integer requests, final int limit, final String message) {
        final int requestTries = ObjectUtils.defaultIfNull(requests, 0);
        if (requestTries >= limit) {
            throw new XmlErrorTooManyRequestTriesException(String.format("%s %d <= %d", message, requestTries, limit));
        }
    }

    @SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
    private void checkRequestTriesLimitWithAlert(final Integer requests, final int limit, final String message) {
        final int requestTries = ObjectUtils.defaultIfNull(requests, 0);
        if (requestTries == limit / 2) {
            final String smsMessage = String.format("50%% Alert for %s %d", message, requestTries);
            log.warn(smsMessage);
            for (final String alertNumber : createAccountSettings.getAlertSmsNumbers()) {
                smsService.sendSms(alertNumber, smsMessage);
            }
        } else {
            checkRequestTriesLimit(requestTries, limit, message);
        }
    }

    private AccountCreationRequestCount updateCalls(final SimlarId simlarId,
                                                    @SuppressWarnings("TypeMayBeWeakened") final String password,
                                                    @SuppressWarnings("TypeMayBeWeakened") final Instant now) {
        return transactionTemplate.execute(status -> {
            final AccountCreationRequestCount dbEntry = accountCreationRepository.findBySimlarIdForUpdate(simlarId.get());
            if (dbEntry == null || dbEntry.getTimestamp() == null) {
                throw new XmlErrorWrongCredentialsException("no sms request found for simlarId: " + simlarId);
            }
            if (StringUtils.isEmpty(password) || !Objects.equals(dbEntry.getPassword(), password)) {
                throw new XmlErrorWrongCredentialsException("call request with wrong password for simlarId: " + simlarId);
            }

            final long secondsSinceRequest = Duration.between(dbEntry.getTimestamp(), now).getSeconds();
            if (secondsSinceRequest < createAccountSettings.getCallDelaySecondsMin()) {
                throw new XmlErrorCallNotAllowedAtTheMomentException("aborting call to " + simlarId + " because not enough time elapsed since request: " + secondsSinceRequest + 's');
            }
            if (secondsSinceRequest > createAccountSettings.getCallDelaySecondsMax()) {
                throw new XmlErrorCallNotAllowedAtTheMomentException("aborting call to " + simlarId + " because too much time elapsed since request: " + secondsSinceRequest + 's');
            }

            final Instant callTimeStamp = dbEntry.getCallTimestamp();
            if (callTimeStamp == null || Duration.between(callTimeStamp.plus(Duration.ofDays(1)), now).compareTo(Duration.ZERO) > 0) {
                dbEntry.setCalls(1);
            } else {
                dbEntry.incrementCalls();
            }

            return accountCreationRepository.save(dbEntry);
        });
    }

    public SimlarId call(final String telephoneNumber, final String password) {
        return call(telephoneNumber, password, Instant.now());
    }

    SimlarId call(final String telephoneNumber, final String password, final Instant now) {
        final SimlarId simlarId = createValidatedSimlarId(telephoneNumber);

        final AccountCreationRequestCount dbEntry = updateCalls(simlarId, password, now);

        if (dbEntry.getCalls() > createAccountSettings.getMaxCalls()) {
            throw new XmlErrorCallNotAllowedAtTheMomentException("aborting call to " + simlarId + " because too many calls within the last 24 hours");
        }

        if (!smsService.call(telephoneNumber, CallText.format(dbEntry.getRegistrationCode()))) {
            throw new XmlErrorFailedToTriggerCallException("failed to trigger call for simlarId: " + simlarId);
        }

        dbEntry.setCallTimestamp(now);
        accountCreationRepository.save(dbEntry);

        return simlarId;
    }

    public void confirmAccount(final String simlarIdString, @SuppressWarnings("TypeMayBeWeakened") final String registrationCode) {
        final SimlarId simlarId = SimlarId.create(simlarIdString);
        if (simlarId == null) {
            throw new XmlErrorNoSimlarIdException("confirm account request with simlarId: " + simlarIdString);
        }

        if (!checkRegistrationCodeFormat(registrationCode)) {
            throw new XmlErrorNoRegistrationCodeException("confirm account request with simlarId: " + simlarId + " and registrationCode: " + registrationCode);
        }

        final AccountCreationRequestCount creationRequest = transactionTemplate.execute(status -> {
            final AccountCreationRequestCount dbEntry = accountCreationRepository.findBySimlarIdForUpdate(simlarId.get());
            if (dbEntry == null) {
                return null;
            }

            dbEntry.incrementConfirmTries();
            return accountCreationRepository.save(dbEntry);
        });

        if (creationRequest == null) {
            throw new XmlErrorNoSimlarIdException("confirm account request with no creation request in db for simlarId: " + simlarId);
        }

        final int confirmTries = creationRequest.getConfirmTries();
        if (confirmTries > createAccountSettings.getMaxConfirms()) {
            throw new XmlErrorTooManyConfirmTriesException("Too many confirm tries(" + confirmTries + ") for simlarId: " + simlarId);
        }

        if (!Objects.equals(creationRequest.getRegistrationCode(), registrationCode)) {
            throw new XmlErrorWrongRegistrationCodeException("confirm account request with wrong registration code: " + registrationCode + " for simlarId: " + simlarId);
        }

        subscriberService.save(simlarId, creationRequest.getPassword());

        log.info("confirmed account with simlarId '{}'", simlarId);
    }

    private static boolean checkRegistrationCodeFormat(final CharSequence input) {
        return input != null && REGEX_REGISTRATION_CODE.matcher(input).matches();
    }

    public void deleteAccount(final SimlarId simlarId) {
        if (simlarId == null) {
            throw new IllegalArgumentException("simlarId=" + null);
        }

        subscriberService.deleteBySimlarId(simlarId);
        pushNotificationsRepository.deleteBySimlarId(simlarId.get());
        log.info("deleted account with simlarId '{}'", simlarId);
    }
}
