package cl.duoc.motostore.dto;

public record LoginResponse(

    String token,
    String tipoToken,
    long expiresIn,
    UsuarioDTO usuario

) {
}