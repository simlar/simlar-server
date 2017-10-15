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

package org.simlar.simlarserver.database.models;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@SuppressWarnings("FieldCanBeLocal")
@SuppressFBWarnings("FCBL_FIELD_COULD_BE_LOCAL")
@Data
@Entity
@Table(name = "subscriber",
       uniqueConstraints = @UniqueConstraint(name = "account_idx", columnNames = {"username", "domain"}),
       indexes = @Index(name = "username_idx", columnList = "username"))
public class Subscriber {

    @SuppressWarnings("FieldHasSetterButNoGetter")
    @Id
    @GeneratedValue
    @Column(nullable = false, columnDefinition = "int(10) unsigned")
    private Long   id;

    @Column(nullable = false, length = 64)
    private String username;

    @Column(nullable = false, length = 64)
    private String domain;

    @Column(nullable = false, length = 25)
    private String password;

    @Column(nullable = false, length = 64, name = "email_address")
    private String emailAddress;

    @Column(nullable = false, length = 64)
    private String ha1;

    @Column(nullable = false, length = 64)
    private String ha1b;

    @Column(length = 64)
    private String rpid;

    @SuppressWarnings("unused")
    protected Subscriber() {
        // no-args constructor required by JPA spec
        // this one is protected since it shouldn't be used directly
    }

    @SuppressWarnings({"SameParameterValue", "ConstructorWithTooManyParameters"})
    public Subscriber(final String username, final String domain, final String password, final String emailAddress, final String ha1, final String ha1b) {
        this.username = username;
        this.domain = domain;
        this.password = password;
        this.emailAddress = emailAddress;
        this.ha1 = ha1;
        this.ha1b = ha1b;
    }
}
