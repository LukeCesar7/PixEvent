package com.pixevent.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum StatusMesa {
    DISPONIVEL("disponivel"),
    RESERVADA("reservada");

    private final String valor;

    StatusMesa(String valor) {
        this.valor = valor;
    }

    @JsonValue
    public String getValor() {
        return valor;
    }

    @JsonCreator
    public static StatusMesa fromValor(String valor) {
        for (StatusMesa s : values()) {
            if (s.valor.equalsIgnoreCase(valor)) return s;
        }
        throw new IllegalArgumentException("Status de mesa inválido: " + valor);
    }
}
