package com.pixevent.repository;

import com.pixevent.entity.Mesa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import jakarta.persistence.LockModeType;
import java.util.List;

public interface MesaRepository extends JpaRepository<Mesa, String> {

    List<Mesa> findAllByOrderByNumeroAsc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM Mesa m WHERE m.status = com.pixevent.entity.StatusMesa.DISPONIVEL ORDER BY m.numero ASC")
    List<Mesa> findDisponiveisParaAtualizar();

    default List<String> findNumerosDisponiveis(int quantidade) {
        return findDisponiveisParaAtualizar().stream()
                .limit(quantidade)
                .map(Mesa::getNumero)
                .toList();
    }
}
