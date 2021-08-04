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

package org.simlar.simlarserver.controllers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.simlar.simlarserver.data.DeviceType;
import org.simlar.simlarserver.database.models.PushNotification;
import org.simlar.simlarserver.database.repositories.PushNotificationsRepository;
import org.simlar.simlarserver.services.subscriberservice.SubscriberService;
import org.simlar.simlarserver.utils.ApplePushId;
import org.simlar.simlarserver.xml.XmlSuccessPushNotification;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorUnknownApplePushIdException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorUnknownPushIdTypeException;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@Slf4j
@RestController
final class PushNotificationsController {
    public static final String REQUEST_PATH = "/store-push-id.xml";

    private final SubscriberService           subscriberService;
    private final PushNotificationsRepository pushNotificationsRepository;

    /**
     * This method handles http post requests. You may test it with:
     * <blockquote>
     * curl --data "login=*0001*&password=5c3d66f5a3928cca2821d711a2c016bb&deviceType=1&pushId=APAB1bE6JDTGtpHlM4b8K4Z4qst214PdaiZs5rhfk03iFhnwz6wVgMJN01l2homL7gBeE7EuC8ohfxqrDYgkknPY1VurG-5zzuiWQmgrhjhaptOC2LlQi2g9o7aG5gPP7ZmVWyLEL6DrZwN52OvB0egGu5fN3PDKAw" http://localhost:8080/store-push-id.xml
     * </blockquote>
     *
     * @param login
     *            the requesting user's simlarId
     * @param password
     *            the hash of the requesting user's password
     *            md5(simlarId + ":" + domain + ":" + password);
     * @param deviceType
     *            1 Android
     *            2..5 iOS
     * @param pushId
     *            The push notification token identifing the device.
     * @return XmlError or XmlSuccessPushNotification
     *            error message or success message containing deviceType and pushId
     */
    @SuppressWarnings("SpellCheckingInspection")
    @PostMapping(value = REQUEST_PATH, produces = MediaType.APPLICATION_XML_VALUE)
    public XmlSuccessPushNotification storePushId(@RequestParam final String login, @RequestParam final String password, @RequestParam final int deviceType, @RequestParam final String pushId) {
        log.info("'{}' requested with login '{}'", REQUEST_PATH, login);

        subscriberService.checkCredentialsWithException(login, password);

        final DeviceType checkedType = DeviceType.fromInt(deviceType);
        if (checkedType == null) {
            throw new XmlErrorUnknownPushIdTypeException("deviceType='" + deviceType + '\'');
        }

        if (checkedType.isIos() && !ApplePushId.check(pushId)) {
            throw new XmlErrorUnknownApplePushIdException("pushId='" + pushId + '\'');
        }

        pushNotificationsRepository.save(new PushNotification(login, checkedType, pushId));

        return new XmlSuccessPushNotification(deviceType, pushId);
    }
}
