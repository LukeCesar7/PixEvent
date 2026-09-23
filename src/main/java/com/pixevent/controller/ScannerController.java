package com.pixevent.controller;

import com.pixevent.entity.Pedido;
import com.pixevent.entity.Rifa;
import com.pixevent.entity.StatusPedido;
import com.pixevent.repository.PedidoRepository;
import com.pixevent.service.RifaService;
import com.pixevent.util.ValidationUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Protegido pelo AdminAuthInterceptor (veja WebConfig).
 */
@RestController
@RequestMapping("/api/scanner")
public class ScannerController {

    private final PedidoRepository pedidoRepository;
    private final RifaService rifaService;

    public ScannerController(PedidoRepository pedidoRepository, RifaService rifaService) {
        this.pedidoRepository = pedidoRepository;
        this.rifaService = rifaService;
    }

    @PostMapping("/validar")
    public Map<String, Object> validar(@RequestBody Map<String, Object> body) {
        String token = ValidationUtil.cleanString(body.get("token"), 120);
        Map<String, Object> resp = new HashMap<>();

        if (token.isEmpty()) {
            resp.put("valido", false);
            resp.put("erro", "Token não informado.");
            return resp;
        }

        Pedido pedido = pedidoRepository.findByQrcodeToken(token).orElse(null);
        if (pedido == null) {
            resp.put("valido", false);
            resp.put("motivo", "QR Code não encontrado.");
            return resp;
        }

        if (pedido.getStatus() != StatusPedido.PAGO) {
            resp.put("valido", false);
            resp.put("motivo", "Pagamento com status: " + pedido.getStatus().getValor() + ".");
            return resp;
        }

        if (Boolean.TRUE.equals(pedido.getQrcodeUsado())) {
            resp.put("valido", false);
            resp.put("motivo", "QR Code já utilizado.");
            resp.put("usado_em", pedido.getQrcodeUsadoEm());
            resp.put("nome", pedido.getNome());
            return resp;
        }

        int marcados = pedidoRepository.marcarQrcodeUsadoNativo(token);
        if (marcados == 0) {
            resp.put("valido", false);
            resp.put("motivo", "QR Code já utilizado.");
            resp.put("usado_em", pedido.getQrcodeUsadoEm());
            resp.put("nome", pedido.getNome());
            return resp;
        }

        var rifas = rifaService.findByPedido(pedido.getId()).stream().map(Rifa::getNumero).toList();

        resp.put("valido", true);
        resp.put("nome", pedido.getNome());
        resp.put("cpf", pedido.getCpf());
        resp.put("produto", pedido.getProduto().getValor());
        resp.put("mesa", pedido.getMesaNumero());
        resp.put("itens_json", pedido.getItensJson());
        resp.put("rifas", rifas);
        resp.put("pago_em", pedido.getPagoEm());
        return resp;
    }
}
