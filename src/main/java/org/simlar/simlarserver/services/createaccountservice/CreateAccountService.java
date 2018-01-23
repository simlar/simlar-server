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

package org.simlar.simlarserver.services.createaccountservice;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.simlar.simlarserver.database.models.AccountCreationRequestCount;
import org.simlar.simlarserver.database.repositories.AccountCreationRequestCountRepository;
import org.simlar.simlarserver.services.settingsservice.SettingsService;
import org.simlar.simlarserver.services.smsservice.SmsService;
import org.simlar.simlarserver.services.subscriberservice.SubscriberService;
import org.simlar.simlarserver.utils.LibPhoneNumber;
import org.simlar.simlarserver.utils.Password;
import org.simlar.simlarserver.utils.SimlarId;
import org.simlar.simlarserver.utils.SmsText;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorFailedToSendSmsException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorInvalidTelephoneNumberException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorNoIpException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorNoRegistrationCodeException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorNoSimlarIdException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorTooManyConfirmTriesException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorTooManyRequestTriesException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorWrongRegistrationCodeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.regex.Pattern;

@Slf4j
@Component
public final class CreateAccountService {
    private static final long WARN_TIMEOUT = 180L;
    private static final Pattern REGEX_REGISTRATION_CODE = Pattern.compile("\\d{" + Password.REGISTRATION_CODE_LENGTH + '}');

    private final SmsService smsService;
    private final SettingsService settingsService;
    private final AccountCreationRequestCountRepository accountCreationRepository;
    private final SubscriberService subscriberService;
    private final TransactionTemplate transactionTemplate;
    private final TaskScheduler taskScheduler;

    @SuppressWarnings("ConstructorWithTooManyParameters")
    @Autowired
    public CreateAccountService(final SmsService smsService, final SettingsService settingsService, final AccountCreationRequestCountRepository accountCreationRepository, final SubscriberService subscriberService, final PlatformTransactionManager transactionManager, final TaskScheduler taskScheduler) {
        this.smsService = smsService;
        this.settingsService = settingsService;
        this.accountCreationRepository = accountCreationRepository;
        this.subscriberService = subscriberService;
        transactionTemplate = new TransactionTemplate(transactionManager);
        this.taskScheduler = taskScheduler;
    }

    public AccountRequest createAccountRequest(final String telephoneNumber, final String smsText, final String ip) {
        final SimlarId simlarId = SimlarId.createWithTelephoneNumber(telephoneNumber);
        if (simlarId == null) {
            throw new XmlErrorInvalidTelephoneNumberException("invalid telephone number: " + telephoneNumber);
        }

        if (!LibPhoneNumber.isValid(telephoneNumber)) {
            throw new XmlErrorInvalidTelephoneNumberException("libphonenumber invalidates telephone number: " + telephoneNumber);
        }

        if (StringUtils.isEmpty(ip)) {
            throw new XmlErrorNoIpException("request account creation with empty ip for telephone number:  " + telephoneNumber);
        }

        final AccountCreationRequestCount dbEntry = updateRequestTries(simlarId, ip);
        checkRequestTriesLimit(dbEntry.getRequestTries(), settingsService.getAccountCreationMaxRequestsPerSimlarIdPerDay(),
                "too many create account requests %d >= %d for number: " + telephoneNumber);

        final Timestamp anHourAgo = Timestamp.from(Instant.now().minus(Duration.ofHours(1)));
        checkRequestTriesLimit(accountCreationRepository.sumRequestTries(ip, anHourAgo), settingsService.getAccountCreationMaxRequestsPerIpPerHour(),
                "too many create account requests %d >= %d for ip: " + ip);

        checkRequestTriesLimitWithAlert(accountCreationRepository.sumRequestTries(anHourAgo), settingsService.getAccountCreationMaxRequestsTotalPerHour(),
                "too many total create account requests %d >= %d within one hour");

        checkRequestTriesLimitWithAlert(accountCreationRepository.sumRequestTries(Timestamp.from(Instant.now().minus(Duration.ofDays(1)))),
                settingsService.getAccountCreationMaxRequestsTotalPerDay(),
                "too many total create account requests %d >= %d within one day");

        dbEntry.setRegistrationCode(Password.generateRegistrationCode());
        final String smsMessage = SmsText.create(smsText, dbEntry.getRegistrationCode());
        if (!smsService.sendSms(telephoneNumber, smsMessage)) {
            throw new XmlErrorFailedToSendSmsException("failed to send sms to '" + telephoneNumber + "' with text: " + smsMessage);
        }

        dbEntry.setPassword(Password.generate());
        accountCreationRepository.save(dbEntry);

        taskScheduler.schedule(() -> {
            if (!subscriberService.checkCredentials(dbEntry.getSimlarId(), dbEntry.getPassword())) {
                log.warn("no confirmation after '{}' seconds for number '{}'", WARN_TIMEOUT, telephoneNumber);
            }
        }, Date.from(Instant.now().plusSeconds(WARN_TIMEOUT)));

        log.info("created account request for simlarId: {}", simlarId);
        return new AccountRequest(simlarId, dbEntry.getPassword());
    }

