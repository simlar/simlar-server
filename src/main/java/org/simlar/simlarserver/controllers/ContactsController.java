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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.simlar.simlarserver.services.subscriberservice.SubscriberService;
import org.simlar.simlarserver.utils.SimlarId;
import org.simlar.simlarserver.xml.XmlContact;
import org.simlar.simlarserver.xml.XmlContacts;
import org.simlar.simlarserver.xml.XmlError;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
final class ContactsController {
    public static final String      REQUEST_URL_CONTACTS_STATUS = "/get-contacts-status.xml";
    private static final Logger     logger                      = Logger.getLogger(ContactsController.class.getName());

    private final SubscriberService subscriberService;


    @Autowired
    public ContactsController(final SubscriberService subscriberService) {
        this.subscriberService = subscriberService;
    }

    /**
     * This method handles http post requests. You may test it with:
     * <blockquote>
     * curl --data "login=*0001*&password=xxxxxxx&contacts=*0002*|*0003*" http://localhost:8080/get-contacts-status.xml
     * </blockquote>
     *
     * @param login
     *            the requesting user's simlarId
     * @param password
     *            the hash of the requesting user's password md5(simlarId + ":"
     *            + domain + ":" + password);
     * @param contacts
     *            pipe separated list of simlarIds
     * @return XmlError or xmlContactList
     *            error message or contact list in xml
     */
    @RequestMapping(value = REQUEST_URL_CONTACTS_STATUS, method = RequestMethod.POST, produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public Object getContactStatus(@RequestParam final String login, @RequestParam final String password, @RequestParam final String contacts) {
        logger.info(REQUEST_URL_CONTACTS_STATUS + " requested with login=\"" + login + "\"");

        if (!subscriberService.checkCredentials(login, password)) {
            logger.info(REQUEST_URL_CONTACTS_STATUS + " requested with wrong credentials: login=\"" + login + "\"");
            return XmlError.wrongCredentials();
        }

        final List<XmlContact> xmlContactList = new ArrayList<>();

        for (final SimlarId contactSimlarId : SimlarId.parsePipeSeparatedSimlarIds(contacts)) {
            xmlContactList.add(new XmlContact(contactSimlarId.get(), subscriberService.getStatus(contactSimlarId)));
        }

        return new XmlContacts(xmlContactList);
    }
}
