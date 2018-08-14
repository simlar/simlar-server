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
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;

public final class XmlSuccessCreateAccountConfirmTest {
    @Test
    public void testMarshal() throws JAXBException {
        final XmlSuccessCreateAccountConfirm response = new XmlSuccessCreateAccountConfirm("*12345*", "123456");
        final StringWriter writer = new StringWriter();
        JAXBContext.newInstance(XmlSuccessCreateAccountConfirm.class).createMarshaller().marshal(response, writer);
        final String xml = writer.toString();
        assertEquals(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><success simlarId=\"*12345*\" registrationCode=\"123456\"/>",
                xml);
    }
}
