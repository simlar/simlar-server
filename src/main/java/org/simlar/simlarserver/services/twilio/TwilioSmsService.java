/*
 * Copyright (C) 2016 The Simlar Authors.
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

package org.simlar.simlarserver.services.twilio;

import org.simlar.simlarserver.services.settingsservice.SettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public final class TwilioSmsService {
    private static final Logger LOGGER = Logger.getLogger(TwilioSmsService.class.getName());

    private final SettingsService       settingsService;
    private final TwilioSettingsService twilioSettingsService;

    @Autowired
    public TwilioSmsService(final SettingsService settingsService, final TwilioSettingsService twilioSettingsService) {
        this.settingsService       = settingsService;
        this.twilioSettingsService = twilioSettingsService;
    }

    @SuppressWarnings({"BooleanMethodNameMustStartWithQuestion", "MethodWithMultipleReturnPoints"})
    public boolean sendSms(final String telephoneNumber, final String text) {
        if (!twilioSettingsService.isConfigured()) {
            LOGGER.severe("twilio not configured: " + twilioSettingsService);
            return false;
        }

        final MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("To", telephoneNumber);
        parameters.add("From", twilioSettingsService.getSmsSourceNumber());
        parameters.add("StatusCallback", "https://" +
                twilioSettingsService.getCallbackUser() + ':' + twilioSettingsService.getCallbackPassword() + '@' +
                settingsService.getDomain() + ":6161/twilio/delivery-report.php");
        parameters.add("Body", text);

        try {
            final String response = new RestTemplateBuilder().basicAuthorization(twilioSettingsService.getSid(), twilioSettingsService.getAuthToken()).build()
                    .postForObject(twilioSettingsService.getUrl(), parameters, String.class);

            LOGGER.info("response: " + response);
            return true;
        } catch (final HttpClientErrorException e) {
            LOGGER.info("while sending sms to " + telephoneNumber + " received error response: " + e.getMessage() + " response: " + e.getResponseBodyAsString());
            return false;
        } catch (final RestClientException e) {
            LOGGER.log(Level.SEVERE, "while sending sms to " + telephoneNumber + " failed to connect to twilio server", e);
            return false;
        }
    }
}
