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
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.simlar.simlarserver.data.TwilioRequestType;
import org.simlar.simlarserver.database.models.SmsSentLog;
import org.simlar.simlarserver.database.repositories.SmsSentLogRepository;
import org.simlar.simlarserver.json.twilio.MessageResponse;
import org.simlar.simlarserver.services.settingsservice.SettingsService;
import org.simlar.simlarserver.services.smsservice.SmsService;
import org.simlar.simlarserver.utils.TwilioCallBackErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import java.io.IOException;

@AllArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
@Component
public final class TwilioSmsService implements SmsService {
    public static final String REQUEST_PATH_DELIVERY    = "twilio/delivery-report.json";
    public static final String REQUEST_PATH_CALL_STATUS = "twilio/call-status.json";
    public static final String REQUEST_PATH_CALL        = "twilio/call.xml";

    private final SettingsService       settingsService;
    private final TwilioSettingsService twilioSettingsService;
    private final SmsSentLogRepository  smsSentLogRepository;

    private String createCallbackBaseUrl() {
        return "https://" +
                twilioSettingsService.getCallbackUser() + ':' + twilioSettingsService.getCallbackPassword() + '@' +
                settingsService.getDomain() + ':' + settingsService.getPort() + '/';
    }

    private String postRequest(final TwilioRequestType type, final String telephoneNumber, final String text) {
        final MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("To", telephoneNumber);
        parameters.add("From", twilioSettingsService.getSmsSourceNumber());
        switch (type) {
            case SMS:
                parameters.add("StatusCallback", createCallbackBaseUrl() + REQUEST_PATH_DELIVERY);
                parameters.add("Body", text);
                break;
            case CALL:
                parameters.add("Url", createCallbackBaseUrl() + REQUEST_PATH_CALL);
                parameters.add("StatusCallback", createCallbackBaseUrl() + REQUEST_PATH_CALL_STATUS);
                break;
        }

        try {
            final String response = new RestTemplateBuilder().basicAuthorization(twilioSettingsService.getSid(), twilioSettingsService.getAuthToken()).build()
                    .postForObject(twilioSettingsService.getUrl() + type.getUrlPostfix(), parameters, String.class);

            log.info("response to '{}' request: '{}'", type, response);
            return response;
        } catch (final HttpClientErrorException e) {
            log.info("while sending '{}' request to '{}' received error '{}' response '{}'", type, telephoneNumber, e.getMessage(), e.getResponseBodyAsString());
            return e.getStatusCode() == HttpStatus.BAD_REQUEST ? e.getResponseBodyAsString() : null;
        } catch (final RestClientException e) {
            final String cause = ExceptionUtils.getRootCauseMessage(e);
            log.error("while sending '{}' request to '{}' failed to connect to twilio server '{}'", type, telephoneNumber, cause, e);
            smsSentLogRepository.save(new SmsSentLog(type, telephoneNumber, null, "SimlarServerException", cause, text));
            return null;
        }
    }

    @SuppressWarnings("BooleanMethodNameMustStartWithQuestion")
    boolean handleResponse(final TwilioRequestType type, final String telephoneNumber, final String text, final String response) {
        if (StringUtils.isEmpty(response)) {
            log.error("while sending '{}' request to '{}' received empty response", type, telephoneNumber);
            return false;
        }

        try {
            final MessageResponse messageResponse = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).readValue(response, MessageResponse.class);
            if (StringUtils.isEmpty(messageResponse.getSid())) {
                log.error("while sending '{}' request to '{}' received message response without MessageSid '{}'", type, telephoneNumber, response);
                smsSentLogRepository.save(new SmsSentLog(type, telephoneNumber, null, messageResponse.getStatus(), messageResponse.getErrorCode() + " - " + messageResponse.getErrorMessage(), text));
                return false;
            }

            if (StringUtils.isEmpty(messageResponse.getStatus())) {
                log.error("while sending '{}' request to '{}' received message response without status '{}'", type, telephoneNumber, response);
                smsSentLogRepository.save(new SmsSentLog(type, telephoneNumber, null, "SimlarServerException", "not parsable response: " + response, text));
                return false;
            }

            log.info("while sending '{}' request to '{}' received message response: '{}' ", type, telephoneNumber , messageResponse);
            smsSentLogRepository.save(new SmsSentLog(type, telephoneNumber, messageResponse.getSid(), messageResponse.getStatus(), text));
            return true;
        } catch (final JsonMappingException | JsonParseException e) {
            log.error("while sending '{}' request to '{}' unable to parse response: '{}'", type, telephoneNumber, response, e);
            smsSentLogRepository.save(new SmsSentLog(type, telephoneNumber, null, "SimlarServerException", "not parsable response: " + response, text));
            return false;
        } catch (final IOException e) {
            log.error("while sending '{}' request to '{}' IOException during response parsing: '{}'", type, telephoneNumber, response, e);
            smsSentLogRepository.save(new SmsSentLog(type, telephoneNumber, null, "SimlarServerException", "not parsable response: " + response, text));
            return false;
        }
    }

    @SuppressWarnings({"BooleanMethodNameMustStartWithQuestion", "MethodWithMultipleReturnPoints"})
    private boolean doPostRequest(final TwilioRequestType type, final String telephoneNumber, final String text) {
        if (!twilioSettingsService.isConfigured()) {
            log.error("twilio not configured '{}'", twilioSettingsService);
            smsSentLogRepository.save(new SmsSentLog(type, telephoneNumber, null, "SimlarServerException", "twilio not configured", text));
            return false;
        }

        return handleResponse(type, telephoneNumber, text, postRequest(type, telephoneNumber, text));
    }

    @Override
    @SuppressWarnings("BooleanMethodNameMustStartWithQuestion")
    public boolean sendSms(final String telephoneNumber, final String text) {
        return doPostRequest(TwilioRequestType.SMS, telephoneNumber, text);
    }

    @SuppressWarnings("BooleanMethodNameMustStartWithQuestion")
    public boolean call(final String telephoneNumber) {
        return doPostRequest(TwilioRequestType.CALL, telephoneNumber, null);
    }

    @SuppressFBWarnings("PRMC_POSSIBLY_REDUNDANT_METHOD_CALLS")
    public void handleStatusReport(final TwilioRequestType type, @SuppressWarnings("TypeMayBeWeakened") final String telephoneNumber, final String messageSid, final String messageStatus, final String errorCode) {
        final SmsSentLog smsSentLog = smsSentLogRepository.findByDlrNumber(messageSid);
        if (smsSentLog == null) {
            log.error("no db entry");
            return;
        }

        if (!StringUtils.equals(smsSentLog.getTelephoneNumber(), telephoneNumber)) {
            log.warn("status report with unequal telephone numbers: saved='{}' received '{}'", smsSentLog.getTelephoneNumber(), telephoneNumber);
        }

        if (smsSentLog.getType() != type) {
            log.warn("status report with unequal type: saved='{}' received '{}'", smsSentLog.getType(), type);
        }

        smsSentLog.setDlrTimestampToNow();
        smsSentLog.setTwilioStatus(messageStatus);
        smsSentLog.setTwilioError(TwilioCallBackErrorCode.createString(errorCode));
        smsSentLogRepository.save(smsSentLog);
    }
}
