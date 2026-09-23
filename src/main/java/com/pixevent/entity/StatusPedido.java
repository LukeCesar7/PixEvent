package com.pixevent.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum StatusPedido {
    PENDENTE("pendente"),
    AGUARDANDO_PIX("aguardando_pix"),
    PAGO("pago"),
    CANCELADO("cancelado"),
    EXPIRADO("expirado");

    private final String valor;

    StatusPedido(String valor) {
        this.valor = valor;
    }

    @JsonValue
    public String getValor() {
        return valor;
    }

    @JsonCreator
    public static StatusPedido fromValor(String valor) {
        for (StatusPedido s : values()) {
            if (s.valor.equalsIgnoreCase(valor)) return s;
        }
        throw new IllegalArgumentException("Status inválido: " + valor);
    }
}
