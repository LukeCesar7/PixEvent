package com.pixevent.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "pedidos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Pedido {

    @Id
    @Column(length = 24)
    private String id;

    @Column(length = 11, nullable = false)
    private String cpf;

    @Column(nullable = false)
    private String nome;

    @Column
    @Builder.Default
    private String email = "";

    @Column(length = 20, nullable = false)
    private String telefone;

    @Convert(converter = ProdutoConverter.class)
    @Column(nullable = false, length = 20)
    private Produto produto;

    @Column(nullable = false)
    @Builder.Default
    private Integer quantidade = 1;

    @Column(name = "mesa_numero", length = 64)
    private String mesaNumero;

    @Column(name = "valor_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorTotal;

    @Column(name = "metodo_pgto", nullable = false, length = 20)
    @Builder.Default
    private String metodoPgto = "pix";

    @Convert(converter = StatusPedidoConverter.class)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatusPedido status = StatusPedido.PENDENTE;

    @Column(name = "mp_payment_id")
    private String mpPaymentId;

    @Column(name = "qrcode_token", length = 40, unique = true)
    private String qrcodeToken;

    @Column(name = "qrcode_usado", nullable = false)
    @Builder.Default
    private Boolean qrcodeUsado = false;

    @Column(name = "qrcode_usado_em")
    private OffsetDateTime qrcodeUsadoEm;

    @Column(name = "itens_json", columnDefinition = "TEXT")
    private String itensJson;

    @Column(name = "pago_em")
    private OffsetDateTime pagoEm;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private OffsetDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private OffsetDateTime atualizadoEm;

    @JsonIgnore
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Rifa> rifas = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<PedidoMesa> pedidoMesas = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        this.criadoEm = now;
        this.atualizadoEm = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.atualizadoEm = OffsetDateTime.now();
    }
}
