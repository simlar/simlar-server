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

package org.simlar.simlarserver.controllers;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.simlar.simlarserver.database.models.SimlarPushNotification;
import org.simlar.simlarserver.database.repositories.PushNotificationsRepository;
import org.simlar.simlarserver.testdata.TestUser;
import org.simlar.simlarserver.xml.XmlError;
import org.simlar.simlarserver.xml.XmlSuccessPushNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.junit.Assert.*;

@SuppressFBWarnings("PRMC_POSSIBLY_REDUNDANT_METHOD_CALLS")
@RunWith(SpringJUnit4ClassRunner.class)
public final class PushNotificationsControllerTest extends BaseControllerTest {
    @SuppressWarnings("SpellCheckingInspection")
    private static final String ANDROID_PUSH_ID = "APAB1bE6JDTGtpHlM4b8K4Z4qst214PdaiZs5rhfk03iFhnwz6wVgMJN01l2homL7gBeE7EuC8ohfxqrDYgkknPY1VurG-5zzuiWQmgrhjhaptOC2LlQi2g9o7aG5gPP7ZmVWyLEL6DrZwN52OvB0egGu5fN3PDKAw";

    @Autowired
    private PushNotificationsRepository pushNotificationsRepository;

    @SuppressWarnings("unchecked")
    private <T> T requestStorePushId(final Class<T> responseClass, final String login, final String password, final int deviceType, final String pushId) {
        final MultiValueMap<String, String> parameter = new LinkedMultiValueMap<>();
        parameter.add("login", login);
        parameter.add("password", password);
        parameter.add("deviceType", String.valueOf(deviceType));
        parameter.add("pushId", pushId);

        return postRequest(responseClass, PushNotificationsController.REQUEST_URL_STORE_PUSH_ID, parameter);
    }

    private void loginWithWrongCredentials(final String username, final String password) {
        final XmlError error = requestStorePushId(XmlError.class, username, password, 1, ANDROID_PUSH_ID);
        assertNotNull(error);
        assertEquals(10, error.getId());
    }

    @Test
    public void loginWithWrongCredentials() {
        loginWithWrongCredentials(null, "xxxxxxx");
        loginWithWrongCredentials("*", "xxxxxxx");
        loginWithWrongCredentials(TestUser.get(0).getSimlarId(), null);
        loginWithWrongCredentials(TestUser.get(0).getSimlarId(), "xxxxxxx");
    }

    @Test
    public void storePushId() {
        final int deviceType = 1;
        final XmlSuccessPushNotification response = requestStorePushId(XmlSuccessPushNotification.class, TestUser.get(0).getSimlarId(), TestUser.get(0).getPasswordHash(), deviceType, ANDROID_PUSH_ID);
        assertNotNull(response);
        assertEquals(deviceType, response.getDeviceType());
        assertEquals(ANDROID_PUSH_ID, response.getPushId());
        final SimlarPushNotification notification = pushNotificationsRepository.findBySimlarId(TestUser.get(0).getSimlarId());
        assertNotNull(notification);
        assertEquals(deviceType, notification.getDeviceType().toInt());
        assertEquals(ANDROID_PUSH_ID, notification.getPushId());
    }
}
