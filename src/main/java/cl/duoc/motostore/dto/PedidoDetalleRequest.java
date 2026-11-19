package cl.duoc.motostore.dto;

import jakarta.validation.constraints.*;

public record PedidoDetalleRequest(

    @NotNull(message = "El producto es obligatorio")
    @Positive
    Integer idProducto,

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor a cero")
    @Max(value = 1000, message = "Cantidad maxima excedida")
    Integer cantidad

) {
}