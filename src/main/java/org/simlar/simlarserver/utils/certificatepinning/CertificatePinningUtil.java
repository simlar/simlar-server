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
 */

package org.simlar.simlarserver.utils.certificatepinning;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.net.http.HttpClient;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;

@SuppressWarnings("UtilityClass")
public final class CertificatePinningUtil {
    private CertificatePinningUtil() {
        throw new AssertionError("This class was not meant to be instantiated");
    }

    @SuppressFBWarnings("EXS_EXCEPTION_SOFTENING_NO_CONSTRAINTS")
    private static HttpClient createHttpClient(final Collection<String> certificatePinnings) {
        try {
            final SSLContext sslContext = SSLContext.getInstance("TLSv1.3");

            final TrustManager[] tm = { new Pinning509TrustManager(certificatePinnings) };
            sslContext.init(null, tm, null);

            return HttpClient.newBuilder()
                    .sslContext(sslContext)
                    .build();
        } catch (final NoSuchAlgorithmException | KeyManagementException e) {
            throw new CertificatePinningException("failed to create HttpClient", e);
        }
    }

    public static RestTemplate createRestTemplate(final Collection<String> certificatePinnings) {
        return new RestTemplateBuilder()
                .requestFactory(() -> new JdkClientHttpRequestFactory(createHttpClient(certificatePinnings)))
                .build();
    }
}
