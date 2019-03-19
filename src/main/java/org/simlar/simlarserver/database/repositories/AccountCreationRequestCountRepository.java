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

package org.simlar.simlarserver.database.repositories;

import org.simlar.simlarserver.database.models.AccountCreationRequestCount;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import javax.persistence.LockModeType;
import java.time.Instant;

@SuppressWarnings({"unused", "InterfaceNeverImplemented", "MethodReturnAlwaysConstant"})
public interface AccountCreationRequestCountRepository extends CrudRepository<AccountCreationRequestCount, Integer> {
    AccountCreationRequestCount findBySimlarId(final String simlarId);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("FROM AccountCreationRequestCount WHERE simlarId = ?1")
    AccountCreationRequestCount findBySimlarIdForUpdate(final String simlarId);

    @Query("SELECT SUM(requestTries) FROM AccountCreationRequestCount WHERE ip = ?1 AND timestamp >= ?2")
    Integer sumRequestTries(final String ip, final Instant timestamp);

    @Query("SELECT SUM(requestTries) FROM AccountCreationRequestCount WHERE timestamp >= ?1")
    Integer sumRequestTries(final Instant timestamp);

    @Query("SELECT SUM(requestTries) FROM AccountCreationRequestCount WHERE simlarId like ?1 AND timestamp >= ?2")
    Integer sumRequestTriesForRegion(final String likeSimlarId, final Instant timestamp);
}
