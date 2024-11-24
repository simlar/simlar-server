package org.simlar.simlarserver.webcontrollers.deleteaccountcontroller;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.simlar.simlarserver.services.accountservice.AccountService;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorFailedToSendSmsException;
import org.simlar.simlarserver.xmlerrorexceptions.XmlErrorWrongRegistrationCodeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings({"ProhibitedExceptionDeclared", "ClassWithTooManyTransitiveDependencies"})
@WebMvcTest(DeleteAccountController.class)
@RunWith(SpringRunner.class)
public final class DeleteAccountControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @SuppressWarnings("unused")
    @MockBean
    private AccountService accountService;

    @SuppressWarnings({"TestMethodWithoutAssertion", "PMD.JUnitTestsShouldIncludeAssert"})
    @SuppressFBWarnings("UTAO_JUNIT_ASSERTION_ODDITIES_NO_ASSERT")
    @Test
    public void testRenderRequest() throws Exception {
        mockMvc.perform(get(DeleteAccountController.REQUEST_PATH_REQUEST).accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Step 1: Request SMS Deletion Code")))
                .andExpect(content().string(containsString("Phone Number:")))
                .andExpect(content().string(containsString("<input type=\"submit\" value=\"Request Code\">")));
    }

    private void assertSubmitRequest(final String telephoneNumber, final String telephoneNumberUnified) throws Exception {
        reset(accountService);
        mockMvc.perform(post(DeleteAccountController.REQUEST_PATH_CONFIRM).param("telephoneNumber", telephoneNumber))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Step 2: Confirm Account Deletion")))
                .andExpect(content().string(containsString(telephoneNumber)))
                .andExpect(content().string(containsString("Deletion Code")))
                .andExpect(content().string(containsString("<input type=\"submit\" value=\"Confirm Deletion\">")));
        verify(accountService).deleteAccountRequest(eq(telephoneNumberUnified), eq("127.0.0.1")); //NOPMD.AvoidUsingHardCodedIP
    }

    @Test
    public void testSubmitRequestSuccess() throws Exception {
        assertSubmitRequest("+4917612345678", "+4917612345678");
        assertSubmitRequest("+4917612345679", "+4917612345679");
        assertSubmitRequest(" + 49 \t 176   123 4567 8 ", "+4917612345678");
    }

    @Test
    public void testSubmitRequestError() throws Exception {
        when(accountService.deleteAccountRequest(anyString(), anyString())).thenThrow(XmlErrorFailedToSendSmsException.class);
        mockMvc.perform(post(DeleteAccountController.REQUEST_PATH_CONFIRM).param("telephoneNumber", "+4917612345678"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Error")))
                .andExpect(content().string(containsString("failed to send sms")));
        verify(accountService).deleteAccountRequest(eq("+4917612345678"), eq("127.0.0.1")); //NOPMD.AvoidUsingHardCodedIP
    }

    private void assertSubmitConfirm(final String telephoneNumber, final String telephoneNumberUnified) throws Exception {
        reset(accountService);
        mockMvc.perform(post(DeleteAccountController.REQUEST_PATH_RESULT).param("telephoneNumber", telephoneNumber).param("deletionCode", "123456"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Success")))
                .andExpect(content().string(containsString("The account with the telephone number " + telephoneNumber + " has been deleted successfully.")));
        verify(accountService).confirmAccountDeletion(eq(telephoneNumberUnified), eq("123456"));
    }

    @Test
    public void testSubmitConfirmSuccess() throws Exception {
        assertSubmitConfirm("+4917612345678", "+4917612345678");
        assertSubmitConfirm("+4917612345679", "+4917612345679");
        assertSubmitConfirm(" + 49 \t 176   123 4567 8 ", "+4917612345678");
    }

    @Test
    public void testSubmitConfirmError() throws Exception {
        doThrow(XmlErrorWrongRegistrationCodeException.class).when(accountService).confirmAccountDeletion(anyString(), anyString());
        mockMvc.perform(post(DeleteAccountController.REQUEST_PATH_RESULT).param("telephoneNumber", "+4917612345678").param("deletionCode", "123456"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Error")))
                .andExpect(content().string(containsString("wrong deletion cod")));
        verify(accountService).confirmAccountDeletion(eq("+4917612345678"), eq("123456"));
    }
}
