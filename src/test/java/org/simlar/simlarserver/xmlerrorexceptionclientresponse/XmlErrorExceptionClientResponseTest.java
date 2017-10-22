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

import org.junit.Test;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorFailedToSendSmsException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorInvalidTelephoneNumberException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorNoIpException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorNoRegistrationCodeException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorNoSimlarIdException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorRequestedTooManyContactsException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorTooManyConfirmTriesException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorTooManyRequestTriesException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorUnknownApplePushIdException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorUnknownPushIdTypeException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorUnknownStructureException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorWrongCredentialsException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorWrongRegistrationCodeException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public final class XmlErrorExceptionClientResponseTest {

    private static final class XmlErrorNoResponseRegisteredException extends XmlErrorException {
        private static final long serialVersionUID = 1L;

        private XmlErrorNoResponseRegisteredException(final String message) {
            super(message);
        }
    }

    @SuppressWarnings("OverlyCoupledMethod")
    @Test
    public void testFromException() {
        assertNull(XmlErrorExceptionClientResponse.fromException(XmlErrorNoResponseRegisteredException.class));
        assertEquals(XmlErrorExceptionClientResponse.UNKNOWN_ERROR, XmlErrorExceptionClientResponse.fromException(null));
        assertEquals(XmlErrorExceptionClientResponse.UNKNOWN_STRUCTURE, XmlErrorExceptionClientResponse.fromException(XmlErrorUnknownStructureException.class));
        assertEquals(XmlErrorExceptionClientResponse.WRONG_CREDENTIALS, XmlErrorExceptionClientResponse.fromException(XmlErrorWrongCredentialsException.class));
        assertEquals(XmlErrorExceptionClientResponse.NO_IP, XmlErrorExceptionClientResponse.fromException(XmlErrorNoIpException.class));
        assertEquals(XmlErrorExceptionClientResponse.INVALID_TELEPHONE_NUMBER, XmlErrorExceptionClientResponse.fromException(XmlErrorInvalidTelephoneNumberException.class));
        assertEquals(XmlErrorExceptionClientResponse.TOO_MANY_REQUEST_TRIES, XmlErrorExceptionClientResponse.fromException(XmlErrorTooManyRequestTriesException.class));
        assertEquals(XmlErrorExceptionClientResponse.FAILED_TO_SEND_SMS, XmlErrorExceptionClientResponse.fromException(XmlErrorFailedToSendSmsException.class));
        assertEquals(XmlErrorExceptionClientResponse.TOO_MANY_CONFIRM_TRIES, XmlErrorExceptionClientResponse.fromException(XmlErrorTooManyConfirmTriesException.class));
        assertEquals(XmlErrorExceptionClientResponse.WRONG_REGISTRATION_CODE, XmlErrorExceptionClientResponse.fromException(XmlErrorWrongRegistrationCodeException.class));
        assertEquals(XmlErrorExceptionClientResponse.NO_SIMLAR_ID, XmlErrorExceptionClientResponse.fromException(XmlErrorNoSimlarIdException.class));
        assertEquals(XmlErrorExceptionClientResponse.NO_REGISTRATION_CODE, XmlErrorExceptionClientResponse.fromException(XmlErrorNoRegistrationCodeException.class));
        assertEquals(XmlErrorExceptionClientResponse.UNKNOWN_PUSH_ID_TYPE, XmlErrorExceptionClientResponse.fromException(XmlErrorUnknownPushIdTypeException.class));
        assertEquals(XmlErrorExceptionClientResponse.UNKNOWN_APPLE_PUSH_ID, XmlErrorExceptionClientResponse.fromException(XmlErrorUnknownApplePushIdException.class));
        assertEquals(XmlErrorExceptionClientResponse.REQUESTED_TOO_MANY_CONTACTS, XmlErrorExceptionClientResponse.fromException(XmlErrorRequestedTooManyContactsException.class));
    }
}
