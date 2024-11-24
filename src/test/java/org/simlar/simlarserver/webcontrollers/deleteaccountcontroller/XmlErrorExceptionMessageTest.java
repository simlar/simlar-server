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

package org.simlar.simlarserver.webcontrollers.deleteaccountcontroller;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorCallNotAllowedAtTheMomentException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorFailedToRequestPushNotificationException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorFailedToTriggerCallException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorNoCallSessionException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorNoIpException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorRequestedTooManyContactsException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorUnknownApplePushIdException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorUnknownPushIdTypeException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorUnknownStructureException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorWrongCredentialsException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.io.Serial;
import java.util.EnumSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class XmlErrorExceptionMessageTest {

    private static final class XmlErrorNoResponseRegisteredException extends XmlErrorException {
        @Serial
        private static final long serialVersionUID = 1L;

        private XmlErrorNoResponseRegisteredException(final String message) {
            super(message);
        }
    }

    @Test
    public void testUnregisteredXmlErrorException() {
        assertEquals(XmlErrorExceptionMessage.UNKNOWN_ERROR, XmlErrorExceptionMessage.fromException(XmlErrorNoResponseRegisteredException.class));
        assertEquals(XmlErrorExceptionMessage.UNKNOWN_ERROR, XmlErrorExceptionMessage.fromException(null));
    }

    @SuppressWarnings("StaticCollection")
    private static final Set<Class<?>> UNKNOWN_ERROR_CLASSES = Set.of(
            XmlErrorCallNotAllowedAtTheMomentException.class,
            XmlErrorFailedToRequestPushNotificationException.class,
            XmlErrorFailedToTriggerCallException.class,
            XmlErrorNoCallSessionException.class,
            XmlErrorNoIpException.class,
            XmlErrorRequestedTooManyContactsException.class,
            XmlErrorUnknownApplePushIdException.class,
            XmlErrorUnknownPushIdTypeException.class,
            XmlErrorUnknownStructureException.class,
            XmlErrorWrongCredentialsException.class
    );

    @SuppressWarnings("ObjectAllocationInLoop")
    @Test
    public void testEveryXmlErrorExceptionIsRegistered() throws ClassNotFoundException {
        final ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AssignableTypeFilter(XmlErrorException.class));

        final EnumSet<XmlErrorExceptionMessage> mappings = EnumSet.of(XmlErrorExceptionMessage.UNKNOWN_ERROR);

        for (final BeanDefinition definition : scanner.findCandidateComponents(XmlErrorException.class.getPackage().getName())) {
            final String name = definition.getBeanClassName();

            final Class<?> clazz = Class.forName(name);
            if (!UNKNOWN_ERROR_CLASSES.contains(clazz)) {
                @SuppressWarnings("unchecked")
                final XmlErrorExceptionMessage mapping = XmlErrorExceptionMessage.fromException((Class<? extends XmlErrorException>) clazz);
                assertTrue(name + " has no message", StringUtils.isNotEmpty(mapping.getMessage()));

                assertFalse(name + " -> " + mapping + " Already registered", mappings.contains(mapping));
                mappings.add(mapping);
            }
        }

        assertEquals(EnumSet.allOf(XmlErrorExceptionMessage.class), mappings);
    }
}
