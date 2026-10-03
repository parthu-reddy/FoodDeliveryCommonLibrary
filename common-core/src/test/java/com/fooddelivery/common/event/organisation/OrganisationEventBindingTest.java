package com.fooddelivery.common.event.organisation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fooddelivery.common.event.*;
import com.fooddelivery.common.constants.EventType;
import com.fooddelivery.common.enums.*;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class OrganisationEventBindingTest {
    @Test void allThreeEventsBindAtTheRootWithValidatedFieldsAndNullableRemovedRole() throws Exception {
        var mapper=new ObjectMapper().findAndRegisterModules();
        try(var validators=Validation.buildDefaultValidatorFactory()){
            var binder=new EventBinder(mapper,validators.getValidator());UUID org=UUID.randomUUID(),actor=UUID.randomUUID();Instant now=Instant.parse("2026-10-03T10:00:00Z");
            var created=new OrganisationCreatedEvent(org,"Brand",actor,now);
            assertEquals(created,binder.bindIf(EventType.ORGANISATION_CREATED,EventType.ORGANISATION_CREATED.name(),mapper.writeValueAsString(created),OrganisationCreatedEvent.class).orElseThrow());
            var changed=new OrganisationMembershipChangedEvent(org,actor,OrganisationRole.OWNER,MembershipStatus.ACTIVE,actor,now);
            assertEquals(changed,binder.bind(mapper.writeValueAsString(changed),OrganisationMembershipChangedEvent.class));
            var missingRole=mapper.valueToTree(changed);
            ((com.fasterxml.jackson.databind.node.ObjectNode)missingRole).remove("role");
            assertThrows(EventBindingException.class,()->binder.bind(missingRole.toString(),OrganisationMembershipChangedEvent.class));
            var removed=new OrganisationMembershipChangedEvent(org,actor,null,MembershipStatus.REMOVED,actor,now);
            assertEquals(removed,binder.bind(mapper.writeValueAsString(removed),OrganisationMembershipChangedEvent.class));
            var suspended=new OrganisationStatusChangedEvent(org,OrganisationStatus.SUSPENDED,"Under review",now);
            assertEquals(suspended,binder.bind(mapper.writeValueAsString(suspended),OrganisationStatusChangedEvent.class));
            assertThrows(EventBindingException.class,()->binder.bind("{}",OrganisationCreatedEvent.class));
            assertThrows(EventBindingException.class,()->binder.bind("{\"status\":\"ACTIVE\"}",OrganisationMembershipChangedEvent.class));
        }
    }
}
