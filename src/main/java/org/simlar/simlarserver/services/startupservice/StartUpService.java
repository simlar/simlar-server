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

import org.simlar.simlarserver.services.settingsservice.SettingsService;
import org.simlar.simlarserver.services.subscriberservice.SubscriberService;
import org.simlar.simlarserver.testdata.TestUser;
import org.simlar.simlarserver.utils.SimlarId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import java.util.logging.Logger;

@Component
final class StartUpService {
    private static final Logger LOGGER = Logger.getLogger(StartUpService.class.getName());

    private final SettingsService   settingsService;
    private final SubscriberService subscriberService;
    private final String            datasourceUrl;
    private final String            datasourceDriver;
    private final String            hibernateDdlAuto;

    @Autowired
    public StartUpService(final SettingsService settingsService, final SubscriberService subscriberService, final DataSourceProperties dataSourceProperties, final JpaProperties jpaProperties) {
        this.settingsService   = settingsService;
        this.subscriberService = subscriberService;
        datasourceUrl          = dataSourceProperties.getUrl();
        datasourceDriver       = dataSourceProperties.getDriverClassName();
        hibernateDdlAuto       = jpaProperties.getHibernate().getDdlAuto();
    }

    private void createTestData() {
        for (final TestUser user: TestUser.USERS) {
            final SimlarId simlarId = SimlarId.create(user.getSimlarId());
            if (simlarId != null) {
                subscriberService.save(simlarId, user.getPassword());
                LOGGER.info("added test user: " + user.getSimlarId());
            }
        }
    }

    @SuppressWarnings("unused")
    @EventListener
    public void handleApplicationReadyEvent(final ApplicationReadyEvent event) {
        LOGGER.info(
                "started on domain='" + settingsService.getDomain() +
                "', hibernateDdlAuto='" + hibernateDdlAuto +
                "', dataSource='" + datasourceUrl +
                "' and version='" + settingsService.getVersion() + '\'');

        if (event.getApplicationContext() instanceof WebApplicationContext && ("create-drop".equals(hibernateDdlAuto) || "org.h2.Driver".equals(datasourceDriver)))
        {
            createTestData();
        }
    }
}
