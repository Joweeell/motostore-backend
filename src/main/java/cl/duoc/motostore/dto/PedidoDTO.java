package cl.duoc.motostore.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoDTO(

    Integer idPedido,
    Integer idUsuario,
    LocalDateTime fechaPedido,
    BigDecimal total,
    String estadoPago,
    String direccionEnvio,
    String ciudad,
    List<PedidoDetalleDTO> detalles

) {
}