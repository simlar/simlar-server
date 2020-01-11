/*
 * Copyright (C) 2019 The Simlar Authors.
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

package org.simlar.simlarserver.controllers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.simlar.simlarserver.services.pushnotification.PushNotificationService;
import org.simlar.simlarserver.services.pushnotification.PushNotificationSettings;
import org.simlar.simlarserver.utils.SimlarId;
import org.simlar.simlarserver.xml.XmlSuccessSendPushNotification;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorFailedToRequestPushNotificationException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorWrongCredentialsException;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@Slf4j
@RestController
final class SendPushNotificationController {
    public static final String REQUEST_PATH = "/send-push-notification.xml";

    private final PushNotificationService pushNotificationsService;
    private final PushNotificationSettings pushNotificationsSettings;

    /**
     * This method handles http post requests. You may test it with:
     * <blockquote>
     * curl --data "apiKey=someKey&simlarId=*0001*" http://localhost:8080/send-push-notification.xml
     * </blockquote>
     *
     * @param apiKey
     *            a key for authorization
     * @param simlarId
     *            the simlarId identifies the client the push notification should go to
     * @return XmlError or XmlSuccessPushSendNotification
     *            error message or success message containing deviceType and pushId
     */
    @PostMapping(value = REQUEST_PATH, produces = MediaType.APPLICATION_XML_VALUE)
    public XmlSuccessSendPushNotification sendPushNotification(@RequestParam final String apiKey, @RequestParam final String simlarId) {
        log.info("'{}' requested with simlarId '{}'", REQUEST_PATH, simlarId);

        final String configuredApiKey = pushNotificationsSettings.getApiKey();
        if (StringUtils.isBlank(configuredApiKey)) {
            throw new XmlErrorWrongCredentialsException("received apiKey '" + apiKey + "' but none configured");
        }

        if (!StringUtils.equals(apiKey, configuredApiKey)) {
            throw new XmlErrorWrongCredentialsException("wrong apiKey '" + apiKey + '\'');
        }

        final String messageId = pushNotificationsService.sendPushNotification(null, SimlarId.create(simlarId));
        if (StringUtils.isBlank(messageId)) {
            throw new XmlErrorFailedToRequestPushNotificationException("failed to request push notification to simlarId '" + simlarId + '\'');
        }

        return new XmlSuccessSendPushNotification(messageId);
    }
}
