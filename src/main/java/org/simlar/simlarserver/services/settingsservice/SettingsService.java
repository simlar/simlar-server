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

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@SuppressWarnings("PMD.AvoidUsingShortType")
@Getter
@Component
public final class SettingsService {
    private final String domain;
    private final short port;
    private final String version;

    @Autowired // fix IntelliJ inspection warning unused
    public SettingsService(
            @Value("${domain:}") final String                                      domain,
            @Value("${port:6161}") final short                                     port,
            @Value("${info.app.version:}") final String                            version
            ) {
        this.domain                                      = domain;
        this.port                                        = port;
        this.version                                     = version;
    }
}
