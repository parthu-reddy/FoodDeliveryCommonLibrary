package com.fooddelivery.common.ledger;

import com.fooddelivery.common.constants.LedgerAccounts;
import com.fooddelivery.common.dto.ledger.LedgerLeg;
import com.fooddelivery.common.dto.ledger.LedgerTransactionCommand;
import com.fooddelivery.common.enums.ChargeCategory;
import com.fooddelivery.common.enums.LedgerAccountType;
import com.fooddelivery.common.util.DeterministicIdUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The ledger postings of a booking deposit (DiningPickupBooking D50), one method per movement. The ledger takes any balanced
 * command and its producer decides the accounts, so these rules live here, where reservation-service calls them and a test
 * runs them through the real ledger service. Each transaction id is derived from (producer, reservation, leg), so a replay
 * posts nothing twice, and each posting is dated on the instant the caller says the money moved. This class reads no clock.
 */
public final class DepositLedgerPostings {

    public static final String PRODUCER = "reservation-service";

    private DepositLedgerPostings() {}

    /** What a forfeit splits into: {@code ForfeitSplit} computes the parts so that they sum to the amount forfeited (D51, D52). */
    public record ForfeitParts(BigDecimal restaurant, BigDecimal platform, BigDecimal gst) {}

    private record Account(LedgerAccountType type, UUID id) {}

    /** One deposit attempt captured, keyed by its gateway order id. */
    public static LedgerTransactionCommand capture(UUID reservationId, String gateway, String gatewayOrderId, BigDecimal amount, Instant occurredAt) {
        return command(reservationId, "RESERVATION_DEPOSIT:" + gatewayOrderId, occurredAt,
                leg(gateway(gateway), held(), amount, ChargeCategory.RESERVATION_DEPOSIT));
    }

    /** Credit spent on a served order the platform settled: the clearing account pays the order's parties. */
    public static LedgerTransactionCommand redeemToClearing(UUID reservationId, UUID orderId, BigDecimal amount, Instant occurredAt) {
        return command(reservationId, "BOOKING_CREDIT:" + orderId, occurredAt,
                leg(held(), clearing(), amount, ChargeCategory.BOOKING_CREDIT));
    }

    /** Credit spent on a served order the restaurant settled itself, so the platform owes the restaurant the credit. */
    public static LedgerTransactionCommand redeemToRestaurant(UUID reservationId, UUID orderId, UUID restaurantOwnerId, BigDecimal amount, Instant occurredAt) {
        return command(reservationId, "BOOKING_CREDIT:" + orderId, occurredAt,
                leg(held(), restaurant(restaurantOwnerId), amount, ChargeCategory.BOOKING_CREDIT));
    }

    /** The credit a finished visit left unspent. */
    public static LedgerTransactionCommand minSpendShortfall(UUID reservationId, UUID restaurantOwnerId, ForfeitParts parts, Instant occurredAt) {
        return command(reservationId, "MIN_SPEND_SHORTFALL", occurredAt, split(
                leg(held(), restaurant(restaurantOwnerId), parts.restaurant(), ChargeCategory.MIN_SPEND_SHORTFALL),
                leg(held(), revenue(), parts.platform(), ChargeCategory.MIN_SPEND_SHORTFALL),
                leg(held(), tax(), parts.gst(), ChargeCategory.MIN_SPEND_SHORTFALL)));
    }

    /** The deposit of a booking nobody came for. */
    public static LedgerTransactionCommand noShowForfeit(UUID reservationId, UUID restaurantOwnerId, ForfeitParts parts, Instant occurredAt) {
        return command(reservationId, "NO_SHOW_FORFEIT", occurredAt, split(
                leg(held(), restaurant(restaurantOwnerId), parts.restaurant(), ChargeCategory.NO_SHOW_FORFEIT),
                leg(held(), revenue(), parts.platform(), ChargeCategory.NO_SHOW_FORFEIT),
                leg(held(), tax(), parts.gst(), ChargeCategory.NO_SHOW_FORFEIT)));
    }

    /** The deposit of a booking cancelled after the cutoff: the same accounts and category as a no-show, under its own leg. */
    public static LedgerTransactionCommand lateCancelForfeit(UUID reservationId, UUID restaurantOwnerId, ForfeitParts parts, Instant occurredAt) {
        return command(reservationId, "LATE_CANCEL_FORFEIT", occurredAt, split(
                leg(held(), restaurant(restaurantOwnerId), parts.restaurant(), ChargeCategory.NO_SHOW_FORFEIT),
                leg(held(), revenue(), parts.platform(), ChargeCategory.NO_SHOW_FORFEIT),
                leg(held(), tax(), parts.gst(), ChargeCategory.NO_SHOW_FORFEIT)));
    }

    /** One refund, keyed by its deterministic refund id (D55), so partial refunds post separately and a replay does not. */
    public static LedgerTransactionCommand refund(UUID reservationId, String gateway, String refundId, BigDecimal amount, Instant occurredAt) {
        return command(reservationId, "RESERVATION_REFUND:" + refundId, occurredAt,
                leg(held(), gateway(gateway), amount, ChargeCategory.RESERVATION_REFUND));
    }

    private static Account held() {
        return new Account(LedgerAccountType.RESERVATION_DEPOSITS_HELD, LedgerAccounts.RESERVATION_DEPOSITS_HELD);
    }

    private static Account gateway(String gateway) {
        return new Account(LedgerAccountType.GATEWAY_RECEIVABLE, LedgerAccounts.gatewayOwnerId(gateway));
    }

    private static Account clearing() {
        return new Account(LedgerAccountType.PLATFORM_CLEARING, LedgerAccounts.PLATFORM_CLEARING);
    }

    private static Account restaurant(UUID ownerId) {
        return new Account(LedgerAccountType.RESTAURANT_PAYABLE, ownerId);
    }

    private static Account revenue() {
        return new Account(LedgerAccountType.PLATFORM_REVENUE, LedgerAccounts.PLATFORM_REVENUE);
    }

    private static Account tax() {
        return new Account(LedgerAccountType.TAX_PAYABLE, LedgerAccounts.TAX_PAYABLE);
    }

    private static LedgerLeg leg(Account from, Account to, BigDecimal amount, ChargeCategory category) {
        LedgerLeg leg = new LedgerLeg();
        leg.setFromType(from.type());
        leg.setFromId(from.id());
        leg.setToType(to.type());
        leg.setToId(to.id());
        leg.setAmount(amount);
        leg.setCategory(category);
        return leg;
    }

    /** The ledger refuses a leg of zero, and a part can be zero: a 0 % commission, or GST configured at 0 %. */
    private static LedgerLeg[] split(LedgerLeg... parts) {
        List<LedgerLeg> nonZero = new ArrayList<>();
        for (LedgerLeg part : parts) {
            if (part.getAmount().signum() != 0) {
                nonZero.add(part);
            }
        }
        return nonZero.toArray(new LedgerLeg[0]);
    }

    private static LedgerTransactionCommand command(UUID reservationId, String leg, Instant occurredAt, LedgerLeg... legs) {
        UUID transactionId = DeterministicIdUtils.ledgerId(PRODUCER, reservationId, leg);
        return new LedgerTransactionCommand(transactionId, reservationId, PRODUCER, leg, List.of(legs), occurredAt);
    }
}
