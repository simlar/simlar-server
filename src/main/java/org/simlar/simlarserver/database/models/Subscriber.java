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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.codec.digest.DigestUtils;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "subscriber",
        uniqueConstraints = @UniqueConstraint(name = "account_idx", columnNames = {"username", "domain"}),
        indexes = @Index(name = "username_idx", columnList = "username"))
public class Subscriber {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, columnDefinition = "int(10)")
    private Long id;

    @Column(nullable = false, length = 64)
    private String username;

    @Column(nullable = false, length = 64)
    private String domain;

    @Column(nullable = false, length = 64)
    private String password;

    @Column(nullable = false, length = 128)
    private String ha1;

    @Column(nullable = false, length = 128)
    private String ha1b;

    @SuppressWarnings("UnnecessaryThis")
    public Subscriber(final String username, final String domain, final String password) {
        this.username     = username;
        this.domain       = domain;
        this.password     = password;
        this.ha1          = createHashHa1(username, domain, password);
        this.ha1b         = createHashHa1b(username, domain, password);
    }

    @SuppressFBWarnings("WEAK_MESSAGE_DIGEST_MD5") /// We share the subscriber table with kamailio, and it defines md5.
    static String createHashHa1(final String username, final String domain, final String password) {
        return DigestUtils.md5Hex(username + ':' + domain + ':' + password);
    }

    @SuppressFBWarnings("WEAK_MESSAGE_DIGEST_MD5") /// We share the subscriber table with kamailio, and it defines md5.
    static String createHashHa1b(final String username, final String domain, final String password) {
        return DigestUtils.md5Hex(username + '@' + domain + ':' + domain + ':' + password);
    }
}
