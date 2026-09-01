package com.finsight.api;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitFilterTest {

    @Test
    void appliesAuthLimitToVerificationAndPasswordResetEndpoints() throws Exception {
        for (String path : List.of(
                "/api/auth/verification-code",
                "/api/auth/password-reset/request",
                "/api/auth/password-reset/confirm"
        )) {
            RateLimitFilter.RateLimitFilterSupport filter = new RateLimitFilter.RateLimitFilterSupport(
                    null,
                    new RateLimiter(1, Duration.ofMinutes(1)),
                    new RateLimiter(2, Duration.ofMinutes(1)),
                    new RateLimiter(2, Duration.ofMinutes(1))
            );

            assertThat(filterStatus(filter, path)).isEqualTo(200);
            assertThat(filterStatus(filter, path)).isEqualTo(429);
        }
    }

    private int filterStatus(RateLimitFilter.RateLimitFilterSupport filter, String path) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", path);
        request.setRemoteAddr("192.0.2.10");
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response.getStatus();
    }
}
