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

import org.simlar.simlarserver.utils.SimlarId;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.time.Instant;

@Entity
@Table(name = "simlar_contacts_request_count")
public final class ContactsRequestCount {
    @SuppressWarnings("FieldCanBeLocal")
    @Id
    @Column(nullable = false, length = 64)
    private String simlarId;

    @Column(nullable = false, columnDefinition = "TIMESTAMP")
    private Timestamp timestamp;

    @Column(nullable = false, length = 64)
    private String hash;

    @Column(nullable = false, columnDefinition = "int(10) unsigned")
    private int count;

    @SuppressWarnings({"unused", "ProtectedMemberInFinalClass"})
    protected ContactsRequestCount() {
        // no-args constructor required by JPA spec
        // this one is protected since it shouldn't be used directly
    }

    public ContactsRequestCount(final SimlarId simlarId, final Instant timestamp, final String hash, final int count) {
        this.simlarId = simlarId.get();
        this.timestamp = Timestamp.from(timestamp);
        this.hash = hash;
        this.count = count;
    }

    @SuppressWarnings("TypeMayBeWeakened") // Instant instead of Temporal
    public Instant getTimestamp() {
        return timestamp.toInstant();
    }

    public String getHash() {
        return hash;
    }

    public int getCount() {
        return count;
    }
}
