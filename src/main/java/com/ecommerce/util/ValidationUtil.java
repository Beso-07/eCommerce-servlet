package com.ecommerce.util;

import com.ecommerce.exception.ValidationException;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

public final class ValidationUtil {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private ValidationUtil() {
    }

    public static void requireNotBlank(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(field + " is required");
        }
    }

    public static void validateEmail(String email) {
        requireNotBlank(email, "Email");
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException("Invalid email format");
        }
    }

    public static void validatePassword(String password) {
        requireNotBlank(password, "Password");
        if (password.length() < 8) {
            throw new ValidationException("Password must be at least 8 characters");
        }
    }

    public static void validateRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new ValidationException("Rating must be between 1 and 5");
        }
    }

    public static void validatePrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Price must be greater than zero");
        }
    }

    public static void validateId(long id, String field) {
        if (id <= 0) {
            throw new ValidationException(field + " is invalid");
        }
    }

    public static void validateImageUrl(String imageUrl) {
        requireNotBlank(imageUrl, "Product image URL");
        try {
            URI uri = new URI(imageUrl.trim());
            String scheme = uri.getScheme();
            if (scheme == null || (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme))) {
                throw new ValidationException("Product image URL must start with http or https");
            }
        } catch (URISyntaxException ex) {
            throw new ValidationException("Product image URL is invalid");
        }
    }
}
