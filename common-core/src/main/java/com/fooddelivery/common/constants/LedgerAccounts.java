package com.fooddelivery.common.constants;

import com.fooddelivery.common.enums.PaymentGateway;
import com.fooddelivery.common.util.DeterministicIdUtils;

import java.util.UUID;

public final class LedgerAccounts {

    public static final UUID PLATFORM_CLEARING = UUID.fromString("00000000-0000-0000-0000-000000000000");
    public static final UUID PLATFORM_REVENUE = UUID.fromString("11111111-1111-1111-1111-111111111111");
    public static final UUID TAX_PAYABLE = UUID.fromString("22222222-2222-2222-2222-222222222222");
    public static final UUID BANK = UUID.fromString("33333333-3333-3333-3333-333333333333");
    /**
     * The single in-transit account a raised payout sits in until the bank confirms it.
     *
     * <p>Named here because {@code PayoutService} addressed it, and BANK, with a literal
     * all-zeros UUID at four call sites -- the same all-zeros value {@link #PLATFORM_CLEARING} uses.
     * They were distinct accounts only because the owner <em>type</em> differed, and a payout was
     * credited to a BANK account that no other producer ever touched: {@code CashService} books
     * remittances against {@link #BANK}, so cash in and payouts out were landing in two different
     * bank accounts. This is P-05's defect for a different account.
     */
    public static final UUID PAYOUT_IN_TRANSIT = UUID.fromString("44444444-4444-4444-4444-444444444444");

    
    // Namespace for ledger derivation is defined in DeterministicIdUtils
    /**
     * The one derivation of a gateway receivable account id. It previously existed in three shapes --
     * this one, {@code ledgerId("gateway", <gateway>, "")} at capture (which appends a trailing pipe)
     * and {@code ledgerId("gateway", <paymentMethod>, "")} at refund (which hashes a payment method
     * rather than a gateway) -- so a refund never reduced the account its capture had credited and the
     * GATEWAY_VS_LEDGER reconciliation check could not balance. Every caller must come through here.
     */
    public static UUID gatewayOwnerId(String gatewayName) {
        if (gatewayName == null || gatewayName.isBlank()) {
            throw new IllegalArgumentException(
                    "A gateway name is required to address a GATEWAY_RECEIVABLE account; "
                    + "hashing a null would silently create a bogus shared account.");
        }
        return DeterministicIdUtils.uuid5(DeterministicIdUtils.NS_LEDGER,
                "gateway|" + gatewayName.trim().toUpperCase());
    }

    public static UUID gatewayOwnerId(PaymentGateway gateway) {
        return gatewayOwnerId(gateway.name());
    }

    private LedgerAccounts() {}
}
