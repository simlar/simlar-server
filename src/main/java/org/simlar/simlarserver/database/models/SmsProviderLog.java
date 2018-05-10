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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.simlar.simlarserver.data.TwilioRequestType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.time.Instant;

@SuppressWarnings("ClassWithTooManyMethods")
@SuppressFBWarnings("FCBL_FIELD_COULD_BE_LOCAL")
@Data
@NoArgsConstructor
@Entity
@Table(name = "simlar_sms_provider_log")
public final class SmsProviderLog {
    @SuppressWarnings("unused")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(length = 64)
    private TwilioRequestType type;

    @Column(nullable = false, length = 64)
    private String telephoneNumber;

    @Column(nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp timestamp;

    @Column(length = 64)
    private String sessionId;

    private Timestamp callbackTimestamp;

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
        this.timestamp         = Timestamp.from(Instant.now());
        this.sessionId         = sessionId;
        //noinspection AssignmentToNull
        this.callbackTimestamp = callbackTimestamp == null ? null : Timestamp.from(callbackTimestamp);
        this.status            = status;
        this.error             = StringUtils.left(error, 64);
        this.message           = message;
    }

    @SuppressWarnings("TypeMayBeWeakened") // Instant instead of Temporal
    public Instant getTimestamp() {
        return timestamp == null ? null : timestamp.toInstant();
    }

    @SuppressWarnings("TypeMayBeWeakened") // Instant instead of Temporal
    public Instant getCallbackTimestamp() {
        return callbackTimestamp == null ? null : callbackTimestamp.toInstant();
    }

    public void setCallbackTimestampToNow() {
        callbackTimestamp = Timestamp.from(Instant.now());
    }
}
