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
import org.simlar.simlarserver.utils.SimlarId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@NoArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
@Table(name = "simlar_contacts_request_count")
public final class ContactsRequestCount {
    @Id
    @Column(nullable = false, length = 64)
    private String simlarId;

    @Column(nullable = false, columnDefinition = "TIMESTAMP")
    private Instant timestamp;

    @Column(nullable = false, length = 64)
    private String hash;

    @Column(nullable = false, columnDefinition = "int(10) unsigned")
    private int count;

    public ContactsRequestCount(final SimlarId simlarId, final Instant timestamp, final String hash, final int count) {
        this.simlarId = simlarId.get();
        this.timestamp = timestamp;
        this.hash = hash;
        this.count = count;
    }

    public void incrementCount() {
        count++;
    }
}
