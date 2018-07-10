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

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import java.io.StringReader;
import java.io.StringWriter;

@SuppressWarnings("UtilityClass")
public final class MarshalUtil {
    private MarshalUtil() {
        throw new AssertionError("This class was not meant to be instantiated");
    }

    @SuppressWarnings("OverlyBroadThrowsClause")
    private static SAXParser createSAXParser() throws SAXException, ParserConfigurationException {
        final SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        return factory.newSAXParser();
    }

    public static <T> T unmarshal(final Class<T> resultClass, final String xml) throws JAXBException, ParserConfigurationException, SAXException {
        return JAXBContext.newInstance(resultClass).createUnmarshaller().unmarshal(
                new SAXSource(createSAXParser().getXMLReader(), new InputSource(new StringReader(xml))),
                resultClass).getValue();
    }

    public static String marshal(final Object o) throws JAXBException {
        final StringWriter writer = new StringWriter();
        JAXBContext.newInstance(o.getClass()).createMarshaller().marshal(o, writer);
        return writer.toString();
    }
}
