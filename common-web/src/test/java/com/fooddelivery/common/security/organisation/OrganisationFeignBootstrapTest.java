package com.fooddelivery.common.security.organisation;

import com.fooddelivery.common.enums.OrganisationPermission;
import feign.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.openfeign.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Regression: eager shared-client creation must not poison other Feign circuit breakers. */
@SpringBootTest(classes=OrganisationFeignBootstrapTest.Config.class,webEnvironment=SpringBootTest.WebEnvironment.NONE,
    properties={"spring.cloud.config.enabled=false","eureka.client.enabled=false",
        "spring.cloud.openfeign.circuitbreaker.enabled=true",
        "spring.cloud.openfeign.client.config.organisation-service.url=http://identity",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration,org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration"})
class OrganisationFeignBootstrapTest {
    @Configuration @EnableAutoConfiguration @EnableFeignClients(clients=Probe.class)
    static class Config { }
    @FeignClient(name="existing-probe",url="http://probe") interface Probe {@GetMapping("/probe") String get();}
    @MockBean Client transport;
    @Autowired Probe probe;
    @Autowired OrganisationAccessPolicy policy;
    UUID org=UUID.fromString("22222222-2222-2222-2222-222222222222"),user=UUID.fromString("11111111-1111-1111-1111-111111111111");
    @BeforeEach void setup() throws Exception {
        when(transport.execute(any(),any())).thenAnswer(i -> {
            Request request=i.getArgument(0);
            String body=request.url().contains("/probe")?"okay":
                "{\"organisationId\":\""+org+"\",\"organisationStatus\":\"ACTIVE\",\"userId\":\""+user+"\",\"role\":\"STAFF\",\"status\":\"ACTIVE\"}";
            return Response.builder().status(200).reason("OK").request(request).headers(Map.of("Content-Type",List.of("application/json"))).body(body,StandardCharsets.UTF_8).build();
        });
    }
    @Test void bothExistingAndOrganisationClientsMakeCallsWithWorkingCircuitBreakers() throws Exception {
        assertEquals("okay",probe.get());
        assertTrue(policy.can(new UsernamePasswordAuthenticationToken(user.toString(),null,List.of()),org,OrganisationPermission.STOCK_TOGGLE));
        assertEquals("okay",probe.get());verify(transport,times(3)).execute(any(),any());
    }
}
