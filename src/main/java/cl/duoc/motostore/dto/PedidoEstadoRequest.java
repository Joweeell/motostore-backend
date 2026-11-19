package cl.duoc.motostore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PedidoEstadoRequest(

    @NotBlank(message = "El estado es obligatorio")

    @Pattern(
        regexp = "pagado|cancelado",
        message = "El estado debe ser pagado o cancelado"
    )
    String estadoPago

) {
}