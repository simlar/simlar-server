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
import org.simlar.simlarserver.database.models.PushNotification;
import org.simlar.simlarserver.database.repositories.PushNotificationsRepository;
import org.simlar.simlarserver.testdata.TestUser;
import org.simlar.simlarserver.xml.XmlError;
import org.simlar.simlarserver.xml.XmlSuccessPushNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@SuppressFBWarnings("PRMC_POSSIBLY_REDUNDANT_METHOD_CALLS")
@RunWith(SpringRunner.class)
public final class PushNotificationsControllerTest extends BaseControllerTest {
    @SuppressWarnings("SpellCheckingInspection")
    private static final String ANDROID_PUSH_ID_1 = "APAB1bE6JDTGtpHlM4b8K4Z4qst214PdaiZs5rhfk03iFhnwz6wVgMJN01l2homL7gBeE7EuC8ohfxqrDYgkknPY1VurG-5zzuiWQmgrhjhaptOC2LlQi2g9o7aG5gPP7ZmVWyLEL6DrZwN52OvB0egGu5fN3PDKAw";
    @SuppressWarnings("SpellCheckingInspection")
    private static final String ANDROID_PUSH_ID_2 = "APA91bHpBzJWqeBkFyEwnzeISsYN8I7ni_aMn8xthy-0Y_MSVrs5wPHzJfmldK8LkkoEmeu0-Ud2rvDri2pdcuhH89-vhTd_Fw7gF5HB6YnXyYfWruLPeJU";
    @SuppressWarnings("SpellCheckingInspection")
    private static final String IOS_PUSH_ID       = "7fd224670ab46d041e08101cd2bc3a5646c252a1dd5bfcb02f667203338f89a9";

    @Autowired
    private PushNotificationsRepository pushNotificationsRepository;

    @SuppressWarnings("unchecked")
    private <T> T requestStorePushId(final Class<T> responseClass, final String login, final String password, final int deviceType, final String pushId) {
        return postRequest(responseClass, PushNotificationsController.REQUEST_PATH, createParameters(new String[][] {
                { "login", login },
                { "password", password },
                { "deviceType", String.valueOf(deviceType) },
                { "pushId", pushId }
        }));
    }

    private <T> T requestStorePushId(final Class<T> responseClass, final TestUser user, final int deviceType, final String pushId) {
        return requestStorePushId(responseClass, user.getSimlarId(), user.getPasswordHash(), deviceType, pushId);
    }

    private void loginWithWrongCredentials(final String username, final String password) {
        final XmlError error = requestStorePushId(XmlError.class, username, password, 1, ANDROID_PUSH_ID_1);
        assertNotNull(error);
        assertEquals(10, error.getId());
    }

    @Test
    public void testLoginWithWrongCredentials() {
        loginWithWrongCredentials(null, "xxxxxxx");
        loginWithWrongCredentials("*", "xxxxxxx");
        loginWithWrongCredentials(TestUser.U1.getSimlarId(), null);
        loginWithWrongCredentials(TestUser.U1.getSimlarId(), "xxxxxxx");
    }

    private void storePushId(final TestUser testUser, final int deviceType, final String pushId) {
        final XmlSuccessPushNotification response = requestStorePushId(XmlSuccessPushNotification.class, testUser, deviceType, pushId);
        assertNotNull(response);
        assertEquals(deviceType, response.getDeviceType());
        assertEquals(pushId, response.getPushId());
        final PushNotification notification = pushNotificationsRepository.findBySimlarId(testUser.getSimlarId());
        assertNotNull(notification);
        assertEquals(deviceType, notification.getDeviceType().toInt());
        assertEquals(pushId, notification.getPushId());
    }

    @Test
    public void testStorePushId() {
        storePushId(TestUser.U1, 1, ANDROID_PUSH_ID_1);
        storePushId(TestUser.U1, 1, ANDROID_PUSH_ID_2);
        storePushId(TestUser.U2, 1, ANDROID_PUSH_ID_1);
        storePushId(TestUser.U2, 1, ANDROID_PUSH_ID_2);
        storePushId(TestUser.U2, 5, IOS_PUSH_ID);
    }

    private void unknownDeviceType(final int deviceType) {
        final XmlError error = requestStorePushId(XmlError.class, TestUser.U1, deviceType, ANDROID_PUSH_ID_1);
        assertNotNull(error);
        assertEquals(30, error.getId());
    }

    @Test
    public void testUnknownDeviceType() {
        unknownDeviceType(-1);
        unknownDeviceType(0);
        unknownDeviceType(6);
        unknownDeviceType(5400);
    }

    private void unknownApplePushId(final int deviceType, final String pushId) {
        final XmlError error = requestStorePushId(XmlError.class, TestUser.U1, deviceType, pushId);
        assertNotNull(error);
        assertEquals(31, error.getId());
    }

    @Test
    public void testUnknownApplePushId() {
        unknownApplePushId(2, ANDROID_PUSH_ID_1);
        unknownApplePushId(3, ANDROID_PUSH_ID_2);
        unknownApplePushId(4, ANDROID_PUSH_ID_2);
        unknownApplePushId(5, ANDROID_PUSH_ID_1);
    }
}
