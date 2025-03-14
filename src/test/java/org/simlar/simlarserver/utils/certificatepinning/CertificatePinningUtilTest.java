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
import org.simlar.simlarserver.data.ApplePushServer;
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
    @SuppressWarnings({"SpellCheckingInspection", "StaticCollection"})
    public static final List<String> GOOGLE_CERTIFICATE_CHECKSUMS = List.of("sha256/zCTnfLwLKbS9S2sbp+uFz4KZOocFvXxkV06Ce9O5M2w=", "sha256/hxqRlPTu1bMS/0DITB1SSu0vd4u/8l8TjPgfaAp63Gc=", "sha256/vh78KSg1Ry4NaqGDV10w/cTb9VH3BQUZoCWNa93W/EY=", "sha256/YPtHaftLw6/0vnc2BnNKGF54xiCA28WFcccjkA4ypCM=", "sha256/InvalidCertificateChecksum=");
    @SuppressWarnings({"SpellCheckingInspection", "StaticCollection"})
    public static final List<String> APPLE_CERTIFICATE_CHECKSUMS = List.of("sha256/1CC6SL5QjEUUEr5JiV4Zw8QxiSkGVmp2CRJ4mm1IhKU=", "sha256/piwIbt4Hf3u1n9lkgUEzu3UlcXomKKirJ0NmgMppc04=", "sha256/xXDXs35ozcjwk0JgHOmIO+JXQxta1bb9rQ14FVEqdWE=", "sha256/InvalidCertificateChecksum=");

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
        requestGoogle(GOOGLE_CERTIFICATE_CHECKSUMS);
    }

    @Test
    public void testGoogleWithInvalidCertificatePinning() {
        final String message = assertThrows(ResourceAccessException.class, () ->
                requestGoogle(List.of("sha256/InvalidCertificateChecksum="))
        ).getMessage();

        assertTrue(message, GOOGLE_CERTIFICATE_CHECKSUMS.stream().anyMatch(message::contains));
    }

    private static void requestAppleProduction(final Collection<String> certificatePinnings) {
        request(URI.create("https://" + ApplePushServer.PRODUCTION.getBaseUrl()), certificatePinnings);
    }

    private static void requestAppleDevelopment(final Collection<String> certificatePinnings) {
        request(URI.create("https://" + ApplePushServer.SANDBOX.getBaseUrl()), certificatePinnings);
    }

    @Test
    public void testAppleWithoutCertificatePinning() {
        requestAppleProduction(null);
        requestAppleDevelopment(null);
    }

    @Test
    public void testAppleWithCertificatePinning() {
        requestAppleProduction(APPLE_CERTIFICATE_CHECKSUMS);
        requestAppleDevelopment(APPLE_CERTIFICATE_CHECKSUMS);
    }

    @Test
    public void testAppleProductionWithInvalidCertificatePinning() {
        final String message = assertThrows(ResourceAccessException.class, () ->
                requestAppleProduction(List.of("sha256/InvalidCertificateChecksum1=", "sha256/InvalidCertificateChecksum2="))
        ).getMessage();

        assertTrue(message, APPLE_CERTIFICATE_CHECKSUMS.stream().anyMatch(message::contains));
    }

    @Test
    public void testAppleDevelopmentWithInvalidCertificatePinning() {
        final String message = assertThrows(ResourceAccessException.class, () ->
                requestAppleDevelopment(List.of("sha256/InvalidCertificateChecksum="))
        ).getMessage();

        assertTrue(message, APPLE_CERTIFICATE_CHECKSUMS.stream().anyMatch(message::contains));
    }
}
