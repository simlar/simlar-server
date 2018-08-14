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

package org.simlar.simlarserver.services.settingsservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Component
public class SettingsService implements ApplicationListener<ContextRefreshedEvent> {
    private static final Logger LOGGER = Logger.getLogger(SettingsService.class.getName());

    @Value("${domain:}")
    private String domain;

    @Value("${info.app.version:}")
    private String version;

    @Value("${spring.datasource.url:}")
    private String datasource;


    public String getDomain() {
        return domain;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public void onApplicationEvent(final ContextRefreshedEvent event) {
        LOGGER.info("started on domain '" + domain + "', dataSource '" + datasource + "' and version '" + version + '\'');
    }
}
