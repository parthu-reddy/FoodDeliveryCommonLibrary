package com.fooddelivery.common.ledger;

import com.fooddelivery.common.dto.ledger.LedgerLeg;
import com.fooddelivery.common.dto.ledger.LedgerTransactionCommand;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link DepositLedgerPostings#returnToDeposits} is the exact reverse of {@link DepositLedgerPostings#redeemToClearing}
 * (NamedLimits NL-D5): a redemption followed by a return of the same amount leaves every account where it was, and each
 * return is its own transaction.
 */
class DepositLedgerPostingsReturnTest {

    private static final Instant AT = Instant.parse("2026-11-22T12:00:00Z");
    private final UUID reservation = UUID.randomUUID();

    @Test
    void aRedemptionAndItsReturnNetToZeroOnEveryAccount() {
        BigDecimal amount = new BigDecimal("300.00");
        Map<String, BigDecimal> net = new HashMap<>();
        apply(net, DepositLedgerPostings.redeemToClearing(reservation, UUID.randomUUID(), amount, AT));
        apply(net, DepositLedgerPostings.returnToDeposits(reservation, UUID.randomUUID(), amount, AT));

        assertThat(net).isNotEmpty();
        assertThat(net.values()).allSatisfy(v -> assertThat(v).isEqualByComparingTo("0"));
    }

    @Test
    void theReturnHasTheRedemptionsLegSwappedWithTheSameCategory() {
        LedgerLeg redeem = DepositLedgerPostings.redeemToClearing(reservation, UUID.randomUUID(), BigDecimal.TEN, AT).getLegs().get(0);
        LedgerTransactionCommand back = DepositLedgerPostings.returnToDeposits(reservation, UUID.randomUUID(), BigDecimal.TEN, AT);

        assertThat(back.getLegs()).hasSize(1);
        LedgerLeg leg = back.getLegs().get(0);
        assertThat(leg.getFromType()).isEqualTo(redeem.getToType());
        assertThat(leg.getFromId()).isEqualTo(redeem.getToId());
        assertThat(leg.getToType()).isEqualTo(redeem.getFromType());
        assertThat(leg.getToId()).isEqualTo(redeem.getFromId());
        assertThat(leg.getCategory()).isEqualTo(redeem.getCategory());
        assertThat(back.getOccurredAt()).isEqualTo(AT);
    }

    @Test
    void eachReturnIsItsOwnTransactionAndAReplayIsTheSame() {
        UUID first = UUID.randomUUID();
        LedgerTransactionCommand a = DepositLedgerPostings.returnToDeposits(reservation, first, BigDecimal.TEN, AT);
        LedgerTransactionCommand replay = DepositLedgerPostings.returnToDeposits(reservation, first, BigDecimal.TEN, AT);
        LedgerTransactionCommand b = DepositLedgerPostings.returnToDeposits(reservation, UUID.randomUUID(), BigDecimal.TEN, AT);
        LedgerTransactionCommand redeem = DepositLedgerPostings.redeemToClearing(reservation, first, BigDecimal.TEN, AT);

        assertThat(replay.getTransactionId()).isEqualTo(a.getTransactionId());
        assertThat(b.getTransactionId()).isNotEqualTo(a.getTransactionId());
        assertThat(redeem.getTransactionId()).as("a return never collides with the redemption of the same id")
                .isNotEqualTo(a.getTransactionId());
    }

    private static void apply(Map<String, BigDecimal> net, LedgerTransactionCommand command) {
        for (LedgerLeg leg : command.getLegs()) {
            net.merge(leg.getFromType() + ":" + leg.getFromId(), leg.getAmount().negate(), BigDecimal::add);
            net.merge(leg.getToType() + ":" + leg.getToId(), leg.getAmount(), BigDecimal::add);
        }
    }
}
