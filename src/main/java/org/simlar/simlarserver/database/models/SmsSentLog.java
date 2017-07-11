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
import org.apache.commons.lang3.StringUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.time.Instant;

@SuppressWarnings("LocalCanBeFinal")
@SuppressFBWarnings("FCBL_FIELD_COULD_BE_LOCAL")
@Entity
@Table(name = "simlar_sms_sent_log_java")
public final class SmsSentLog {
    @SuppressWarnings("unused")
    @Id
    @GeneratedValue
    private int id;

    @Column(nullable = false, length = 64)
    private String telephoneNumber;

    @Column(nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp timestamp;

    @Column(length = 64)
    private String dlrNumber;

    @Column(nullable = false, columnDefinition = "int(10) signed DEFAULT -1")
    private int dlrStatus;

    @Column(columnDefinition = "TIMESTAMP NULL DEFAULT NULL") /// hibernate does not support columnDefinition = "TIMESTAMP DEFAULT '0000-00-00 00:00:00'"
    private Timestamp dlrTimestamp;

    @Column(nullable = false, columnDefinition = "int(10) signed DEFAULT -1")
    private int smsTradeStatus;

    @Column(length = 64)
    private String twilioStatus;

    @Column(length = 64)
    private String twilioError;

    @Column(length = 170)
    private String message;

    @SuppressWarnings({"unused", "ProtectedMemberInFinalClass"})
    protected SmsSentLog() {
        // no-args constructor required by JPA spec
        // this one is protected since it shouldn't be used directly
    }

    public SmsSentLog(final String telephoneNumber, final String dlrNumber, final String twilioStatus, final String message) {
        this(telephoneNumber, dlrNumber, twilioStatus, null, message);
    }

    public SmsSentLog(final String telephoneNumber, final String dlrNumber, final String twilioStatus, final String message, final Instant dlrTimestamp) {
        this(telephoneNumber, dlrNumber, twilioStatus, null, message, dlrTimestamp);
    }

    public SmsSentLog(final String telephoneNumber, final String dlrNumber, final String twilioStatus, final String twilioError, final String message) {
        this(telephoneNumber, dlrNumber, twilioStatus, twilioError, message, null);
    }

    @SuppressWarnings({"UnnecessaryThis", "ConstructorWithTooManyParameters"})
    private SmsSentLog(final String telephoneNumber, final String dlrNumber, final String twilioStatus, final String twilioError, final String message, final Instant dlrTimestamp) {
        this.telephoneNumber = telephoneNumber;
        this.timestamp       = Timestamp.from(Instant.now());
        this.dlrNumber       = dlrNumber;
        this.dlrStatus       = -1;
        //noinspection AssignmentToNull
        this.dlrTimestamp    = dlrTimestamp == null ? null : Timestamp.from(dlrTimestamp);
        this.smsTradeStatus  = -2;
        this.twilioStatus    = twilioStatus;
        this.twilioError     = StringUtils.left(twilioError, 64);
        this.message         = message;
    }

    @SuppressWarnings("TypeMayBeWeakened")
    public String getTelephoneNumber() {
        return telephoneNumber;
    }

    @SuppressWarnings("TypeMayBeWeakened") // Instant instead of Temporal
    public Instant getTimestamp() {
        return timestamp == null ? null : timestamp.toInstant();
    }

    public String getDlrNumber() {
        return dlrNumber;
    }

    public int getDlrStatus() {
        return dlrStatus;
    }

    @SuppressWarnings("TypeMayBeWeakened") // Instant instead of Temporal
    public Instant getDlrTimestamp() {
        return dlrTimestamp == null ? null : dlrTimestamp.toInstant();
    }

    public void setDlrTimestampToNow() {
        dlrTimestamp = Timestamp.from(Instant.now());
    }

    public int getSmsTradeStatus() {
        return smsTradeStatus;
    }

    public String getTwilioStatus() {
        return twilioStatus;
    }

    public void setTwilioStatus(String twilioStatus) {
        this.twilioStatus = twilioStatus;
    }

    public String getTwilioError() {
        return twilioError;
    }

    public void setTwilioError(String twilioError) {
        this.twilioError = twilioError;
    }

    public String getMessage() {
        return message;
    }
}
