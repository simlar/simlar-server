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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.simlar.simlarserver.database.models.AccountCreationRequestCount;
import org.simlar.simlarserver.database.repositories.AccountCreationRequestCountRepository;
import org.simlar.simlarserver.services.settingsservice.SettingsService;
import org.simlar.simlarserver.services.smsservice.SmsService;
import org.simlar.simlarserver.services.subscriberservice.SubscriberService;
import org.simlar.simlarserver.utils.LibPhoneNumber;
import org.simlar.simlarserver.utils.Password;
import org.simlar.simlarserver.utils.SimlarId;
import org.simlar.simlarserver.utils.SmsText;
import org.simlar.simlarserver.xml.XmlSuccessCreateAccountConfirm;
import org.simlar.simlarserver.xml.XmlSuccessCreateAccountRequest;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorFailedToSendSmsException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorInvalidTelephoneNumberException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorNoRegistrationCodeException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorNoSimlarIdException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorTooManyConfirmTriesException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorWrongRegistrationCodeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.regex.Pattern;

@AllArgsConstructor(onConstructor = @__(@Autowired))
@Component
public final class CreateAccountService {
    private static final Logger LOGGER = Logger.getLogger(CreateAccountService.class.getName());
    private static final Pattern REGEX_REGISTRATION_CODE = Pattern.compile("\\d{6}");

    private final SmsService smsService;
    private final SettingsService settingsService;
    private final AccountCreationRequestCountRepository accountCreationRepository;
    private final SubscriberService subscriberService;

    @SuppressFBWarnings("PRMC_POSSIBLY_REDUNDANT_METHOD_CALLS")
    public XmlSuccessCreateAccountRequest createAccountRequest(final String telephoneNumber, final String smsText, final String ip) {
        final SimlarId simlarId = SimlarId.createWithTelephoneNumber(telephoneNumber);
        if (simlarId == null) {
            throw new XmlErrorInvalidTelephoneNumberException("invalid telephone number: " + telephoneNumber);
        }

        if (!LibPhoneNumber.isValid(telephoneNumber)) {
            throw new XmlErrorInvalidTelephoneNumberException("libphonenumber invalidates telephone number: " + telephoneNumber);
        }

        final String registrationCode = Password.generateRegistrationCode();
        final String smsMessage = SmsText.create(smsText, registrationCode);
        if (!smsService.sendSms(telephoneNumber, smsMessage)) {
            throw new XmlErrorFailedToSendSmsException("failed to send sms to '" + telephoneNumber + "' with text: " + smsMessage);
        }

        final String password = Password.generate();

        final AccountCreationRequestCount dbEntry = ObjectUtils.defaultIfNull(
                accountCreationRepository.findBySimlarId(simlarId.get()),
                new AccountCreationRequestCount(simlarId.get()));
        dbEntry.setPassword(password);
        dbEntry.setRegistrationCode(registrationCode);
        dbEntry.setTimestamp(Instant.now());
        dbEntry.incrementRequestTries();
        dbEntry.setIp(ip);
        accountCreationRepository.save(dbEntry);

        return new XmlSuccessCreateAccountRequest(simlarId.get(), password);
    }

    public XmlSuccessCreateAccountConfirm confirmAccount(final String simlarIdString, final String registrationCode) {
        final SimlarId simlarId = SimlarId.create(simlarIdString);
        if (simlarId == null) {
            throw new XmlErrorNoSimlarIdException("confirm account request with simlarId: " + simlarIdString);
        }

        if (!checkRegistrationCode(registrationCode)) {
            throw new XmlErrorNoRegistrationCodeException("confirm account request with simlarId: " + simlarId + " and registrationCode: " + registrationCode);
        }

        final AccountCreationRequestCount creationRequest = accountCreationRepository.findBySimlarId(simlarId.get());
        if (creationRequest == null) {
            throw new XmlErrorNoSimlarIdException("confirm account request with no creation request in db for simlarId: " + simlarId);
        }

        final int confirmTries = creationRequest.incrementConfirmTries();
        accountCreationRepository.save(creationRequest);
        if (confirmTries >= settingsService.getAccountCreationMaxConfirms()) {
            throw new XmlErrorTooManyConfirmTriesException("Too many confirm tries(" + confirmTries + ") for simlarId: " + simlarId);
        }

        if (!Objects.equals(creationRequest.getRegistrationCode(), registrationCode)) {
            throw new XmlErrorWrongRegistrationCodeException("confirm account request with wrong registration code: " + registrationCode + " for simlarId: " + simlarId);
        }

        subscriberService.save(simlarId, creationRequest.getPassword());

        return new XmlSuccessCreateAccountConfirm(simlarId.get(), registrationCode);
    }

    private static boolean checkRegistrationCode(final CharSequence input) {
        return input != null && REGEX_REGISTRATION_CODE.matcher(input).matches();
    }
}
