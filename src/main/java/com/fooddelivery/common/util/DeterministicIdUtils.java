package com.fooddelivery.common.util;

import java.util.UUID;
import org.springframework.util.DigestUtils;

public class DeterministicIdUtils {
    public static UUID generateId(String input) {
        String md5 = DigestUtils.md5DigestAsHex(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        String uuidStr = md5.replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5");
        return UUID.fromString(uuidStr);
    }
}
