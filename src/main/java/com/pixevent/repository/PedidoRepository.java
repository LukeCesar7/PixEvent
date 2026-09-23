package com.pixevent.repository;

import com.pixevent.entity.Pedido;
import com.pixevent.entity.StatusPedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, String> {

    Optional<Pedido> findByQrcodeToken(String qrcodeToken);

    List<Pedido> findByNomeContainingIgnoreCaseOrderByCriadoEmDesc(String nome);

    List<Pedido> findByCpfOrderByCriadoEmDesc(String cpf);

    List<Pedido> findByStatusOrderByPagoEmDesc(StatusPedido status);

    List<Pedido> findByQrcodeUsadoTrueOrderByQrcodeUsadoEmAsc();

    Page<Pedido> findByStatusAndNomeContainingIgnoreCase(StatusPedido status, String nome, Pageable pageable);

    Page<Pedido> findByStatus(StatusPedido status, Pageable pageable);

    Page<Pedido> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

    @Query("""
        SELECT p.status AS status, COUNT(p) AS total, COALESCE(SUM(p.valorTotal), 0) AS receita
        FROM Pedido p
        GROUP BY p.status
        """)
    List<StatusCount> contarPorStatus();

    interface StatusCount {
        StatusPedido getStatus();
        Long getTotal();
        java.math.BigDecimal getReceita();
    }

    @Modifying
    @Transactional
    @Query("UPDATE Pedido p SET p.qrcodeUsado = true, p.qrcodeUsadoEm = CURRENT_TIMESTAMP " +
           "WHERE p.qrcodeToken = :token AND p.status = com.pixevent.entity.StatusPedido.PAGO AND p.qrcodeUsado = false")
    int marcarQrcodeUsadoNativo(@Param("token") String token);
}