    private AccountCreationRequestCount readAccountCreationRequest(final SimlarId simlarId) {
        final AccountCreationRequestCount dbEntry = accountCreationRepository.findBySimlarId(simlarId.get());
        return dbEntry != null ? dbEntry :
                new AccountCreationRequestCount(simlarId.get(), Password.generate(), Password.generateRegistrationCode());
    }

    private AccountCreationRequestCount updateRequestTries(final SimlarId simlarId, final String ip) {
        return transactionTemplate.execute(status -> {
            final AccountCreationRequestCount dbEntry = readAccountCreationRequest(simlarId);
            final Instant now = Instant.now();
            final Instant savedTimestamp = dbEntry.getTimestamp();
            if (savedTimestamp != null && Duration.between(savedTimestamp.plus(Duration.ofDays(1)), now).compareTo(Duration.ZERO) > 0) {
                dbEntry.setRequestTries(1);
            } else{
                dbEntry.incrementRequestTries();
            }
            dbEntry.setTimestamp(now);
            dbEntry.setIp(ip);
            return accountCreationRepository.save(dbEntry);
        });
    }

    @SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
    private static void checkRequestTriesLimit(final int requestTries, final int limit, final String message) {
        if (requestTries > limit) {
            throw new XmlErrorTooManyRequestTriesException(String.format(message, requestTries, limit));
        }
    }

    @SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
    private void checkRequestTriesLimitWithAlert(final int requestTries, final int limit, final String message) {
        if (requestTries == limit / 2) {
            for (final String alertNumber: settingsService.getAccountCreationAlertSmsNumbers()) {
                smsService.sendSms(alertNumber, "50% Alert for: " + message);
            }
        } else {
            checkRequestTriesLimit(requestTries, limit, message);
        }
    }

    public void confirmAccount(final String simlarIdString, @SuppressWarnings("TypeMayBeWeakened") final String registrationCode) {
        final SimlarId simlarId = SimlarId.create(simlarIdString);
        if (simlarId == null) {
            throw new XmlErrorNoSimlarIdException("confirm account request with simlarId: " + simlarIdString);
        }

        if (!checkRegistrationCode(registrationCode)) {
            throw new XmlErrorNoRegistrationCodeException("confirm account request with simlarId: " + simlarId + " and registrationCode: " + registrationCode);
        }

        final AccountCreationRequestCount creationRequest = transactionTemplate.execute(status -> {
            final AccountCreationRequestCount dbEntry = accountCreationRepository.findBySimlarId(simlarId.get());
            if (dbEntry == null) {
                throw new XmlErrorNoSimlarIdException("confirm account request with no creation request in db for simlarId: " + simlarId);
            }

            dbEntry.incrementConfirmTries();
            return accountCreationRepository.save(dbEntry);
        });

        final int confirmTries = creationRequest.getConfirmTries();
        if (confirmTries > settingsService.getAccountCreationMaxConfirms()) {
            throw new XmlErrorTooManyConfirmTriesException("Too many confirm tries(" + confirmTries + ") for simlarId: " + simlarId);
        }

        if (!Objects.equals(creationRequest.getRegistrationCode(), registrationCode)) {
            throw new XmlErrorWrongRegistrationCodeException("confirm account request with wrong registration code: " + registrationCode + " for simlarId: " + simlarId);
        }

        subscriberService.save(simlarId, creationRequest.getPassword());
    }

    private static boolean checkRegistrationCode(final CharSequence input) {
        return input != null && REGEX_REGISTRATION_CODE.matcher(input).matches();
    }
}
