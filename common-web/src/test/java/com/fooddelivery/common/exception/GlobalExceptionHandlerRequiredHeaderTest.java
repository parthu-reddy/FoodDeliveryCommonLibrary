package com.fooddelivery.common.exception;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * A request missing a required header is the client's mistake: 400, through the real advice. The
 * catch-all {@code Exception} handler answered 500 for it, because {@code MissingRequestHeaderException}
 * carries no {@code @ResponseStatus} -- a business wallet top-up without its Idempotency-Key (W2) would
 * have looked like a server fault.
 */
class GlobalExceptionHandlerRequiredHeaderTest {

    @RestController
    static class NeedsAHeader {
        @PostMapping("/needs-a-header")
        String handle(@RequestHeader(value = "Idempotency-Key", required = true) String key) {
            return key;
        }
    }

    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new NeedsAHeader())
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    @Test
    void aMissingRequiredHeaderIsABadRequestNamingTheHeader() throws Exception {
        mockMvc.perform(post("/needs-a-header"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Idempotency-Key")));
    }

    @Test
    void thePresentHeaderReachesTheHandler() throws Exception {
        mockMvc.perform(post("/needs-a-header").header("Idempotency-Key", "k-1"))
                .andExpect(status().isOk());
    }
}
