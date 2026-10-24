package com.caresphere.common.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests the exception handler in isolation using a standalone MockMvc setup (no Spring
 * context, no security filters). This keeps the focus on the @ControllerAdvice logic and
 * avoids pulling the JWT filter chain into a sliced context.
 */
class GlobalExceptionHandlerTest {

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void resourceNotFoundMapsTo404() throws Exception {
        mvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("missing thing"))
                .andExpect(jsonPath("$.path").value("/test/not-found"));
    }

    @Test
    void unhandledExceptionMapsTo500WithGenericMessage() throws Exception {
        mvc.perform(get("/test/boom"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }

    /** Stand-in controller used to trigger exceptions through the advice. */
    @RestController
    @RequestMapping("/test")
    static class TestController {

        @GetMapping("/not-found")
        public void notFound() {
            throw new ResourceNotFoundException("missing thing");
        }

        @GetMapping("/boom")
        public void boom() {
            throw new IllegalStateException("kaboom");
        }
    }
}
