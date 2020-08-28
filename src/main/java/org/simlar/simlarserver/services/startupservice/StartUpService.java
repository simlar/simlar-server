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

package org.simlar.simlarserver.services.startupservice;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.simlar.simlarserver.services.SharedSettings;
import org.simlar.simlarserver.services.subscriberservice.SubscriberService;
import org.simlar.simlarserver.services.versionservice.VersionService;
import org.simlar.simlarserver.testdata.TestUser;
import org.simlar.simlarserver.utils.SimlarId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateProperties;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.support.DatabaseMetaDataCallback;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.MetaDataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import javax.sql.DataSource;
import java.sql.DatabaseMetaData;

@Slf4j
@Component
final class StartUpService {
    private final VersionService    versionService;
    private final SharedSettings    sharedSettings;
    private final SubscriberService subscriberService;
    private final String            hibernateDdlAuto;
    private final String            datasourceUrl;
    private final String            databaseProduct;

    @Autowired // fix IntelliJ inspection warning unused
    private StartUpService(final VersionService versionService, final SharedSettings sharedSettings, final SubscriberService subscriberService, final HibernateProperties hibernateProperties, final DataSource dataSource) {
        this.versionService    = versionService;
        this.sharedSettings    = sharedSettings;
        this.subscriberService = subscriberService;
        hibernateDdlAuto       = hibernateProperties.getDdlAuto();

        //noinspection NullableProblems
        datasourceUrl          = (String) extractDatabaseMetaData(dataSource, DatabaseMetaData::getURL);
        //noinspection NullableProblems
        databaseProduct = String.format("%s %s",
                extractDatabaseMetaData(dataSource, DatabaseMetaData::getDatabaseProductName),
                extractDatabaseMetaData(dataSource, DatabaseMetaData::getDatabaseProductVersion));
    }

    private static Object extractDatabaseMetaData(final DataSource dataSource, final DatabaseMetaDataCallback action) {
        try {
            return JdbcUtils.extractDatabaseMetaData(dataSource, action);
        } catch (final MetaDataAccessException e) {
            log.error("failed to extract database metadata", e);
            return null;
        }
    }

    private void createTestData() {
        for (final TestUser user : TestUser.values()) {
            final SimlarId simlarId = SimlarId.create(user.getSimlarId());
            if (simlarId != null) {
                subscriberService.save(simlarId, user.getPassword());
                log.info("added test user: {}", user.getSimlarId());
            }
        }
    }

    @SuppressWarnings("unused")
    @EventListener
    public void handleApplicationReadyEvent(final ApplicationReadyEvent event) {
        log.info("started on domain='{}', hibernateDdlAuto='{}', dataSource='{}', databaseProduct='{}' and version='{}'", sharedSettings.getDomain(), hibernateDdlAuto, datasourceUrl, databaseProduct, versionService.getVersion());

        if (event.getApplicationContext() instanceof WebApplicationContext && ("create-drop".equals(hibernateDdlAuto) || StringUtils.contains(datasourceUrl, "h2:mem"))) {
            createTestData();
        }
    }
}
