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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.simlar.simlarserver.database.models.ContactsRequestCount;
import org.simlar.simlarserver.database.repositories.ContactsRequestCountRepository;
import org.simlar.simlarserver.utils.SimlarId;
import org.simlar.simlarserver.xmlerrorexception.XmlErrorException;
import org.simlar.simlarserver.xmlerrorexception.XmlErrorExceptionRequestedTooManyContacts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public final class DelayCalculatorService {
    private static final Logger LOGGER = Logger.getLogger(DelayCalculatorService.class.getName());

    private static final long RESET_COUNTER_MILLISECONDS = 1000 * 60 * 60 * 24; // reset counter after one day
    private static final int DELAY_LIMIT = 8;

    private final ContactsRequestCountRepository contactsRequestCountRepository;
    private final TransactionTemplate transactionTemplate;

    @Autowired
    public DelayCalculatorService(final ContactsRequestCountRepository contactsRequestCountRepository, final PlatformTransactionManager transactionManager) {
        this.contactsRequestCountRepository = contactsRequestCountRepository;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @SuppressFBWarnings("LEST_LOST_EXCEPTION_STACK_TRACE")
    public void delayRequest(final SimlarId simlarId, final Collection<SimlarId> contacts) throws XmlErrorException {
        final int delay = calculateRequestDelay(simlarId, contacts);
        if (delay > DELAY_LIMIT) {
            throw new XmlErrorExceptionRequestedTooManyContacts("request delay=" + delay + " blocking simlarId=" + simlarId);
        }

        if (delay <= 0) {
            return;
        }


        LOGGER.info("request delay=" + delay + " blocking simlarId=" + simlarId);
        // TODO: think about
        try {
            Thread.sleep(delay * 1000L);
        } catch (final InterruptedException e) {
            LOGGER.log(Level.SEVERE, "InterruptedException simlarId=" + simlarId + " delay=" + delay + "seconds", e);
            throw new XmlErrorExceptionRequestedTooManyContacts("interrupted request delay=" + delay + " blocking simlarId=" + simlarId);
        }
    }

    int calculateRequestDelay(final SimlarId simlarId, final Collection<SimlarId> contacts) {
        return calculateDelay(calculateTotalRequestedContacts(simlarId, contacts, new Date()));
    }

    int calculateTotalRequestedContacts(final SimlarId simlarId, final Collection<SimlarId> contacts, final Date now) {
        final List<SimlarId> sortedContacts = SimlarId.sortAndUnifySimlarIds(contacts);
        final Integer count = calculateTotalRequestedContacts(simlarId, now, SimlarId.hashSimlarIds(sortedContacts), sortedContacts.size());
        return count == null ? Integer.MAX_VALUE : count;
    }

    private Integer calculateTotalRequestedContacts(final SimlarId simlarId, final Date now, final String hash, final int count) {
        return transactionTemplate.execute(status -> {
            final ContactsRequestCount saved = contactsRequestCountRepository.findBySimlarId(simlarId.get());
            final int totalCount = calculateTotalRequestedContactsStatic(saved, now, hash, count);
            contactsRequestCountRepository.save(new ContactsRequestCount(simlarId, new Timestamp(now.getTime()), hash, totalCount));
            return totalCount;
        });
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
