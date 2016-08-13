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

import javax.xml.bind.annotation.XmlAttribute;

public final class XmlContact {
    private String simlarId;
    private int    status;

    @SuppressWarnings("unused")
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

    @SuppressFBWarnings("UPM_UNCALLED_PRIVATE_METHOD")
    @SuppressWarnings("unused")
    private void setSimlarId(final String simlarId) {
        this.simlarId = simlarId;
    }

    @XmlAttribute
    public int getStatus() {
        return status;
    }

    @SuppressFBWarnings("UPM_UNCALLED_PRIVATE_METHOD")
    @SuppressWarnings("unused")
    private void setStatus(final int status) {
        this.status = status;
    }
}
