package com.pixevent.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Produto {
    INGRESSO("ingresso"),
    RIFA("rifa"),
    MESA("mesa"),
    PACOTE("pacote");

    private final String valor;

    Produto(String valor) {
        this.valor = valor;
    }

    @JsonValue
    public String getValor() {
        return valor;
    }

    @JsonCreator
    public static Produto fromValor(String valor) {
        for (Produto p : values()) {
            if (p.valor.equalsIgnoreCase(valor)) return p;
        }
        throw new IllegalArgumentException("Produto inválido: " + valor);
    }
}
