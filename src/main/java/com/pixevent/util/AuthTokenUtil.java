package com.pixevent.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Porta do utils/authToken.js: gera e valida um token próprio
 * (payload em base64url + assinatura HMAC-SHA256), sem depender de biblioteca JWT.
 */
@Component
public class AuthTokenUtil {

    private static final Pattern DURATION_PATTERN = Pattern.compile("^(\\d+)(m|h|d)?$", Pattern.CASE_INSENSITIVE);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expires-in:8h}")
    private String defaultExpiresIn;

    public String signToken(Map<String, Object> payload) {
        return signToken(payload, defaultExpiresIn);
    }

    public String signToken(Map<String, Object> payload, String expiresIn) {
        try {
            long now = System.currentTimeMillis();
            Map<String, Object> body = new LinkedHashMap<>(payload);
            body.put("iat", now);
            body.put("exp", now + parseDurationMs(expiresIn, 8 * 60 * 60_000L));

            String json = MAPPER.writeValueAsString(body);
            String encoded = base64url(json.getBytes(StandardCharsets.UTF_8));
            String sig = hmacSha256Base64Url(encoded);
            return encoded + "." + sig;
        } catch (Exception e) {
            throw new RuntimeException("Falha ao assinar token", e);
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> verifyToken(String token) {
        try {
            if (token == null || !token.contains(".")) return null;
            String[] parts = token.split("\\.", 2);
            if (parts.length != 2) return null;
            String encoded = parts[0];
            String sig = parts[1];

            String expected = hmacSha256Base64Url(encoded);
            if (!constantTimeEquals(sig, expected)) return null;

            byte[] decoded = Base64.getUrlDecoder().decode(encoded);
            Map<String, Object> payload = MAPPER.readValue(decoded, Map.class);

            Object exp = payload.get("exp");
            if (exp == null) return null;
            long expMs = ((Number) exp).longValue();
            if (System.currentTimeMillis() > expMs) return null;

            return payload;
        } catch (Exception e) {
            return null;
        }
    }

    public boolean safeEqual(String a, String b) {
        return constantTimeEquals(a == null ? "" : a, b == null ? "" : b);
    }

    private boolean constantTimeEquals(String a, String b) {
        byte[] aa = a.getBytes(StandardCharsets.UTF_8);
        byte[] bb = b.getBytes(StandardCharsets.UTF_8);
        if (aa.length != bb.length) return false;
        return MessageDigest.isEqual(aa, bb);
    }

    private String hmacSha256Base64Url(String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] raw = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return base64url(raw);
    }

    private String base64url(byte[] input) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(input);
    }

    private long parseDurationMs(String value, long fallbackMs) {
        if (value == null || value.isBlank()) return fallbackMs;
        Matcher m = DURATION_PATTERN.matcher(value.trim());
        if (!m.matches()) return fallbackMs;
        long n = Long.parseLong(m.group(1));
        String unit = m.group(2) == null ? "ms" : m.group(2).toLowerCase();
        return switch (unit) {
            case "m" -> n * 60_000L;
            case "h" -> n * 60 * 60_000L;
            case "d" -> n * 24 * 60 * 60_000L;
            default -> n;
        };
    }
}
