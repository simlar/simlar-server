/*
 * Copyright (C) 2015 The Simlar Authors.
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

import org.simlar.simlarserver.services.delaycalculatorservice.DelayCalculatorService;
import org.simlar.simlarserver.services.subscriberservice.SubscriberService;
import org.simlar.simlarserver.utils.SimlarId;
import org.simlar.simlarserver.xml.XmlContact;
import org.simlar.simlarserver.xml.XmlContacts;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorRequestedTooManyContactsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@RestController
final class ContactsController {
    public  static final String REQUEST_PATH  = "/get-contacts-status.xml";
    private static final Logger LOGGER        = Logger.getLogger(ContactsController.class.getName());
    private static final int    DELAY_MAXIMUM = 8; // seconds
    private static final long   DELAY_MINIMUM = 10; // milliseconds

    private final SubscriberService subscriberService;
    private final DelayCalculatorService delayCalculatorService;
    private final TaskScheduler taskScheduler;


    @Autowired
    private ContactsController(final SubscriberService subscriberService, final DelayCalculatorService delayCalculatorService, final TaskScheduler taskScheduler) {
        this.subscriberService      = subscriberService;
        this.delayCalculatorService = delayCalculatorService;
        this.taskScheduler          = taskScheduler;
    }

    /**
     * This method handles http post requests. You may test it with:
     * <blockquote>
     * curl --data "login=*0001*&password=5c3d66f5a3928cca2821d711a2c016bb&contacts=*0002*|*0003*" http://localhost:8080/get-contacts-status.xml
     * </blockquote>
     *
     * @param login
     *            the requesting user's simlarId
     * @param password
     *            the hash of the requesting user's password
     *            md5(simlarId + ":" + domain + ":" + password);
     * @param contacts
     *            pipe separated list of simlarIds
     * @return XmlError or xmlContactList
     *            error message or contact list in xml
     */
    @RequestMapping(value = REQUEST_PATH, method = RequestMethod.POST, produces = MediaType.APPLICATION_XML_VALUE)
    public DeferredResult<XmlContacts> getContactStatus(@RequestParam final String login, @RequestParam final String password, @RequestParam final String contacts) {
        LOGGER.info(REQUEST_PATH + " requested with login=\"" + login + '\"');

        subscriberService.checkCredentialsWithException(login, password);

        final List<SimlarId> simlarIds = SimlarId.parsePipeSeparatedSimlarIds(contacts);
        final int delay = delayCalculatorService.calculateRequestDelay(SimlarId.create(login), simlarIds);
        if (delay > DELAY_MAXIMUM) {
            throw new XmlErrorRequestedTooManyContactsException("request delay=" + delay + " blocking simlarId=" + login);
        }

        final long delayMillis = Math.max(DELAY_MINIMUM, delay * 1000L);
        final Instant instant = Instant.now().plus(delayMillis, ChronoUnit.MILLIS);
        LOGGER.info("scheduling getContactStatus to: " + instant);

        final DeferredResult<XmlContacts> deferredResult = new DeferredResult<>();
        taskScheduler.schedule(() -> {
            if (deferredResult.isSetOrExpired()) {
                LOGGER.severe("deferred result already set or expired simlarId=" + login + " delay=" + delay + " seconds");
            } else {
                LOGGER.info("executing getContactStatus scheduled to: " + instant);
                deferredResult.setResult(
                        new XmlContacts(simlarIds.stream()
                                .map(contactSimlarId -> new XmlContact(contactSimlarId.get(), subscriberService.getStatus(contactSimlarId)))
                                .collect(Collectors.toList()))
                );
            }
        }, Date.from(instant));

        return deferredResult;
    }
}
