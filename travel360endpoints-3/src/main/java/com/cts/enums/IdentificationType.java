package com.cts.enums;

/**
 * Supported identification documents. Each type owns the regex its number must match,
 * so format validation stays next to the type it describes.
 */
public enum IdentificationType {

    AADHAAR("^[0-9]{12}$"),
    PAN("^[A-Z]{5}[0-9]{4}[A-Z]$"),
    PASSPORT("^[A-Z0-9]{6,9}$"),
    DRIVING_LICENSE("^[A-Z]{2}[0-9]{13}$");

    private final String pattern;

    IdentificationType(String pattern) {
        this.pattern = pattern;
    }

    /** True if the given number matches this identification type's format. */
    public boolean matches(String value) {
        return value != null && value.matches(pattern);
    }
}
