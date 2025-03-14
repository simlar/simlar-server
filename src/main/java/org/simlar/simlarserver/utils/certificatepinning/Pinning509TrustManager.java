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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
final class Pinning509TrustManager implements X509TrustManager {
    private final X509TrustManager defaultTrustManager = createTrustManager();
    private final Set<String> certificatePinnings;

    Pinning509TrustManager(final Collection<String> certificatePinnings) {
        this.certificatePinnings = certificatePinnings == null ? Collections.emptySet() : Set.copyOf(certificatePinnings);
    }

    @SuppressFBWarnings({"WEM_WEAK_EXCEPTION_MESSAGING", "EXS_EXCEPTION_SOFTENING_NO_CONSTRAINTS"})
    private static TrustManagerFactory createTrustManagerFactory() {
        try {
            final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init((KeyStore) null);
            return trustManagerFactory;
        } catch (final NoSuchAlgorithmException | KeyStoreException e) {
            throw new CertificatePinningException("failed to create TrustManagerFactory", e);
        }
    }

    private static X509TrustManager createTrustManager() {
        final TrustManager trustManager = createTrustManagerFactory().getTrustManagers()[0];
        if (!(trustManager instanceof X509TrustManager)) {
            throw new CertificatePinningException("first trust manager of invalid type: " + trustManager.getClass().getSimpleName());
        }
        return (X509TrustManager) trustManager;
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return defaultTrustManager.getAcceptedIssuers();
    }

    @Override
    public void checkClientTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
        defaultTrustManager.checkClientTrusted(chain, authType);
    }

    @SuppressFBWarnings("WEM_WEAK_EXCEPTION_MESSAGING")
    @Override
    public void checkServerTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
        defaultTrustManager.checkServerTrusted(chain, authType);

        if (certificatePinnings.isEmpty()) {
            log.warn("no certificates for pinning configured");
            return;
        }

        if (chain == null || chain.length == 0) {
            throw new CertificateException("certificate chain is empty");
        }

        //noinspection BooleanVariableAlwaysNegated
        final boolean certificateMatch = Arrays.stream(chain).anyMatch(x509Certificate -> certificatePinnings.contains("sha256/" + getPublicKeySha256(x509Certificate)));
        if (!certificateMatch) {
            throw new CertificateException("Certificate pinning failure! None of the following certificates matches:\n" +
                    Arrays.stream(chain).map(x509Certificate -> String.format("  'sha256/%s' issuer: '%s' validFrom: '%s' validTil: '%s'%n",
                            getPublicKeySha256(x509Certificate),
                            x509Certificate.getIssuerX500Principal(),
                            formatDate(x509Certificate.getNotBefore()),
                            formatDate(x509Certificate.getNotAfter())
                    )).collect(Collectors.joining())
            );
        }
    }

    @SuppressWarnings("UseOfObsoleteDateTimeApi")
    private static String formatDate(final Date date) {
        if (date == null) {
            return null;
        }

        return new SimpleDateFormat("yyyy/MM/dd HH:mm:ss z", Locale.US).format(date);
    }

    private static String getPublicKeySha256(final X509Certificate x509Certificate) {
        final PublicKey publicKey = x509Certificate.getPublicKey();
        return publicKey == null
                ? null
                : Base64.encodeBase64String(DigestUtils.sha256(publicKey.getEncoded()));
    }
}
