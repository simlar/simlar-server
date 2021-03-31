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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Handshake;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.web.client.HttpStatusCodeException;

import java.io.IOException;
import java.net.URI;
import java.security.Principal;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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

    private static final class ReadableCertificate {
        private final X509Certificate x509Certificate;

        @SuppressFBWarnings("WEM_WEAK_EXCEPTION_MESSAGING")
        ReadableCertificate(final Certificate certificate) {
            if (certificate == null) {
                throw new IllegalArgumentException("certificate null");
            }

            if (!(certificate instanceof X509Certificate)) {
                throw new IllegalArgumentException("certificate of unknown type '" + certificate.getType() + '\'');
            }

            x509Certificate = (X509Certificate) certificate;
        }

        @Nullable
        public String getSubject() {
            final Principal subject = x509Certificate.getSubjectDN();
            return subject == null
                    ? null
                    : subject.getName();
        }

        public List<String> getSubjectAlternativeNames() {
            try {
                final Collection<List<?>> alternativeNames = x509Certificate.getSubjectAlternativeNames();
                return alternativeNames == null
                        ? Collections.emptyList()
                        : alternativeNames.stream()
                            .map(an -> an.get(1).toString())
                            .collect(Collectors.toUnmodifiableList());
            } catch (final CertificateParsingException e) {
                log.error("error parsing certificate '{}'", x509Certificate, e);
                return Collections.emptyList();
            }
        }

        @Nullable
        public String getPublicKeySha256() {
            final PublicKey publicKey = x509Certificate.getPublicKey();
            return publicKey == null
                    ? null
                    : Base64.encodeBase64String(DigestUtils.sha256(publicKey.getEncoded()));
        }

        @Nullable
        public Instant getNotBefore() {
            return toInstant(x509Certificate.getNotBefore());
        }

        @Nullable
        public Instant getNotAfter() {
            return toInstant(x509Certificate.getNotAfter());
        }

        @Nullable
        private static Instant toInstant(@SuppressWarnings("UseOfObsoleteDateTimeApi") final Date date) {
            return date == null
                    ? null
                    : date.toInstant();
        }
    }

    private static List<ReadableCertificate> requestReadablePeerCertificates(final URI url) {
        return requestPeerCertificates(url).stream()
                .map(ReadableCertificate::new)
                .collect(Collectors.toUnmodifiableList());
    }

    private static List<ReadableCertificate> requestAndLogReadablePeerCertificates(final URI url) {
        final List<ReadableCertificate> certificates = requestReadablePeerCertificates(url);
        log.info("peer certificates for url '{}'\n{}", url,
                String.join("%n%n", certificates.stream().map(certificate ->
                        String.format("Subject: '%s'%n" +
                                        "SubjectAlternativeNames: '%s'%n" +
                                        "sha256: '%s'%n" +
                                        "valid from '%s' till '%s'",
                                certificate.getSubject(),
                                String.join("', '", certificate.getSubjectAlternativeNames()),
                                certificate.getPublicKeySha256(),
                                certificate.getNotBefore(), certificate.getNotAfter())
                ).collect(Collectors.toUnmodifiableList())));
        return certificates;
    }

    @Test
    public void testAppleProduction() {
        final List<ReadableCertificate> certificates = requestAndLogReadablePeerCertificates(URI.create(
                "https://" + ApplePushServer.PRODUCTION.getBaseUrl()));

        assertEquals(3, certificates.size());
        assertEquals("tc+C1H75gj+ap48SMYbFLoh56oSw+CLJHYPgQnm3j9U=", certificates.get(1).getPublicKeySha256());
    }

    @Test
    public void testAppleDevelopment() {
        final List<ReadableCertificate> certificates = requestAndLogReadablePeerCertificates(URI.create(
                "https://" + ApplePushServer.SANDBOX.getBaseUrl()));

        assertEquals(3, certificates.size());
        assertEquals("1CC6SL5QjEUUEr5JiV4Zw8QxiSkGVmp2CRJ4mm1IhKU=", certificates.get(1).getPublicKeySha256());
    }
}
