package com.example.catalog.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.cors.CorsConfigurationSource;

@WebMvcTest(WebMvcConfig.class)
class WebMvcConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CorsConfigurationSource corsConfigurationSource;

    @Test
    void shouldConfigureCors() {
        assertThat(corsConfigurationSource).isNotNull();
    }

    @Test
    void shouldAllowCorsRequests() throws Exception {
        mockMvc.perform(options("/api/products")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Origin", "http://localhost:3000"))
                .andExpect(status().isOk());
    }
}
