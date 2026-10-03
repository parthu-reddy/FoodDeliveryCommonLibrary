package com.fooddelivery.common.enums;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OrganisationRoleTest {
    @Test void matchesEveryCellOfThePublishedMatrix() {
        OrganisationPermission[] columns = { OrganisationPermission.ORG_VIEW, OrganisationPermission.ORG_MANAGE,
            OrganisationPermission.MEMBERS_MANAGE, OrganisationPermission.BUSINESS_APPLY,
            OrganisationPermission.OUTLET_MANAGE, OrganisationPermission.MENU_MANAGE,
            OrganisationPermission.STOCK_TOGGLE, OrganisationPermission.ORDERS_OPERATE,
            OrganisationPermission.EARNINGS_VIEW, OrganisationPermission.PAYOUTS_MANAGE,
            OrganisationPermission.ADS_VIEW, OrganisationPermission.ADS_MANAGE,
            OrganisationPermission.WALLET_VIEW, OrganisationPermission.WALLET_TOPUP };
        boolean[][] matrix = {
            {true,true,true,true,true,true,true,true,true,true,true,true,true,true},
            {true,false,true,true,true,true,true,true,true,true,true,true,true,true},
            {true,false,false,false,true,true,true,true,true,false,true,true,true,false},
            {true,false,false,false,false,false,true,true,false,false,false,false,false,false}
        };
        assertEquals(OrganisationPermission.values().length, columns.length);
        for (int r = 0; r < OrganisationRole.values().length; r++) {
            OrganisationRole role = OrganisationRole.values()[r];
            for (int c = 0; c < columns.length; c++) {
                assertEquals(matrix[r][c], role.grants(columns[c]), role + " / " + columns[c]);
            }
            assertFalse(role.grants(null));
        }
    }
    @Test void readOnlyAndRanksAreExplicit() {
        for (OrganisationPermission permission : OrganisationPermission.values()) {
            assertEquals(java.util.Set.of(OrganisationPermission.ORG_VIEW, OrganisationPermission.EARNINGS_VIEW,
                OrganisationPermission.ADS_VIEW, OrganisationPermission.WALLET_VIEW).contains(permission), permission.readOnly());
        }
        assertTrue(OrganisationRole.OWNER.rank() > OrganisationRole.ADMIN.rank());
        assertTrue(OrganisationRole.ADMIN.rank() > OrganisationRole.MANAGER.rank());
        assertTrue(OrganisationRole.MANAGER.rank() > OrganisationRole.STAFF.rank());
    }
}
