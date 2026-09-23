package com.pixevent.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "pedido_mesas")
@IdClass(PedidoMesaId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class PedidoMesa {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", referencedColumnName = "id")
    private Pedido pedido;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mesa_numero", referencedColumnName = "numero")
    private Mesa mesa;

    @Column(name = "reservado_em", nullable = false)
    private OffsetDateTime reservadoEm;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private OffsetDateTime criadoEm;

    @PrePersist
    protected void onCreate() {
        this.criadoEm = OffsetDateTime.now();
        if (this.reservadoEm == null) this.reservadoEm = this.criadoEm;
    }
}
