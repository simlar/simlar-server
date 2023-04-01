/*
 * Copyright (C) 2018 The Simlar Authors.
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

package org.simlar.simlarserver.utils;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;
import java.io.StringWriter;

@SuppressWarnings("UtilityClass")
public final class MarshalUtil {
    private MarshalUtil() {
        throw new AssertionError("This class was not meant to be instantiated");
    }

    private static XMLStreamReader createXMLStreamReader(final String xml) throws XMLStreamException {
        final XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
        return factory.createXMLStreamReader(new StringReader(xml));
    }

    public static <T> T unmarshal(final Class<T> resultClass, final String xml) throws JAXBException, XMLStreamException {
        return JAXBContext.newInstance(resultClass).createUnmarshaller().unmarshal(
                createXMLStreamReader(xml),
                resultClass).getValue();
    }

    public static String marshal(final Object o) throws JAXBException {
        final StringWriter writer = new StringWriter();
        JAXBContext.newInstance(o.getClass()).createMarshaller().marshal(o, writer);
        return writer.toString();
    }
}
