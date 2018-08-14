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

import org.simlar.simlarserver.data.DeviceType;
import org.simlar.simlarserver.database.models.PushNotification;
import org.simlar.simlarserver.database.repositories.PushNotificationsRepository;
import org.simlar.simlarserver.services.subscriberservice.SubscriberService;
import org.simlar.simlarserver.utils.ApplePushId;
import org.simlar.simlarserver.xml.XmlSuccessPushNotification;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorUnknownApplePushIdException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorUnknownPushIdTypeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.logging.Logger;

@RestController
final class PushNotificationsController {
    public  static final String REQUEST_PATH = "/store-push-id.xml";
    private static final Logger LOGGER       = Logger.getLogger(PushNotificationsController.class.getName());

    private final SubscriberService           subscriberService;
    private final PushNotificationsRepository pushNotificationsRepository;

    @Autowired
    private PushNotificationsController(final SubscriberService subscriberService, final PushNotificationsRepository pushNotificationsRepository) {
        this.subscriberService           = subscriberService;
        this.pushNotificationsRepository = pushNotificationsRepository;
    }

    /**
     * This method handles http post requests. You may test it with:
     * <blockquote>
     * curl --data "login=*0001*&password=5c3d66f5a3928cca2821d711a2c016bb&deviceType=1&pushId=APAB1bE6JDTGtpHlM4b8K4Z4qst214PdaiZs5rhfk03iFhnwz6wVgMJN01l2homL7gBeE7EuC8ohfxqrDYgkknPY1VurG-5zzuiWQmgrhjhaptOC2LlQi2g9o7aG5gPP7ZmVWyLEL6DrZwN52OvB0egGu5fN3PDKAw" http://localhost:8080/store-push-id.xml ; echo ""
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
     *            The Id to send push notifications to.
     * @return XmlError or xmlContactList
     *            error message or success message containing deviceType and pushId
     */
    @SuppressWarnings("SpellCheckingInspection")
    @RequestMapping(value = REQUEST_PATH, method = RequestMethod.POST, produces = MediaType.APPLICATION_XML_VALUE)
    public XmlSuccessPushNotification getContactStatus(@RequestParam final String login, @RequestParam final String password, @RequestParam final int deviceType, @RequestParam final String pushId) {
        LOGGER.info(REQUEST_PATH + " requested with login=\"" + login + '\"');

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
