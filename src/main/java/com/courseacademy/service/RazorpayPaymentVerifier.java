package com.courseacademy.service;

import com.courseacademy.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;

/**
 * Production-grade Razorpay signature verification.
 *
 * Uses the standard algorithm:
 * signature = HMAC_SHA256(key_secret, orderId + "|" + paymentId)
 * and compares against the signature sent by the client.
 */
@Component
@RequiredArgsConstructor
public class RazorpayPaymentVerifier {

    @Value("${razorpay.key.secret:}")
    private String razorpayKeySecret;

    public void verifyOrThrow(String orderId, String paymentId, String signature) {
        if (razorpayKeySecret == null || razorpayKeySecret.isBlank()) {
            // Fail fast: without secret, the backend cannot verify payments.
            throw new BadRequestException("Razorpay verification is not configured on the server.");
        }
        if (isBlank(orderId) || isBlank(paymentId) || isBlank(signature)) {
            throw new BadRequestException("Payment verification failed.");
        }

        String payload = orderId + "|" + paymentId;
        String expected = hmacSha256Hex(razorpayKeySecret, payload);

        if (!expected.equalsIgnoreCase(signature.trim())) {
            throw new BadRequestException("Payment verification failed.");
        }
    }

    private boolean isBlank(String v) {
        return v == null || v.isBlank();
    }

    private String hmacSha256Hex(String key, String data) {
        try {
            // javax.crypto is available in JDK. Use it without adding extra dependencies.
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            javax.crypto.spec.SecretKeySpec secretKeySpec = new javax.crypto.spec.SecretKeySpec(
                    key.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );
            mac.init(secretKeySpec);

            byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            // hex encoding
            StringBuilder sb = new StringBuilder(rawHmac.length * 2);
            for (byte b : rawHmac) {
                sb.append(String.format(Locale.ROOT, "%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            // Internal failure: do not leak details.
            throw new BadRequestException("Payment verification failed.");
        }
    }
}

