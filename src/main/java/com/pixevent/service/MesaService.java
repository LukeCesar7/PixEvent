package com.pixevent.service;

import com.pixevent.entity.Mesa;
import com.pixevent.entity.PedidoMesa;
import com.pixevent.entity.StatusMesa;
import com.pixevent.repository.MesaRepository;
import com.pixevent.repository.PedidoMesaRepository;
import com.pixevent.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Porta de src/repositories/MesaRepository.js. No Node esse arquivo já continha
 * regra de negócio (reservar/liberar mesas), então virou um @Service aqui,
 * não um repositório JPA puro.
 */
@Service
public class MesaService {

    private final MesaRepository mesaRepository;
    private final PedidoMesaRepository pedidoMesaRepository;
    private final PedidoRepository pedidoRepository;

    public MesaService(MesaRepository mesaRepository, PedidoMesaRepository pedidoMesaRepository,
                        PedidoRepository pedidoRepository) {
        this.mesaRepository = mesaRepository;
        this.pedidoMesaRepository = pedidoMesaRepository;
        this.pedidoRepository = pedidoRepository;
    }

    public List<Mesa> findAll() {
        return mesaRepository.findAllByOrderByNumeroAsc();
    }

    public Optional<Mesa> findByNumero(String numero) {
        return mesaRepository.findById(numero);
    }

    @Transactional
    public List<String> findDisponiveis(int quantidade) {
        return mesaRepository.findNumerosDisponiveis(quantidade);
    }

    @Transactional
    public List<String> getMesasDoPedido(String pedidoId) {
        return pedidoMesaRepository.findByPedidoId(pedidoId).stream()
                .map(pm -> pm.getMesa().getNumero())
                .toList();
    }

    /** Reserva uma mesa específica para um pedido. Retorna false se ela não estava disponível. */
    @Transactional
    public boolean reservar(String numero, String pedidoId) {
        Mesa mesa = mesaRepository.findById(numero).orElse(null);
        if (mesa == null || mesa.getStatus() != StatusMesa.DISPONIVEL) return false;

        OffsetDateTime agora = OffsetDateTime.now();
        mesa.setStatus(StatusMesa.RESERVADA);
        mesa.setPedidoId(pedidoId);
        mesa.setReservadoEm(agora);
        mesaRepository.save(mesa);

        var pedido = pedidoRepository.findById(pedidoId).orElseThrow();
        PedidoMesa vinculo = PedidoMesa.builder()
                .pedido(pedido)
                .mesa(mesa)
                .reservadoEm(agora)
                .build();
        pedidoMesaRepository.save(vinculo);
        return true;
    }

    @Transactional
    public int reservarMuitas(List<String> numeros, String pedidoId) {
        int total = 0;
        for (String numero : numeros) {
            if (reservar(numero, pedidoId)) total++;
        }
        return total;
    }

    @Transactional
    public int garantirReservadasParaPedido(List<String> numeros, String pedidoId) {
        int total = 0;
        for (String numero : numeros) {
            boolean jaVinculada = pedidoMesaRepository.findByPedidoIdAndMesaNumero(pedidoId, numero).isPresent();
            if (jaVinculada) {
                total++;
                continue;
            }
            Mesa mesa = mesaRepository.findById(numero).orElse(null);
            if (mesa == null) continue;
            if (mesa.getStatus() == StatusMesa.DISPONIVEL) {
                if (reservar(numero, pedidoId)) total++;
            }
        }
        return total;
    }

    @Transactional
    public void liberar(String numero) {
        pedidoMesaRepository.deleteByMesa_Numero(numero);
        mesaRepository.findById(numero).ifPresent(mesa -> {
            mesa.setStatus(StatusMesa.DISPONIVEL);
            mesa.setPedidoId(null);
            mesa.setReservadoEm(null);
            mesaRepository.save(mesa);
        });
    }

    @Transactional
    public void liberarMuitas(List<String> numeros) {
        for (String numero : numeros) liberar(numero);
    }

    @Transactional
    public int liberarDoPedido(String pedidoId) {
        List<String> numeros = getMesasDoPedido(pedidoId);
        if (!numeros.isEmpty()) liberarMuitas(numeros);
        return numeros.size();
    }
}
