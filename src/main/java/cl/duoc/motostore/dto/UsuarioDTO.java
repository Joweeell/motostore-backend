
package cl.duoc.motostore.dto;

import java.time.LocalDateTime;

public record UsuarioDTO(

    Integer idUsuario,
    String nombre,
    String apellido,
    String email,
    String telefono,
    String rol,
    LocalDateTime fechaRegistro

) {
}
