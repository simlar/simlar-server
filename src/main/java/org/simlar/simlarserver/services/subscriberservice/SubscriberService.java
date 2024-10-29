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

package org.simlar.simlarserver.services.subscriberservice;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.simlar.simlarserver.database.models.Subscriber;
import org.simlar.simlarserver.database.repositories.SubscriberRepository;
import org.simlar.simlarserver.services.SharedSettings;
import org.simlar.simlarserver.utils.SimlarId;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorWrongCredentialsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

@Slf4j
@Component
public final class SubscriberService {
    private final SharedSettings sharedSettings;
    private final SubscriberRepository subscriberRepository;
    private final TransactionTemplate transactionTemplate;

    @Autowired // fix IntelliJ inspection warning unused
    private SubscriberService(final SharedSettings sharedSettings, final SubscriberRepository subscriberRepository, final PlatformTransactionManager transactionManager) {
        this.sharedSettings = sharedSettings;
        this.subscriberRepository = subscriberRepository;
        transactionTemplate = new TransactionTemplate(transactionManager);
    }

    public void save(final SimlarId simlarId, final String password) {
        if (simlarId == null || StringUtils.isEmpty(password)) {
            throw new IllegalArgumentException("simlarId=" + simlarId + " password=" + password);
        }

        final Subscriber subscriber = new Subscriber(simlarId.get(), sharedSettings.domain(), password);

        //noinspection AnonymousInnerClass
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @SuppressWarnings("PMD.AccessorMethodGeneration")
            @Override
            protected void doInTransactionWithoutResult(@NonNull final TransactionStatus status) {
                subscriber.setId(findSubscriberId(simlarId));
                subscriberRepository.save(subscriber);
            }
        });
    }

    @SuppressFBWarnings("UPM_UNCALLED_PRIVATE_METHOD") // false positive
    private Long findSubscriberId(final SimlarId simlarId) {
        final List<Long> ids = subscriberRepository.findIdByUsernameAndDomain(simlarId.get(), sharedSettings.domain());
        if (CollectionUtils.isEmpty(ids)) {
            return null;
        }

        if (ids.size() > 1) {
            log.error("found more than 1 subscriber for simlarID={}", simlarId);
        }

        return ids.get(0);
    }

    public boolean isRegistered(final SimlarId simlarId) {
        return simlarId != null && findSubscriberId(simlarId) != null;
    }

    public boolean checkCredentials(final String simlarId, final String ha1) {
        return checkCredentials(SimlarId.create(simlarId), ha1);
    }

    private boolean checkCredentials(final SimlarId simlarId, @SuppressWarnings("TypeMayBeWeakened") final String ha1) {
        if (StringUtils.isEmpty(ha1)) {
            return false;
        }

        return StringUtils.equals(ha1, getHa1(simlarId));
    }

    @Nullable
    public String getHa1(final SimlarId simlarId) {
        if (simlarId == null) {
            return null;
        }

        final List<String> savedHa1s = subscriberRepository.findHa1ByUsernameAndDomain(simlarId.get(), sharedSettings.domain());
        if (CollectionUtils.isEmpty(savedHa1s)) {
            return null;
        }

        if (savedHa1s.size() > 1) {
            log.error("found more than 1 subscriber for simlarId={}", simlarId);
        }

        return savedHa1s.get(0);
    }

    @SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
    public void checkCredentialsWithException(final String simlarId, final String ha1) {
        if (!checkCredentials(simlarId, ha1)) {
            throw new XmlErrorWrongCredentialsException("simlarId='" + simlarId + '\'');
        }
    }

    public List<SimlarId> filterSimlarIdsRegistered(final Collection<SimlarId> simlarIds) {
        return ListUtils.partition(simlarIds.stream()
                        .map(SimlarId::get)
                        .toList(), 100).stream()
                .map(HashSet::new)
                .map(usernames -> subscriberRepository.findUsernameByDomainAndUsernameIn(sharedSettings.domain(), usernames))
                .flatMap(List::stream)
                .map(SimlarId::create)
                .toList();
    }
}
