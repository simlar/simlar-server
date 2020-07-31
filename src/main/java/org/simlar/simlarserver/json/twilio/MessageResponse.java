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

package org.simlar.simlarserver.json.twilio;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@SuppressWarnings("unused")
@Getter
@ToString
@EqualsAndHashCode
public final class MessageResponse {
    private String sid;
    private String to;
    private String status;
    private String price;
    @JsonProperty("priceUnit")
    private String priceUnit;
    @JsonProperty("errorCode")
    private String errorCode;
    @JsonProperty("message")
    private String errorMessage;

    // We are not interested in every field of the twilio response. At least the following are ignored:
    //  account_sid, from, date_created, date_updated, date_sent, messaging_service_sid, body, num_segments, num_media, direction, api_version, uri, subresource_uris, media
}
