package com.fooddelivery.common.filter;

import com.fooddelivery.common.constants.RequestAttributeConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.springframework.mock.web.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class IdentityFilterTest {
    @AfterEach void clear() { SecurityContextHolder.clearContext(); }
    @Test void forgedHeadersNeverPopulateIdentityAttributes() throws Exception {
        var request=new MockHttpServletRequest();request.addHeader("X-User-Id","forged");request.addHeader("X-User-Roles","CUSTOMER,DELIVERY");
        new IdentityFilter().doFilter(request,new MockHttpServletResponse(),new MockFilterChain());
        assertNull(request.getAttribute(RequestAttributeConstants.X_USER_ID));assertNull(request.getAttribute(RequestAttributeConstants.CUSTOMER_ID));
    }
    @Test void allVerifiedRolesPopulateTheirOwnEntityAttributesWithTheSameCanonicalPerson() throws Exception {
        var request=new MockHttpServletRequest();request.addHeader("X-User-Id","forged");
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("person",null,
                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"),new SimpleGrantedAuthority("ROLE_DELIVERY"),new SimpleGrantedAuthority("ROLE_RESTAURANT"),new SimpleGrantedAuthority("ROLE_BUSINESS"))));
        new IdentityFilter().doFilter(request,new MockHttpServletResponse(),new MockFilterChain());
        for(var key:List.of(RequestAttributeConstants.CUSTOMER_ID,RequestAttributeConstants.DELIVERY_EXECUTIVE_ID,RequestAttributeConstants.OWNER_ID))
            assertEquals("person",request.getAttribute(key));
    }
}
