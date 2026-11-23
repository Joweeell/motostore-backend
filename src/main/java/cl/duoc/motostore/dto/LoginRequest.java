
package cl.duoc.motostore.dto;

import jakarta.validation.constraints.*;

public record LoginRequest(

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no es valido")
    @Size(max = 150)
    String email,

    @NotBlank(message = "La contrasena es obligatoria")
    @Size(max = 72)
    String password

) {
}
