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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.simlar.simlarserver.database.models.Subscriber;
import org.simlar.simlarserver.database.repositories.SubscriberRepository;
import org.simlar.simlarserver.services.settingsservice.SettingsService;
import org.simlar.simlarserver.utils.Hash;
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

import java.util.List;

@Slf4j
@Component
public final class SubscriberService {
    private final SettingsService settingsService;
    private final SubscriberRepository subscriberRepository;
    private final TransactionTemplate transactionTemplate;

    @Autowired
    public SubscriberService(final SettingsService settingsService, final SubscriberRepository subscriberRepository, final PlatformTransactionManager transactionManager) {
        this.settingsService = settingsService;
        this.subscriberRepository = subscriberRepository;
        transactionTemplate = new TransactionTemplate(transactionManager);
    }

    public void save(final SimlarId simlarId, final String password) {
        if (simlarId == null || StringUtils.isEmpty(password)) {
            throw new IllegalArgumentException("simlarId=" + simlarId + " password=" + password);
        }

        final Subscriber subscriber = new Subscriber(simlarId.get(), settingsService.getDomain(), password, "", createHashHa1(simlarId, password),
                createHashHa1b(simlarId, password));

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

    public String createHashHa1(final SimlarId simlarId, final String password) {
        return Hash.md5(simlarId.get() + ':' + settingsService.getDomain() + ':' + password);
    }

    String createHashHa1b(final SimlarId simlarId, final String password) {
        return Hash.md5(simlarId.get() + '@' + settingsService.getDomain() + ':' + settingsService.getDomain() + ':' + password);
    }

    private Long findSubscriberId(final SimlarId simlarId) {
        final List<Long> ids = subscriberRepository.findIdByUsernameAndDomain(simlarId.get(), settingsService.getDomain());
        if (CollectionUtils.isEmpty(ids)) {
            return null;
        }

        if (ids.size() > 1) {
            log.error("found more than 1 subscriber for simlarID={}", simlarId);
        }

        return ids.get(0);
    }

    public boolean checkCredentials(final String simlarId, @SuppressWarnings("TypeMayBeWeakened") final String ha1) {
        if (!SimlarId.check(simlarId)) {
            return false;
        }

        if (StringUtils.isEmpty(ha1)) {
            return false;
        }

        final List<String> savedHa1s = subscriberRepository.findHa1ByUsernameAndDomain(simlarId, settingsService.getDomain());
        if (CollectionUtils.isEmpty(savedHa1s)) {
            return false;
        }

        if (savedHa1s.size() > 1) {
            log.error("found more than 1 subscriber for simlarID={}", simlarId);
        }

        return StringUtils.equals(ha1, savedHa1s.get(0));
    }

    @SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
    public void checkCredentialsWithException(final String simlarId, final String ha1) {
        if (!checkCredentials(simlarId, ha1)) {
            throw new XmlErrorWrongCredentialsException("simlarId='" + simlarId + '\'');
        }
    }

    public int getStatus(final SimlarId simlarId) {
        if (simlarId == null) {
            return 0;
        }

        return subscriberRepository.findHa1ByUsernameAndDomain(simlarId.get(), settingsService.getDomain()).isEmpty() ? 0 : 1;
    }
}
