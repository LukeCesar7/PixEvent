package com.pixevent.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class RegistrarPedidoResultado {
    private String id;
    private BigDecimal valorTotal;
    private List<String> mesas;
}
