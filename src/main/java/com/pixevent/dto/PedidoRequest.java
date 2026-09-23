package com.pixevent.dto;

import lombok.Data;

import java.util.List;

@Data
public class PedidoRequest {
    private String nome;
    private String telefone;
    private String cpf;
    private List<ItemRequest> itens;
    private Object mesa_numero; // pode vir como String (CSV) ou List<String>, igual ao Node
}
