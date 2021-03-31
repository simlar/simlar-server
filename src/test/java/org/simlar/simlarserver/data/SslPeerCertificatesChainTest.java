/*
 * Copyright (C) 2021 The Simlar Authors.
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

package org.simlar.simlarserver.data;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Handshake;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.web.client.HttpStatusCodeException;

import java.io.IOException;
import java.net.URI;
import java.security.cert.Certificate;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

@Slf4j
public final class SslPeerCertificatesChainTest {
    private static final class PeerCertificatesInterceptor implements Interceptor {
        @Nullable
        private List<Certificate> peerCertificates;

        @NotNull
        @Override
        public Response intercept(@NotNull final Chain chain) throws IOException {
            final Response response = chain.proceed(chain.request());
            final Handshake handshake = response.handshake();

            peerCertificates = handshake == null
                    ? null
                    : handshake.peerCertificates();

            return response;
        }

        public List<Certificate> getPeerCertificates() {
            return peerCertificates == null
                    ? Collections.emptyList()
                    : Collections.unmodifiableList(peerCertificates);
        }
    }

    private static List<Certificate> requestPeerCertificates(final URI url) {
        final PeerCertificatesInterceptor interceptor = new PeerCertificatesInterceptor();

        try {
            new RestTemplateBuilder()
                    .requestFactory(() -> new OkHttp3ClientHttpRequestFactory(
                            new OkHttpClient.Builder().addInterceptor(interceptor).build()
                    ))
                    .build()
                    .getForEntity(url, String.class);
        } catch (final HttpStatusCodeException e) {
            log.info("url '{}' ignored error '{}'", url, e.getMessage());
        }

        return interceptor.getPeerCertificates();
    }

    @Test
    public void testAppleProduction() {
        assertEquals(3, requestPeerCertificates(URI.create("https://" + ApplePushServer.PRODUCTION.getBaseUrl())).size());
    }

    @Test
    public void testAppleDevelopment() {
        assertEquals(3, requestPeerCertificates(URI.create("https://" + ApplePushServer.SANDBOX.getBaseUrl())).size());
    }
}
