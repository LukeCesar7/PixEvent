package com.pixevent.util;

import com.pixevent.exception.ApiException;

import java.util.regex.Pattern;

public final class ValidationUtil {

    private static final Pattern CONTROL_CHARS = Pattern.compile("[\\x00-\\x1F\\x7F]");
    private static final Pattern ANGLE_BRACKETS = Pattern.compile("[<>]");
    private static final Pattern NON_DIGITS = Pattern.compile("\\D");

    private ValidationUtil() {
    }

    public static String cleanString(Object value, int max) {
        String s = value == null ? "" : String.valueOf(value);
        s = ANGLE_BRACKETS.matcher(s).replaceAll("");
        s = CONTROL_CHARS.matcher(s).replaceAll("");
        s = s.trim();
        if (s.length() > max) s = s.substring(0, max);
        return s;
    }

    public static String onlyDigits(Object value, int max) {
        String s = value == null ? "" : String.valueOf(value);
        s = NON_DIGITS.matcher(s).replaceAll("");
        if (s.length() > max) s = s.substring(0, max);
        return s;
    }

    public static void validarCadastro(String nome, String cpfLimpo, String telefoneLimpo) {
        if (nome == null || nome.isBlank()) {
            throw ApiException.badRequest("Nome é obrigatório.");
        }
        if (cpfLimpo == null || cpfLimpo.length() != 11) {
            throw ApiException.badRequest("CPF inválido.");
        }
        if (telefoneLimpo != null && !telefoneLimpo.isEmpty()
                && (telefoneLimpo.length() < 10 || telefoneLimpo.length() > 11)) {
            throw ApiException.badRequest("Telefone inválido.");
        }
    }
}
