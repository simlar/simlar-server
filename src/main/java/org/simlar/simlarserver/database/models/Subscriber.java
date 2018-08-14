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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "subscriber", uniqueConstraints = { @UniqueConstraint(columnNames = { "username", "domain" }) })
public class Subscriber {
    @Id
    @GeneratedValue
    private Long   id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String domain;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String email_address;

    @Column(nullable = false)
    private String ha1;

    @Column(nullable = false)
    private String ha1b;

    @Column(nullable = true)
    private String rpid;

    protected Subscriber() {
        // no-args constructor required by JPA spec
        // this one is protected since it shouldn't be used directly
    }

    @SuppressWarnings("SameParameterValue")
    public Subscriber(final String username, final String domain, final String password, final String email_address, final String ha1, final String ha1b) {
        this.username = username;
        this.domain = domain;
        this.password = password;
        this.email_address = email_address;
        this.ha1 = ha1;
        this.ha1b = ha1b;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(final String domain) {
        this.domain = domain;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public String getEmail_address() {
        return email_address;
    }

    public void setEmail_address(final String email_address) {
        this.email_address = email_address;
    }

    public String getHa1() {
        return ha1;
    }

    public void setHa1(final String ha1) {
        this.ha1 = ha1;
    }

    public String getHa1b() {
        return ha1b;
    }

    public void setHa1b(final String ha1b) {
        this.ha1b = ha1b;
    }

    public String getRpid() {
        return rpid;
    }

    public void setRpid(final String rpid) {
        this.rpid = rpid;
    }
}
