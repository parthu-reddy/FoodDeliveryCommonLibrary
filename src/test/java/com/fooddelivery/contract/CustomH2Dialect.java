package com.fooddelivery.contract;

import org.hibernate.boot.model.TypeContributions;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.SqlTypes;
import org.hibernate.type.descriptor.jdbc.VarcharJdbcType;
import org.hibernate.type.descriptor.jdbc.spi.JdbcTypeRegistry;

/**
 * Maps Postgres named enums to varchar so H2 can run schemas written for Postgres.
 *
 * Lives here because BaseIntegrationTest (same test-jar) names it in a @DynamicPropertySource. It
 * previously existed only in DeliveryExecutiveApplication and GovernmentIDValidationService test
 * sources, so any other module extending BaseIntegrationTest failed with
 * "Unable to resolve name [com.fooddelivery.contract.CustomH2Dialect]". Those two copies are
 * byte-identical to this one and can be deleted.
 */
public class CustomH2Dialect extends H2Dialect {
    @Override
    public void contributeTypes(TypeContributions typeContributions, ServiceRegistry serviceRegistry) {
        super.contributeTypes(typeContributions, serviceRegistry);
        typeContributions.getTypeConfiguration().getDdlTypeRegistry().addDescriptor(
            new org.hibernate.type.descriptor.sql.internal.DdlTypeImpl(SqlTypes.NAMED_ENUM, "varchar", this)
        );
    }
}
