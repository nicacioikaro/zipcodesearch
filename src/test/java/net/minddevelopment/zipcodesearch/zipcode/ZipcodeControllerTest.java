package net.minddevelopment.zipcodesearch.zipcode;

import net.minddevelopment.zipcodesearch.shared.exception.ZipcodeNotFound;
import net.minddevelopment.zipcodesearch.zipcode.response.ZipcodeResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ZipcodeController.class)
class ZipcodeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ZipcodeService zipcodeService;

    @Test
    void shouldReturn200WhenZipcodeIsValid() throws Exception {
        when(zipcodeService.getByCep(anyString()))
                .thenReturn(mock(ZipcodeResponse.class));

        mockMvc.perform(get("/zipcodes/12345678"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn400WhenZipcodeFormatIsInvalid() throws Exception {
        mockMvc.perform(get("/zipcodes/abc"))   // não bate no regex
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn404WhenZipcodeNotFound() throws Exception {
        when(zipcodeService.getByCep(anyString()))
                .thenThrow(new ZipcodeNotFound("99999999"));

        mockMvc.perform(get("/zipcodes/99999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn400WhenStreetTooShort() throws Exception {
        mockMvc.perform(get("/zipcodes/search").param("street", "abc"))  // < 5 chars
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn405WhenMethodNotAllowed() throws Exception {
        mockMvc.perform(post("/zipcodes/12345678"))   // POST num GET
                .andExpect(status().isMethodNotAllowed());
    }
}