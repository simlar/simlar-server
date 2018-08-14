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
public final class XmlSuccessPushNotification {
    private int deviceType;
    private String pushId;

    public XmlSuccessPushNotification() {
        // needed for JAXBContext
    }

    public XmlSuccessPushNotification(final int deviceType, final String pushId) {
        this.deviceType = deviceType;
        this.pushId = pushId;
    }

    @XmlAttribute
    public int getDeviceType() {
        return deviceType;
    }

    @SuppressFBWarnings("UPM_UNCALLED_PRIVATE_METHOD")
    @SuppressWarnings("unused")
    private void setDeviceType(final int deviceType) {
        this.deviceType = deviceType;
    }

    @XmlAttribute
    public String getPushId() {
        return pushId;
    }

    @SuppressFBWarnings("UPM_UNCALLED_PRIVATE_METHOD")
    @SuppressWarnings("unused")
    private void setPushId(final String pushId) {
        this.pushId = pushId;
    }
}
