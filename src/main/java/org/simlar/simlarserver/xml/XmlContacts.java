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

package org.simlar.simlarserver.xml;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "contacts")
public final class XmlContacts {
    private List<XmlContact> contacts;

    public XmlContacts() {
        // needed for JAXBContext
    }

    public XmlContacts(final List<XmlContact> contacts) {
        //noinspection AssignmentToCollectionOrArrayFieldFromParameter
        this.contacts = contacts;
    }

    @XmlElement(name = "contact")
    public List<XmlContact> getContacts() {
        //noinspection ReturnOfCollectionOrArrayField // JAXB crashes with Collections.unmodifiableList
        return contacts;
    }

    @SuppressFBWarnings("UPM_UNCALLED_PRIVATE_METHOD")
    @SuppressWarnings("unused")
    private void setContacts(final List<XmlContact> contacts) {
        this.contacts = contacts;
    }
}
