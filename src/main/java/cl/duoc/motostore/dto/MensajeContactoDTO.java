package cl.duoc.motostore.dto;

import java.time.LocalDateTime;

public record MensajeContactoDTO(
        Integer idMensaje,
        String nombre,
        String email,
        String asunto,
        String mensaje,
        LocalDateTime fechaEnvio
) {
}