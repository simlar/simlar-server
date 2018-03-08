/*
 * Copyright (C) 2016 The Simlar Authors.
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

package org.simlar.simlarserver.xmlerrorexceptionclientresponse;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public final class XmlErrorExceptionClientResponseTest {

    private static final class XmlErrorNoResponseRegisteredException extends XmlErrorException {
        private static final long serialVersionUID = 1L;

        private XmlErrorNoResponseRegisteredException(final String message) {
            super(message);
        }
    }

    @Test
    public void testUnregisteredXmlErrorException() {
        assertNull(XmlErrorExceptionClientResponse.fromException(XmlErrorNoResponseRegisteredException.class));
        assertEquals(XmlErrorExceptionClientResponse.UNKNOWN_ERROR, XmlErrorExceptionClientResponse.fromException(null));
    }

    @Test
    public void testEveryXmlErrorExceptionIsRegistered() throws ClassNotFoundException {
        final ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AssignableTypeFilter(XmlErrorException.class));

        final EnumSet<XmlErrorExceptionClientResponse> mappings = EnumSet.of(XmlErrorExceptionClientResponse.UNKNOWN_ERROR);

        for (final BeanDefinition definition : scanner.findCandidateComponents(XmlErrorException.class.getPackage().getName())) {
            final String name = definition.getBeanClassName();
            @SuppressWarnings("unchecked")
            final XmlErrorExceptionClientResponse mapping = XmlErrorExceptionClientResponse.fromException((Class<? extends XmlErrorException>)Class.forName(name));
            assertNotNull("No mapping found for: " + name, mapping);
            assertTrue(name + " has no message", StringUtils.isNotEmpty(mapping.getMessage()));
            assertTrue(name + " id = " + mapping.getId(), mapping.getId() > 0);

            assertFalse(name + " -> " + mapping + " Already registered", mappings.contains(mapping));
            mappings.add(mapping);
        }

        assertEquals(EnumSet.allOf(XmlErrorExceptionClientResponse.class), mappings);
    }
}
