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

package org.simlar.simlarserver;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.simlar.simlarserver.services.subscriberservice.SubscriberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public final class ContactsController {
    public static final String      REQUEST_URL_CONTACTS_STATUS = "/get-contacts-status.xml";
    private static final Logger     logger                      = Logger.getLogger(ContactsController.class.getName());

    private final SubscriberService subscriberService;

    public static final class XmlContact {
        private String simlarId;
        private int    status;

        public XmlContact() {
            // needed for JAXBContext
        }

        public XmlContact(final String simlarId, final int status) {
            this.simlarId = simlarId;
            this.status = status;
        }

        @XmlAttribute(name = "id")
        public String getSimlarId() {
            return simlarId;
        }

        public void setSimlarId(final String simlarId) {
            this.simlarId = simlarId;
        }

        @XmlAttribute
        public int getStatus() {
            return status;
        }

        public void setStatus(final int status) {
            this.status = status;
        }
    }

    @XmlRootElement(name = "contacts")
    public static final class XmlContacts {
        private List<XmlContact> contacts;

        public XmlContacts() {
            // needed for JAXBContext
        }

        public XmlContacts(final List<XmlContact> contacts) {
            this.contacts = contacts;
        }

        @XmlElement(name = "contact")
        public List<XmlContact> getContacts() {
            return contacts;
        }

        public void setContacts(final List<XmlContact> contacts) {
            this.contacts = contacts;
        }
    }

    @XmlRootElement(name = "error")
    public static final class XmlError {
        private int    id;
        private String message;

        public XmlError() {
            // needed for JAXBContext
        }

        public XmlError(final int id, final String message) {
            this.id = id;
            this.message = message;
        }

        @XmlAttribute
        public int getId() {
            return id;
        }

        public void setId(final int id) {
            this.id = id;
        }

        @XmlAttribute
        public String getMessage() {
            return message;
        }

        public void setMessage(final String message) {
            this.message = message;
        }
    }

    @Autowired
    public ContactsController(final SubscriberService subscriberService) {
        this.subscriberService = subscriberService;
    }

    /**
     * This method handles http post requests. You may test it with:
     * <blockquote>
     * curl --data "login=*0001*&password=xxxxxx&contacts=*0002*|*0003*" http://localhost:8080/get-contacts-status.xml
     * </blockquote>
     *
     * @param login
     *            the requesting user's simlarId
     * @param password
     *            the hash of the requesting user's password md5(simlarId + ":"
     *            + domain + ":" + password);
     * @param contacts
     *            pipe separated list of simlarIds
     * @return
     */
    @RequestMapping(value = REQUEST_URL_CONTACTS_STATUS, method = RequestMethod.POST, produces = "application/xml")
    @ResponseBody
    public Object getContactStatus(@RequestParam final String login, @RequestParam final String password, @RequestParam final String contacts) {
        logger.info(REQUEST_URL_CONTACTS_STATUS + " requested with login=\"" + login + "\"");

        if (!subscriberService.checkCredentials(login, password)) {
            logger.info(REQUEST_URL_CONTACTS_STATUS + " requested with wrong credentials: login=\"" + login + "\"");
            return new XmlError(20, "wrong credentials");
        }

        final List<XmlContact> xmlContactList = new ArrayList<>();
        for (final String contact : contacts.split("\\|")) {
            final String contactSimlarId = contact.trim();
            xmlContactList.add(new XmlContact(contactSimlarId, subscriberService.getStatus(contactSimlarId)));
        }

        return new XmlContacts(xmlContactList);
    }
}
