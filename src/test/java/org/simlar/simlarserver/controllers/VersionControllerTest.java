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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.simlar.simlarserver.SimlarServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import static org.junit.Assert.assertEquals;

@TestPropertySource(properties = "info.app.version = " + VersionControllerTest.VERSION)
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = SimlarServer.class)
public final class VersionControllerTest {
    @SuppressWarnings("WeakerAccess")
    static final String VERSION = "0.0.7-test";

    @Value("${local.server.port}")
    private int port;

    @Test
    public void testRequestVersion() {
        assertEquals(VERSION + '\n', new RestTemplate().getForObject("http://localhost:" + port + VersionController.REQUEST_PATH, String.class));
    }
}
