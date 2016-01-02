/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.simlar.simlarserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.StringReader;

import javax.xml.bind.JAXBContext;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.simlar.simlarserver.ContactsController.XmlContacts;
import org.simlar.simlarserver.ContactsController.XmlError;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebIntegrationTest(randomPort = true)
public final class ContactsControllerTest {

    @Value("${local.server.port}")
    private int port;

    private String requestContactStatus(final String login, final String password, final String contacts) {
        final MultiValueMap<String, String> parameter = new LinkedMultiValueMap<String, String>();
        parameter.add("login", login);
        parameter.add("password", password);
        parameter.add("contacts", contacts);

        return (new RestTemplate()).postForObject("http://localhost:" + port + ContactsController.REQUEST_URL_CONTACTS_STATUS, parameter, String.class);
    }

    @Test
    public void receiveContactsStatus() throws Exception {
        final String result = requestContactStatus("*0001*", "xxxxxx", "*0002*|*0003*");
        assertNotNull(result);

        final XmlContacts contacts = (XmlContacts) JAXBContext.newInstance(XmlContacts.class).createUnmarshaller().unmarshal(new StringReader(result));
        assertNotNull(contacts);
        assertNotNull(contacts.getContacts());
        assertEquals(2, contacts.getContacts().size());
        assertEquals("*0002*", contacts.getContacts().get(0).getSimlarId());
        assertEquals(0, contacts.getContacts().get(0).getStatus());
        assertEquals("*0003*", contacts.getContacts().get(1).getSimlarId());
        assertEquals(0, contacts.getContacts().get(1).getStatus());
    }

    @Test
    public void loginWithNoSimlarId() throws Exception {
        final String result = requestContactStatus("*", "xxxxxx", "*0002*|*0003*");
        assertNotNull(result);

        final XmlError error = (XmlError) JAXBContext.newInstance(XmlError.class).createUnmarshaller().unmarshal(new StringReader(result));
        assertNotNull(error);
        assertEquals(20, error.getId());
    }
}
