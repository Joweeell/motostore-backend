package cl.duoc.motostore.controller;

import cl.duoc.motostore.dto.PedidoDTO;
import cl.duoc.motostore.dto.PedidoRequest;
import cl.duoc.motostore.dto.PedidoEstadoRequest;
import cl.duoc.motostore.service.PedidoService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    // Crear un pedido como cliente
    @PostMapping
    public ResponseEntity<PedidoDTO> crearPedido(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody PedidoRequest request) {

        Integer idUsuario = Integer.valueOf(
                jwt.getSubject()
        );

        PedidoDTO pedido = pedidoService.crearPedido(
                idUsuario,
                request
        );

        return ResponseEntity
                .created(URI.create(
                        "/api/v1/pedidos/mis-pedidos/"
                        + pedido.idPedido()
                ))
                .body(pedido);
    }

    // Consultar todos los pedidos
    // Administrador y vendedor
    @GetMapping
    public ResponseEntity<List<PedidoDTO>> listarTodos() {

        return ResponseEntity.ok(
                pedidoService.listarTodos()
        );
    }

    // Consultar un pedido por ID
    // Administrador y vendedor
    @GetMapping("/{id}")
    public ResponseEntity<PedidoDTO> buscarPorId(
            @PathVariable("id") Integer id) {

        return ResponseEntity.ok(
                pedidoService.buscarPorId(id)
        );
    }

    // Consultar historial de compras del cliente
    @GetMapping("/mis-pedidos")
    public ResponseEntity<List<PedidoDTO>> listarMisPedidos(
            @AuthenticationPrincipal Jwt jwt) {

        Integer idUsuario = Integer.valueOf(
                jwt.getSubject()
        );

        return ResponseEntity.ok(
                pedidoService.listarPorUsuario(idUsuario)
        );
    }

    // Consultar el detalle de un pedido propio
    @GetMapping("/mis-pedidos/{id}")
    public ResponseEntity<PedidoDTO> buscarPedidoPropio(
            @PathVariable("id") Integer id,
            @AuthenticationPrincipal Jwt jwt) {

        Integer idUsuario = Integer.valueOf(
                jwt.getSubject()
        );

        return ResponseEntity.ok(
                pedidoService.buscarPedidoPropio(
                        id,
                        idUsuario
                )
        );
    }

    // Cambiar estado del pedido como administrador
    @PutMapping("/{id}/estado")
    public ResponseEntity<PedidoDTO> cambiarEstado(
            @PathVariable("id") Integer id,
            @Valid @RequestBody PedidoEstadoRequest request) {

        return ResponseEntity.ok(
                pedidoService.cambiarEstado(
                        id,
                        request.estadoPago()
                )
        );
    }
}