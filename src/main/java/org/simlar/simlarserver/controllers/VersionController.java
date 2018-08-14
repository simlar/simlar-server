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

package org.simlar.simlarserver.controllers;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.simlar.simlarserver.services.settingsservice.SettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.logging.Logger;

@RestController
final class VersionController {
    public static final String REQUEST_URL_VERSION = "/version";
    private static final Logger LOGGER = Logger.getLogger(VersionController.class.getName());

    private final SettingsService settingsService;


    @Autowired
    private VersionController(final SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    /**
     * This method handles http post requests. You may test it with:
     * <blockquote>
     * curl http://localhost:8080/version
     * </blockquote>
     *
     * @return plain version string
     */
    @SuppressFBWarnings("URV_UNRELATED_RETURN_VALUES")
    @RequestMapping(value = REQUEST_URL_VERSION, produces = MediaType.TEXT_PLAIN_VALUE)
    public Object getVersion() {
        final String version = settingsService.getVersion();
        LOGGER.info(REQUEST_URL_VERSION + " requested with version=\"" + version + '\"');
        return version + '\n';
    }
}
