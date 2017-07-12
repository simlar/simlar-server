/*
 * Copyright (C) 2017 The Simlar Authors.
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

package org.simlar.simlarserver.xml;

import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public final class XmlSuccessCreateAccountRequestTest {
    @Test
    public void testMarshal() throws JAXBException {
        final XmlSuccessCreateAccountRequest response = new XmlSuccessCreateAccountRequest("*12345*", "s1cur3Me");
        final StringWriter writer = new StringWriter();
        JAXBContext.newInstance(XmlSuccessCreateAccountRequest.class).createMarshaller().marshal(response, writer);
        final String xml = writer.toString();
        assertEquals(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><success password=\"s1cur3Me\" simlarId=\"*12345*\"/>",
                xml);
    }

    @Test
    public void testUnMarshal() throws JAXBException {
        final String xml =
                "<?xml version=\"1.0\"?>\n" +
                "<success simlarId=\"*23456784*\" password=\"s1cur3Me2\"/>";

        final XmlSuccessCreateAccountRequest response = (XmlSuccessCreateAccountRequest)JAXBContext.newInstance(XmlSuccessCreateAccountRequest.class).createUnmarshaller().unmarshal(new StringReader(xml));
        assertNotNull(response);
        assertEquals("*23456784*", response.getSimlarId());
        assertEquals("s1cur3Me2", response.getPassword());
    }
}
