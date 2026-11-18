
package cl.duoc.motostore.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ProductoRequest(

    @NotNull(message = "La categoria es obligatoria")
    @Positive
    Integer idCategoria,

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 150)
    String nombreProducto,

    @Size(max = 4000)
    String descripcion,

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin("0.01")
    @Digits(integer = 8, fraction = 2)
    BigDecimal precio,

    @DecimalMin("0.01")
    @Digits(integer = 8, fraction = 2)
    BigDecimal precioOferta,

    @NotNull(message = "El stock es obligatorio")
    @PositiveOrZero(message = "El stock no puede ser negativo")
    Integer stock,

    @Size(max = 255)
    String imagenUrl

) {
}
