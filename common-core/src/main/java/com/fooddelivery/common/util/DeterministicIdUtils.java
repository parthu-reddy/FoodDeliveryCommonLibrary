package com.fooddelivery.common.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import org.springframework.util.DigestUtils;

public final class DeterministicIdUtils {

    public static final UUID NS_LEDGER = UUID.fromString("6ba7b810-9dad-11d1-80b4-00c04fd430c8");

    private DeterministicIdUtils() {}

    /**
     * Legacy ID generation, retained if needed elsewhere.
     */
    public static UUID generateId(String input) {
        String md5 = DigestUtils.md5DigestAsHex(input.getBytes(StandardCharsets.UTF_8));
        String uuidStr = md5.replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5");
        return UUID.fromString(uuidStr);
    }

    /**
     * Generates a UUIDv5 (SHA-1 based) from a namespace UUID and a name string.
     */
    public static UUID uuid5(UUID namespace, String name) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(toBytes(namespace));
            md.update(name.getBytes(StandardCharsets.UTF_8));
            byte[] sha1Bytes = md.digest();

            // Set version to 5 (SHA-1)
            sha1Bytes[6] = (byte) ((sha1Bytes[6] & 0x0f) | 0x50);
            // Set variant to RFC 4122
            sha1Bytes[8] = (byte) ((sha1Bytes[8] & 0x3f) | 0x80);

            long msb = 0;
            long lsb = 0;
            for (int i = 0; i < 8; i++) {
                msb = (msb << 8) | (sha1Bytes[i] & 0xff);
            }
            for (int i = 8; i < 16; i++) {
                lsb = (lsb << 8) | (sha1Bytes[i] & 0xff);
            }
            return new UUID(msb, lsb);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-1 not available", e);
        }
    }

    private static byte[] toBytes(UUID uuid) {
        byte[] bytes = new byte[16];
        long msb = uuid.getMostSignificantBits();
        long lsb = uuid.getLeastSignificantBits();
        for (int i = 7; i >= 0; i--) {
            bytes[i] = (byte) (msb & 0xff);
            msb >>= 8;
        }
        for (int i = 15; i >= 8; i--) {
            bytes[i] = (byte) (lsb & 0xff);
            lsb >>= 8;
        }
        return bytes;
    }

    /**
     * Derives a deterministic transaction ID for the ledger based on producer, reference, and leg string.
     */
    public static UUID ledgerId(String producer, UUID reference, String leg) {
        return uuid5(NS_LEDGER, producer + "|" + reference.toString() + "|" + leg);
    }

    /**
     * Derives a deterministic transaction ID for the ledger based on producer, reference, and leg string.
     */
    public static UUID ledgerId(String producer, String reference, String leg) {
        return uuid5(NS_LEDGER, producer + "|" + reference + "|" + leg);
    }

    /**
     * Checks if a given transaction ID is a valid ledger ID derived from the provided triple.
     */
    public static boolean isLedgerId(UUID transactionId, String producer, String reference, String leg) {
        if (transactionId == null) {
            return false;
        }
        if (transactionId.version() != 5) {
            return false;
        }
        UUID expected = uuid5(NS_LEDGER, producer + "|" + reference + "|" + leg);
        return expected.equals(transactionId);
    }
}
