package cl.duoc.motostore.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

public record PedidoRequest(

    @NotBlank(message = "La direccion es obligatoria")
    @Size(max = 1000)
    String direccionEnvio,

    @NotBlank(message = "La ciudad es obligatoria")
    @Size(max = 100)
    String ciudad,

    @NotEmpty(message = "El pedido debe contener productos")
    @Size(max = 100)
    List<@NotNull @Valid PedidoDetalleRequest> detalles

) {
}