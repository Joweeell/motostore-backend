
package cl.duoc.motostore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaRequest(

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100)
    String nombreCategoria,

    @Size(max = 4000)
    String descripcion

) {
}
