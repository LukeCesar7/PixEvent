package com.pixevent.repository;

import com.pixevent.entity.PedidoMesa;
import com.pixevent.entity.PedidoMesaId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface PedidoMesaRepository extends JpaRepository<PedidoMesa, PedidoMesaId> {

    @Query("SELECT pm FROM PedidoMesa pm WHERE pm.pedido.id = :pedidoId ORDER BY pm.mesa.numero ASC")
    List<PedidoMesa> findByPedidoId(@Param("pedidoId") String pedidoId);

    @Query("SELECT pm FROM PedidoMesa pm WHERE pm.pedido.id = :pedidoId AND pm.mesa.numero = :mesaNumero")
    Optional<PedidoMesa> findByPedidoIdAndMesaNumero(@Param("pedidoId") String pedidoId, @Param("mesaNumero") String mesaNumero);

    @Transactional
    void deleteByMesa_Numero(String mesaNumero);

    @Transactional
    void deleteByPedido_Id(String pedidoId);
}
