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

import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class SimlarId {

    private final String simlarId;

    private SimlarId(final String simlarId) {
        this.simlarId = simlarId;
    }

    public static SimlarId create(final String simlarId) {
        if (!check(simlarId)) {
            return null;
        }

        return new SimlarId(simlarId);
    }

    public String get() {
        return simlarId;
    }

    public static boolean check(final String str) {
        return StringUtils.hasText(str) && str.matches("\\*\\d+\\*");
    }

    public static List<SimlarId> parsePipeSeparatedSimlarIds(final String str) {
        final LinkedHashSet<SimlarId> simlarIds = new LinkedHashSet<>();

        if (str != null) {
            for (final String entry : str.split("\\|")) {
                final SimlarId simlarId = SimlarId.create(entry.trim());
                if (simlarId != null) {
                    simlarIds.add(simlarId);
                }
            }
        }

        return new ArrayList<>(simlarIds);
    }

    public static List<SimlarId> sortAndUnifySimlarIds(final Collection<SimlarId> simlarIds) {
        if (simlarIds == null) {
            return null;
        }

        return simlarIds.stream().sorted((o1, o2) -> o1.get().compareTo(o2.get())).distinct().collect(Collectors.toList());
    }

    public static String hashSimlarIds(final Collection<SimlarId> simlarIds) {
        return Hash.md5(String.join("", simlarIds.stream().map(SimlarId::get).collect(Collectors.toList())));
    }

    @Override
    public boolean equals(final Object other) {
        return this == other || other != null && getClass() == other.getClass()
                && Objects.equals(simlarId, ((SimlarId) other).simlarId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(simlarId);
    }


    @Override
    public String toString() {
        return simlarId;
    }
}
