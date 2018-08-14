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

package org.simlar.simlarserver.xml;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "success")
public final class XmlSuccessCreateAccountRequest {
    private String simlarId;
    private String password;

    public XmlSuccessCreateAccountRequest() {
        // needed for JAXBContext
    }

    public XmlSuccessCreateAccountRequest(final String simlarId, final String password) {
        this.simlarId = simlarId;
        this.password = password;
    }


    @XmlAttribute
    public String getSimlarId() {
        return simlarId;
    }

    @SuppressFBWarnings("UPM_UNCALLED_PRIVATE_METHOD")
    @SuppressWarnings("unused")
    private void setSimlarId(final String simlarId) {
        this.simlarId = simlarId;
    }

    @SuppressWarnings("TypeMayBeWeakened")
    @XmlAttribute
    public String getPassword() {
        return password;
    }

    @SuppressFBWarnings("UPM_UNCALLED_PRIVATE_METHOD")
    @SuppressWarnings("unused")
    private void setPassword(final String password) {
        this.password = password;
    }
}
