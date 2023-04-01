/*
 * Copyright (C) 2017 The Simlar Authors.
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
import org.hibernate.annotations.ColumnDefault;
import org.simlar.simlarserver.utils.SimlarId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;

@SuppressWarnings("ClassWithTooManyMethods")
@NoArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
@Table(name = "simlar_account_creation_request", indexes = {
        @Index(name = "request_timestamp", columnList = "timestamp"),
        @Index(name = "request_ip", columnList = "ip") })
public final class AccountCreationRequestCount {
    @Id
    @Column(nullable = false, length = 64)
    private String simlarId;

    @Column(nullable = false, length = 64)
    private String password;

    @Column(nullable = false, length = 64)
    private String registrationCode;

    @Column(nullable = false, columnDefinition = "int(10) unsigned")
    @ColumnDefault("1")
    private int requestTries;

    @Column(nullable = false, columnDefinition = "int(10) unsigned")
    @ColumnDefault("0")
    private int calls;

    @Column(nullable = false, columnDefinition = "int(10) unsigned")
    @ColumnDefault("0")
    private int confirmTries;

    @Column(nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Instant timestamp;

    @Column(nullable = false, length = 64)
    private String ip;

    @Column(columnDefinition = "TIMESTAMP NULL DEFAULT NULL")
    private Instant callTimestamp;

    @Column(nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Instant registrationCodeTimestamp;

    @SuppressWarnings("UnnecessaryThis")
    public AccountCreationRequestCount(final String simlarId, final String password, final String registrationCode, final Instant timestamp, final String ip) {
        this.simlarId                  = simlarId;
        this.password                  = password;
        this.registrationCode          = registrationCode;
        this.requestTries              = 1;
        this.confirmTries              = 0;
        this.timestamp                 = timestamp;
        this.ip                        = ip;
        //noinspection AssignmentToNull
        this.callTimestamp             = null;
        this.registrationCodeTimestamp = timestamp;
    }

    public AccountCreationRequestCount(final SimlarId simlarId, final String password, final String registrationCode, final Instant timestamp, final String ip) {
        this(simlarId.get(), password, registrationCode, timestamp, ip);
    }

    public void incrementRequestTries() {
        requestTries++;
    }

    public void incrementCalls() {
        calls++;
    }

    public void incrementConfirmTries() {
        confirmTries++;
    }
}
