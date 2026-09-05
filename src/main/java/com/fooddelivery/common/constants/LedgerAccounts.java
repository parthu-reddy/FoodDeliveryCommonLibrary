package com.fooddelivery.common.constants;

import com.fooddelivery.common.enums.PaymentGateway;
import com.fooddelivery.common.util.DeterministicIdUtils;

import java.util.UUID;

public final class LedgerAccounts {

    public static final UUID PLATFORM_CLEARING = UUID.fromString("00000000-0000-0000-0000-000000000000");
    public static final UUID PLATFORM_REVENUE = UUID.fromString("11111111-1111-1111-1111-111111111111");
    public static final UUID TAX_PAYABLE = UUID.fromString("22222222-2222-2222-2222-222222222222");
    public static final UUID BANK = UUID.fromString("33333333-3333-3333-3333-333333333333");
    
    // Namespace for ledger derivation is defined in DeterministicIdUtils
    public static UUID gatewayOwnerId(PaymentGateway gateway) {
        return DeterministicIdUtils.uuid5(DeterministicIdUtils.NS_LEDGER, "gateway|" + gateway.name());
    }

    private LedgerAccounts() {}
}
