package com.fooddelivery.common.filter;

import com.fooddelivery.common.filter.RequestCachingFilter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.util.ContentCachingRequestWrapper;
import com.fooddelivery.common.filter.RequestCachingFilter.CachedBodyHttpServletRequest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RequestCachingFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private RequestCachingFilter filter;

    @Test
    void testDoFilterInternal() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/webhooks/vyapar");
        filter.doFilterInternal(request, response, filterChain);
        
        verify(filterChain).doFilter(any(CachedBodyHttpServletRequest.class), any(HttpServletResponse.class));
    }
}
