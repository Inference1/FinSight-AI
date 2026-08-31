package com.finsight.api;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitFilterTest {

    @Test
    void ignoresUntrustedForwardedAddressesWhenRateLimiting() throws Exception {
        RateLimitFilter.RateLimitFilterSupport filter = new RateLimitFilter.RateLimitFilterSupport(
                null,
                new RateLimiter(1, Duration.ofMinutes(1)),
                new RateLimiter(2, Duration.ofMinutes(1)),
                new RateLimiter(2, Duration.ofMinutes(1))
        );

        assertThat(filterStatus(filter, "203.0.113.1")).isEqualTo(200);
        assertThat(filterStatus(filter, "198.51.100.2")).isEqualTo(429);
    }

    private int filterStatus(RateLimitFilter.RateLimitFilterSupport filter, String forwardedFor) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
        request.setRemoteAddr("192.0.2.10");
        request.addHeader("X-Forwarded-For", forwardedFor);
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response.getStatus();
    }
}
