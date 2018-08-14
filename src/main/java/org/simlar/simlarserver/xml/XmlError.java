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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "error")
public final class XmlError {
    private int    id;
    private String message;

    public XmlError() {
        // needed for JAXBContext
    }

    private XmlError(final int id, final String message) {
        this.id = id;
        this.message = message;
    }


    public static XmlError wrongCredentials() {
        return new XmlError(10, "wrong credentials");
    }

    public static XmlError unknownStructure() {
        return new XmlError(1, "unknown structure");
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
