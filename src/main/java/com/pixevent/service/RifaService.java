package com.pixevent.service;

import com.pixevent.entity.Rifa;
import com.pixevent.repository.RifaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class RifaService {

    private final RifaRepository rifaRepository;

    public RifaService(RifaRepository rifaRepository) {
        this.rifaRepository = rifaRepository;
    }

    public List<Rifa> findByPedido(String pedidoId) {
        return rifaRepository.findByPedidoId(pedidoId);
    }

    public long contarTotal() {
        return rifaRepository.count();
    }

    @Transactional
    public List<Rifa> criarLote(String pedidoId, String cpf, String nome, int quantidade) {
        List<Rifa> rifas = new ArrayList<>();
        for (int i = 0; i < quantidade; i++) {
            Integer numero = rifaRepository.proximoNumero();
            Rifa rifa = Rifa.builder()
                    .pedidoId(pedidoId)
                    .numero(numero)
                    .cpf(cpf)
                    .nome(nome)
                    .build();
            rifas.add(rifaRepository.save(rifa));
        }
        return rifas;
    }
}
