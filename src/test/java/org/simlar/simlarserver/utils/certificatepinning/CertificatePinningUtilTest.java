/*
 * Copyright (C) The Simlar Authors.
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

package org.simlar.simlarserver.utils.certificatepinning;

import org.junit.Test;
import org.simlar.simlarserver.data.GooglePushServer;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;

import java.net.URI;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

@SuppressWarnings({"TestMethodWithoutAssertion", "PMD.JUnitTestsShouldIncludeAssert"})
public final class CertificatePinningUtilTest {
    @SuppressWarnings("SpellCheckingInspection")
    public static final String GOOGLE_CERTIFICATE_CHECKSUM = "hxqRlPTu1bMS/0DITB1SSu0vd4u/8l8TjPgfaAp63Gc=";

    private static void request(final URI url, final Collection<String> certificatePinnings) {
        try {
            CertificatePinningUtil.createRestTemplate(certificatePinnings).getForEntity(url, String.class);
        } catch (final HttpStatusCodeException e) {
            assertTrue(e.getStatusCode().is4xxClientError());
        }
    }

    private static void requestGoogle(final Collection<String> certificatePinnings) {
        request(URI.create(GooglePushServer.URL), certificatePinnings);
    }

    @Test
    public void testGoogleWithoutCertificatePinning() {
        requestGoogle(null);
    }

    @Test
    public void testGoogleWithCertificatePinning() {
        requestGoogle(List.of("sha256/" + GOOGLE_CERTIFICATE_CHECKSUM));
    }

    @Test
    public void testGoogleWithInvalidCertificatePinning() {
        final String message = assertThrows(ResourceAccessException.class, () ->
                requestGoogle(List.of("sha256/InvalidCertificateChecksum="))
        ).getMessage();

        assertTrue(message.contains(GOOGLE_CERTIFICATE_CHECKSUM));
    }
}
