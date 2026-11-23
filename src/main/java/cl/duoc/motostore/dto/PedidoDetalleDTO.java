package cl.duoc.motostore.dto;

import java.math.BigDecimal;

public record PedidoDetalleDTO(

    Integer idDetalle,
    Integer idProducto,
    String nombreProducto,
    Integer cantidad,
    BigDecimal precioUnitario,
    BigDecimal subtotal

) {
}