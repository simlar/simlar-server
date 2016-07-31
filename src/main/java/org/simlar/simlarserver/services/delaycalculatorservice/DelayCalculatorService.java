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

package org.simlar.simlarserver.services.delaycalculatorservice;

import org.simlar.simlarserver.database.models.ContactsRequestCount;
import org.simlar.simlarserver.database.repositories.ContactsRequestCountRepository;
import org.simlar.simlarserver.utils.SimlarId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Component
public final class DelayCalculatorService {
    private static final long RESET_COUNTER_MILLISECONDS = 1000 * 60 * 60 * 24; // reset counter after one day

    private final ContactsRequestCountRepository contactsRequestCountRepository;

    @Autowired
    public DelayCalculatorService(final ContactsRequestCountRepository contactsRequestCountRepository) {
        this.contactsRequestCountRepository = contactsRequestCountRepository;
    }

    int calculateRequestDelay(final SimlarId simlarId, final Collection<SimlarId> contacts) {
        return calculateDelay(calculateTotalRequestedContacts(simlarId, contacts, new Date()));
    }

    int calculateTotalRequestedContacts(final SimlarId simlarId, final Collection<SimlarId> contacts, final Date now) {
        final List<SimlarId> sortedContacts = SimlarId.sortAndUnifySimlarIds(contacts);
        return calculateTotalRequestedContacts(simlarId, now, SimlarId.hashSimlarIds(sortedContacts), sortedContacts.size());
    }

    /// TODO: transaction
    private int calculateTotalRequestedContacts(final SimlarId simlarId, final Date now, final String hash, final int count) {
        final ContactsRequestCount saved = contactsRequestCountRepository.findBySimlarId(simlarId.get());
        final int totalCount = calculateTotalRequestedContactsStatic(saved, now, hash, count);
        contactsRequestCountRepository.save(new ContactsRequestCount(simlarId, new Timestamp(now.getTime()), hash, totalCount));
        return totalCount;
    }

    private static int calculateTotalRequestedContactsStatic(final ContactsRequestCount saved, final Date now, final String hash, final int count) {
        if (saved == null) {
            return count;
        }

        //TODO: Think about time in db
        final boolean enoughTimeElapsed =  now.getTime() - saved.getTimestamp().getTime() > RESET_COUNTER_MILLISECONDS;

        return calculateTotalRequestedContactsStatic(enoughTimeElapsed, hash.equals(saved.getHash()), saved.getCount(), count);
    }

    private static int calculateTotalRequestedContactsStatic(final boolean enoughTimeElapsed, final boolean hashIsEqual, final int savedCount, final int count) {
        if (enoughTimeElapsed) {
            return count;
        }

        if (hashIsEqual) {
            return Math.max(savedCount, count);
        }

        return savedCount + count;
    }

    static int calculateDelay(final int requestedContacts) {
        if (requestedContacts < 0) {
            return Integer.MAX_VALUE;
        }

        return (int)(Math.pow(requestedContacts / 4096d, 4) / 4);
    }
}
