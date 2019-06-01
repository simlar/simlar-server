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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.simlar.simlarserver.database.models.ContactsRequestCount;
import org.simlar.simlarserver.database.repositories.ContactsRequestCountRepository;
import org.simlar.simlarserver.utils.SimlarId;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.SortedSet;

@Slf4j
@Component
public final class DelayCalculatorService {
    public static final Duration MAXIMUM = Duration.ofSeconds(Long.MAX_VALUE);

    private static final Duration RESET_COUNTER = Duration.ofDays(1); // reset counter after one day

    private final ContactsRequestCountRepository contactsRequestCountRepository;
    private final TransactionTemplate transactionTemplate;

    private DelayCalculatorService(final ContactsRequestCountRepository contactsRequestCountRepository, final PlatformTransactionManager transactionManager) {
        this.contactsRequestCountRepository = contactsRequestCountRepository;
        transactionTemplate = new TransactionTemplate(transactionManager);
    }

    public Duration calculateRequestDelay(final SimlarId simlarId, final Collection<SimlarId> contacts) {
        return calculateDelay(calculateTotalRequestedContacts(simlarId, contacts, Instant.now()));
    }

    int calculateTotalRequestedContacts(final SimlarId simlarId, final Collection<SimlarId> contacts, final Instant now) {
        final SortedSet<SimlarId> sortedContacts = SimlarId.sortAndUnifySimlarIds(contacts);
        final Integer count = calculateTotalRequestedContacts(simlarId, now, SimlarId.hashSimlarIds(sortedContacts), sortedContacts.size());
        return ObjectUtils.defaultIfNull(count, Integer.MAX_VALUE);
    }

    private Integer calculateTotalRequestedContacts(final SimlarId simlarId, final Instant now, final String hash, final int count) {
        return transactionTemplate.execute(status -> {
            final ContactsRequestCount saved = contactsRequestCountRepository.findBySimlarId(simlarId.get());

            if (saved != null && count == 1 && !StringUtils.equals(saved.getHash(), hash)) {
                saved.incrementCount();
                saved.setTimestamp(now);
                contactsRequestCountRepository.save(saved);
                return saved.getCount();
            }

            final int totalCount = calculateTotalRequestedContactsStatic(saved, now, hash, count);
            contactsRequestCountRepository.save(new ContactsRequestCount(simlarId, now, hash, totalCount));
            return totalCount;
        });
    }

    @SuppressWarnings("TypeMayBeWeakened") // Instant instead of Temporal
    private static int calculateTotalRequestedContactsStatic(final ContactsRequestCount saved, final Instant now, final String hash, final int count) {
        if (saved == null) {
            return count;
        }

        final boolean enoughTimeElapsed = Duration.between(saved.getTimestamp().plus(RESET_COUNTER), now).compareTo(Duration.ZERO) > 0;

        return calculateTotalRequestedContactsStatic(enoughTimeElapsed, StringUtils.equals(hash, saved.getHash()), saved.getCount(), count);
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

    static Duration calculateDelay(final int requestedContacts) {
        final Duration delay = calculateDelayWithoutLog(requestedContacts);
        log.info("requestedContactsCount={} -> delay={}", requestedContacts, delay);
        return delay;
    }

    private static Duration calculateDelayWithoutLog(final int requestedContacts) {
        if (requestedContacts < 0) {
            return MAXIMUM;
        }

        //noinspection NumericCastThatLosesPrecision
        return Duration.ofSeconds((long)(StrictMath.pow(requestedContacts / 4096.0d, 4) / 4));
    }
}
