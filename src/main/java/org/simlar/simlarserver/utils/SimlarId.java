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

package org.simlar.simlarserver.utils;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@SuppressWarnings("ClassWithTooManyDependents")
public final class SimlarId implements Comparable<SimlarId> {
    private static final Pattern REGEX_PATTERN_SIMLAR_ID        = Pattern.compile("\\*\\d+\\*");
    private static final Pattern REGEX_PATTERN_TELEPHONE_NUMBER = Pattern.compile("\\+\\d+");

    private final String simlarId;

    private SimlarId(final String simlarId) {
        this.simlarId = simlarId;
    }

    @SuppressWarnings("ReturnOfNull")
    public static SimlarId create(final String simlarId) {
        if (!check(simlarId)) {
            return null;
        }

        return new SimlarId(simlarId);
    }

    @Nullable
    public static SimlarId createWithTelephoneNumber(final String telephoneNumber) {
        if (telephoneNumber == null || !REGEX_PATTERN_TELEPHONE_NUMBER.matcher(telephoneNumber).matches()) {
            return null;
        }

        return create('*' + telephoneNumber.substring(1) + '*');
    }

    public String get() {
        return simlarId;
    }

    static boolean check(final CharSequence input) {
        return input != null && REGEX_PATTERN_SIMLAR_ID.matcher(input).matches();
    }

    public static List<SimlarId> parsePipeSeparatedSimlarIds(final String str) {
        if (StringUtils.isEmpty(str)) {
            return Collections.emptyList();
        }

        final String[] entries = str.split("\\|");
        final LinkedHashSet<SimlarId> simlarIds = new LinkedHashSet<>(entries.length);

        for (final String entry : entries) {
            final SimlarId simlarId = create(entry.trim());
            if (simlarId != null) {
                simlarIds.add(simlarId);
            }
        }

        return new ArrayList<>(simlarIds);
    }

    public static SortedSet<SimlarId> sortAndUnifySimlarIds(final Collection<SimlarId> simlarIds) {
        return simlarIds == null ? Collections.emptySortedSet() : new TreeSet<>(simlarIds);
    }

    @SuppressWarnings("TypeMayBeWeakened") // we definitely want a sorted set here
    public static String hashSimlarIds(final SortedSet<SimlarId> simlarIds) {
        return DigestUtils.sha256Hex(simlarIds.stream().map(SimlarId::get).collect(Collectors.joining()));
    }

    @Override
    public boolean equals(final Object obj) {
        //noinspection ConstantConditions // false positive class cast warning
        return this == obj ||
                obj != null && Objects.equals(getClass(), obj.getClass()) && Objects.equals(simlarId, ((SimlarId) obj).simlarId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(simlarId);
    }

    @Override
    public String toString() {
        return simlarId;
    }

    @Override
    public int compareTo(@Nonnull final SimlarId o) {
        return Collator.getInstance(Locale.ENGLISH).compare(simlarId, o.simlarId);
    }
}
