package com.pixevent.repository;

import com.pixevent.entity.Rifa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RifaRepository extends JpaRepository<Rifa, Integer> {

    List<Rifa> findByPedidoId(String pedidoId);

    @Query("SELECT COALESCE(MAX(r.numero), 0) + 1 FROM Rifa r")
    Integer proximoNumero();
}
