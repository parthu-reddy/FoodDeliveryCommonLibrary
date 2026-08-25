package com.fooddelivery.common.security;

public class FeignContextHolder {
    private static final InheritableThreadLocal<String> signature = new InheritableThreadLocal<>();
    private static final InheritableThreadLocal<String> issuedAt = new InheritableThreadLocal<>();

    public static void setSignature(String sig) {
        signature.set(sig);
    }

    public static String getSignature() {
        return signature.get();
    }

    public static void setIssuedAt(String ia) {
        issuedAt.set(ia);
    }

    public static String getIssuedAt() {
        return issuedAt.get();
    }

    public static void clear() {
        signature.remove();
        issuedAt.remove();
    }
}
