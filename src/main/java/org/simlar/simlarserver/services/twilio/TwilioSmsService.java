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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.simlar.simlarserver.database.models.SmsSentLog;
import org.simlar.simlarserver.database.repositories.SmsSentLogRepository;
import org.simlar.simlarserver.json.twilio.MessageResponse;
import org.simlar.simlarserver.services.settingsservice.SettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public final class TwilioSmsService {
    private static final Logger LOGGER = Logger.getLogger(TwilioSmsService.class.getName());

    private final SettingsService       settingsService;
    private final TwilioSettingsService twilioSettingsService;
    private final SmsSentLogRepository  smsSentLogRepository;

    @Autowired
    public TwilioSmsService(final SettingsService settingsService, final TwilioSettingsService twilioSettingsService, final SmsSentLogRepository smsSentLogRepository) {
        this.settingsService       = settingsService;
        this.twilioSettingsService = twilioSettingsService;
        this.smsSentLogRepository  = smsSentLogRepository;
    }

    private String postRequest(final String telephoneNumber, final String text) {
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
            return response;
        } catch (final HttpClientErrorException e) {
            LOGGER.info("while sending sms to " + telephoneNumber + " received error: " + e.getMessage() + " response: " + e.getResponseBodyAsString());
            return e.getStatusCode() == HttpStatus.BAD_REQUEST ? e.getResponseBodyAsString() : null;
        } catch (final RestClientException e) {
            final String cause = ExceptionUtils.getRootCauseMessage(e);
            LOGGER.log(Level.SEVERE, "while sending sms to " + telephoneNumber + " failed to connect to twilio server: " + cause);
            smsSentLogRepository.save(new SmsSentLog(telephoneNumber, null, "SimlarServerException", cause, text));
            return null;
        }
    }

    @SuppressWarnings({"BooleanMethodNameMustStartWithQuestion", "MethodWithMultipleReturnPoints"})
    public boolean sendSms(final String telephoneNumber, final String text) {
        if (!twilioSettingsService.isConfigured()) {
            LOGGER.severe("twilio not configured: " + twilioSettingsService);
            smsSentLogRepository.save(new SmsSentLog(telephoneNumber, null, "SimlarServerException", "twilio not configured", text));
            return false;
        }

        return handleResponse(telephoneNumber, text, postRequest(telephoneNumber, text));
    }

    @SuppressWarnings("BooleanMethodNameMustStartWithQuestion")
    boolean handleResponse(final String telephoneNumber, final String text, final String response) {
        if (StringUtils.isEmpty(response)) {
            LOGGER.severe("while sending sms to " + telephoneNumber + " received empty response");
            return false;
        }

        try {
            final MessageResponse messageResponse = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).readValue(response, MessageResponse.class);
            if (StringUtils.isEmpty(messageResponse.getSid())) {
                LOGGER.severe("while sending sms to " + telephoneNumber + " received message response without MessageSid: " + response);
                smsSentLogRepository.save(new SmsSentLog(telephoneNumber, null, messageResponse.getStatus(), messageResponse.getErrorCode() + " - " + messageResponse.getErrorMessage(), text));
                return false;
            }

            if (StringUtils.isEmpty(messageResponse.getStatus())) {
                LOGGER.severe("while sending sms to " + telephoneNumber + " received message response without status: " + response);
                smsSentLogRepository.save(new SmsSentLog(telephoneNumber, null, "SimlarServerException", "not parsable response: " + response, text));
                return false;
            }

            LOGGER.info("while sending sms to " + telephoneNumber + " received message response: " + messageResponse);
            smsSentLogRepository.save(new SmsSentLog(telephoneNumber, messageResponse.getSid(), messageResponse.getStatus(), text));
            return true;
        } catch (final JsonMappingException | JsonParseException e) {
            LOGGER.log(Level.SEVERE, "while sending sms to " + telephoneNumber + " unable to parse response: " + response, e);
            smsSentLogRepository.save(new SmsSentLog(telephoneNumber, null, "SimlarServerException", "not parsable response: " + response, text));
            return false;
        } catch (final IOException e) {
            LOGGER.log(Level.SEVERE, "while sending sms to " + telephoneNumber + " IOException during response parsing: " + response, e);
            smsSentLogRepository.save(new SmsSentLog(telephoneNumber, null, "SimlarServerException", "not parsable response: " + response, text));
            return false;
        }
    }

    public void handleDeliveryReport(final String telephoneNumber, final String messageSid, final String messageStatus, final String errorCode) {
        final SmsSentLog smsSentLog = smsSentLogRepository.findByDlrNumber(messageSid);
        if (smsSentLog == null) {
            LOGGER.severe("no db entry");
            return;
        }

        smsSentLog.setDlrTimestampToNow();
        smsSentLog.setTwilioStatus(messageStatus);
        smsSentLog.setTwilioError(errorCode);
        smsSentLogRepository.save(smsSentLog);
    }
}
