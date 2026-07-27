package com.cts.util;

/**
 * Masking helpers for sensitive values in outbound responses. Entities and request
 * DTOs always retain the full value; only response representations are masked.
 */
public final class MaskUtil {

    private MaskUtil() {
    }

    /** Keeps the last 4 characters and masks the rest, e.g. "ABCDE1234F" -> "••••••234F". */
    public static String maskId(String value) {
        if (value == null || value.length() <= 4) {
            return value; // nothing meaningful to mask
        }
        int visible = 4;
        return "•".repeat(value.length() - visible) + value.substring(value.length() - visible);
    }
}
