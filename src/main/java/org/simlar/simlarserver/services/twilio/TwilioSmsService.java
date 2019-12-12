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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.simlar.simlarserver.data.TwilioRequestType;
import org.simlar.simlarserver.database.models.SmsProviderLog;
import org.simlar.simlarserver.database.repositories.SmsProviderLogRepository;
import org.simlar.simlarserver.json.twilio.MessageResponse;
import org.simlar.simlarserver.services.SharedSettings;
import org.simlar.simlarserver.services.smsservice.SmsService;
import org.simlar.simlarserver.utils.TwilioCallBackErrorCode;
import org.simlar.simlarserver.xml.XmlTwilioCallResponse;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorNoCallSessionException;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

@AllArgsConstructor
@Slf4j
@Component
public final class TwilioSmsService implements SmsService {
    public static final String REQUEST_PATH_DELIVERY    = "twilio/delivery-report.json";
    public static final String REQUEST_PATH_CALL_STATUS = "twilio/call-status.json";
    public static final String REQUEST_PATH_CALL        = "twilio/call.xml";

    private final SharedSettings sharedSettings;
    private final TwilioSettings twilioSettings;
    private final SmsProviderLogRepository smsProviderLogRepository;

    private String createCallbackBaseUrl() {
        return "https://" +
                twilioSettings.getCallbackUser() + ':' + twilioSettings.getCallbackPassword() + '@' +
                sharedSettings.getDomain() + ':' + sharedSettings.getPort() + '/';
    }

    private String postRequest(final TwilioRequestType type, final String telephoneNumber, final String text) {
        final MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("To", telephoneNumber);
        parameters.add("From", twilioSettings.getSmsSourceNumber());
        //noinspection SwitchStatement
        switch (type) { // NOPMD.SwitchStmtsShouldHaveDefault
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
            final String response = new RestTemplateBuilder().basicAuthentication(twilioSettings.getSid(), twilioSettings.getAuthToken()).build()
                    .postForObject(twilioSettings.getUrl() + type.getUrlPostfix(), parameters, String.class);

            log.info("response to '{}' request: '{}'", type, response);
            return response;
        } catch (final HttpClientErrorException e) {
            log.info("while sending '{}' request to '{}' received error '{}' response '{}'", type, telephoneNumber, e.getMessage(), e.getResponseBodyAsString());
            return e.getStatusCode() == HttpStatus.BAD_REQUEST ? e.getResponseBodyAsString() : null;
        } catch (final RestClientException e) {
            final String cause = ExceptionUtils.getRootCauseMessage(e);
            log.error("while sending '{}' request to '{}' failed to connect to twilio server '{}'", type, telephoneNumber, cause, e);
            smsProviderLogRepository.save(new SmsProviderLog(type, telephoneNumber, null, "SimlarServerException", cause, text));
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
                smsProviderLogRepository.save(new SmsProviderLog(type, telephoneNumber, null, messageResponse.getStatus(), messageResponse.getErrorCode() + " - " + messageResponse.getErrorMessage(), text));
                return false;
            }

            if (StringUtils.isEmpty(messageResponse.getStatus())) {
                log.error("while sending '{}' request to '{}' received message response without status '{}'", type, telephoneNumber, response);
                smsProviderLogRepository.save(new SmsProviderLog(type, telephoneNumber, null, "SimlarServerException", "not parsable response: " + response, text));
                return false;
            }

            log.info("while sending '{}' request to '{}' received message response: '{}' ", type, telephoneNumber , messageResponse);
            smsProviderLogRepository.save(new SmsProviderLog(type, telephoneNumber, messageResponse.getSid(), messageResponse.getStatus(), text));
            return true;
        } catch (final JsonMappingException | JsonParseException e) {
            log.error("while sending '{}' request to '{}' unable to parse response: '{}'", type, telephoneNumber, response, e);
            smsProviderLogRepository.save(new SmsProviderLog(type, telephoneNumber, null, "SimlarServerException", "not parsable response: " + response, text));
            return false;
        } catch (final JsonProcessingException e) {
            log.error("while sending '{}' request to '{}' JsonProcessingException during response parsing: '{}'", type, telephoneNumber, response, e);
            smsProviderLogRepository.save(new SmsProviderLog(type, telephoneNumber, null, "SimlarServerException", "not parsable response: " + response, text));
            return false;
        }
    }

    @SuppressWarnings({"BooleanMethodNameMustStartWithQuestion", "MethodWithMultipleReturnPoints"})
    private boolean doPostRequest(final TwilioRequestType type, final String telephoneNumber, final String text) {
        if (!twilioSettings.isConfigured()) {
            log.error("twilio not configured '{}'", twilioSettings);
            smsProviderLogRepository.save(new SmsProviderLog(type, telephoneNumber, null, "SimlarServerException", "twilio not configured", text));
            return false;
        }

        return handleResponse(type, telephoneNumber, text, postRequest(type, telephoneNumber, text));
    }

    @Override
    public boolean sendSms(final String telephoneNumber, final String text) {
        return doPostRequest(TwilioRequestType.SMS, telephoneNumber, text);
    }

    @Override
    public boolean call(final String telephoneNumber, final String text) {
        return doPostRequest(TwilioRequestType.CALL, telephoneNumber, text);
    }

    public void handleStatusReport(final TwilioRequestType type, @SuppressWarnings("TypeMayBeWeakened") final String telephoneNumber, final String messageSid, final String messageStatus, final String errorCode) {
        final SmsProviderLog smsProviderLog = smsProviderLogRepository.findBySessionId(messageSid);
        if (smsProviderLog == null) {
            log.error("no db entry");
            return;
        }

        final String savedTelephoneNumber = smsProviderLog.getTelephoneNumber();
        if (!StringUtils.equals(savedTelephoneNumber, telephoneNumber)) {
            log.warn("status report with unequal telephone numbers: saved='{}' received='{}'", savedTelephoneNumber, telephoneNumber);
        }

        final TwilioRequestType savedType = smsProviderLog.getType();
        if (savedType != type) {
            log.warn("status report with unequal type: saved='{}' received='{}'", savedType, type);
        }

        smsProviderLog.setCallbackTimestampToNow();
        smsProviderLog.setStatus(messageStatus);
        smsProviderLog.setError(TwilioCallBackErrorCode.createString(errorCode));
        smsProviderLogRepository.save(smsProviderLog);
    }

    public XmlTwilioCallResponse handleCall(final String callSid, @SuppressWarnings("TypeMayBeWeakened") final String telephoneNumber, final String callStatus) {
        final SmsProviderLog smsProviderLog = smsProviderLogRepository.findBySessionId(callSid);
        if (smsProviderLog == null) {
            log.error("no db entry");
            throw new XmlErrorNoCallSessionException("callSid='" + callSid + "' not found in DB");
        }

        if (smsProviderLog.getType() != TwilioRequestType.CALL) {
            log.error("call matches db entry for sms: '{}'", smsProviderLog);
            throw new XmlErrorNoCallSessionException("callSid='" + callSid + "' matches SMS");
        }

        final String savedTelephoneNumber = smsProviderLog.getTelephoneNumber();
        if (!StringUtils.equals(savedTelephoneNumber, telephoneNumber)) {
            log.warn("call with unequal telephone numbers: saved='{}' received '{}'", savedTelephoneNumber, telephoneNumber);
        }

        smsProviderLog.setCallbackTimestampToNow();
        smsProviderLog.setStatus(callStatus);
        smsProviderLogRepository.save(smsProviderLog);

        return new XmlTwilioCallResponse(smsProviderLog.getMessage());
    }
}
