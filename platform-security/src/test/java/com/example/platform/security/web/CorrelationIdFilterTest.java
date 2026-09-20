package com.example.platform.security.web;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void reusesIncomingCorrelationIdAndEchoesItOnTheResponse() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(CorrelationIds.HEADER, "corr-123");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, capturingChain("corr-123"));

        assertThat(response.getHeader(CorrelationIds.HEADER)).isEqualTo("corr-123");
        assertThat(MDC.get(CorrelationIds.MDC_KEY)).isNull();
    }

    @Test
    void generatesCorrelationIdWhenHeaderMissing() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> {
            String id = MDC.get(CorrelationIds.MDC_KEY);
            assertThat(id).isNotBlank();
            assertThat(((MockHttpServletResponse) res).getHeader(CorrelationIds.HEADER)).isEqualTo(id);
        });

        assertThat(response.getHeader(CorrelationIds.HEADER)).isNotBlank();
        assertThat(MDC.get(CorrelationIds.MDC_KEY)).isNull();
    }

    private static FilterChain capturingChain(String expected) {
        return (req, res) -> assertThat(MDC.get(CorrelationIds.MDC_KEY)).isEqualTo(expected);
    }
}
