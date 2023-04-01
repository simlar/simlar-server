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

package org.simlar.simlarserver.database.models;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.simlar.simlarserver.data.TwilioRequestType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@SuppressWarnings("ClassWithTooManyMethods")
@NoArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
@Table(name = "simlar_sms_provider_log")
public final class SmsProviderLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(length = 64)
    private TwilioRequestType type;

    @Column(nullable = false, length = 64)
    private String telephoneNumber;

    @Column(nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Instant timestamp;

    @Column(length = 64)
    private String sessionId;

    @Column(columnDefinition = "TIMESTAMP NULL DEFAULT NULL")
    private Instant callbackTimestamp;

    @Column(length = 64)
    private String status;

    @Column(length = 64)
    private String error;

    @Column(length = 170)
    private String message;

    public SmsProviderLog(final TwilioRequestType type, final String telephoneNumber, final String sessionId, final String status, final String message) {
        this(type, telephoneNumber, sessionId, status, null, message);
    }

    @SuppressWarnings("ConstructorWithTooManyParameters")
    public SmsProviderLog(final TwilioRequestType type, final String telephoneNumber, final String sessionId, final String status, final String message, final Instant callbackTimestamp) {
        this(type, telephoneNumber, sessionId, status, null, message, callbackTimestamp);
    }

    @SuppressWarnings("ConstructorWithTooManyParameters")
    public SmsProviderLog(final TwilioRequestType type, final String telephoneNumber, final String sessionId, final String status, final String error, final String message) {
        this(type, telephoneNumber, sessionId, status, error, message, null);
    }

    @SuppressWarnings({"UnnecessaryThis", "ConstructorWithTooManyParameters"})
    public SmsProviderLog(final TwilioRequestType type, final String telephoneNumber, final String sessionId, final String status, final String error, final String message, final Instant callbackTimestamp) {
        this.type              = type;
        this.telephoneNumber   = telephoneNumber;
        this.timestamp         = Instant.now();
        this.sessionId         = sessionId;
        this.callbackTimestamp = callbackTimestamp;
        this.status            = status;
        this.error             = StringUtils.left(error, 64);
        this.message           = message;
    }

    public void setCallbackTimestampToNow() {
        callbackTimestamp = Instant.now();
    }
}
