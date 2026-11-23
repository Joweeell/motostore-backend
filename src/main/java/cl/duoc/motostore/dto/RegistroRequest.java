
package cl.duoc.motostore.dto;

import jakarta.validation.constraints.*;

public record RegistroRequest(

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100)
    String nombre,

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 100)
    String apellido,

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no es valido")
    @Size(max = 150)
    String email,

    @NotBlank(message = "La contrasena es obligatoria")
    @Size(
        min = 8,
        max = 72,
        message = "La contrasena debe tener entre 8 y 72 caracteres"
    )
    String password,

    @Size(max = 20)
    String telefono

) {
}
