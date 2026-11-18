package cl.duoc.motostore.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductoDTO(
    Integer idProducto,
    String nombreProducto,
    String descripcion,
    BigDecimal precio,
    BigDecimal precioOferta,
    Integer stock,
    String imagenUrl,
    LocalDateTime fechaCreacion,
    CategoriaDTO categoria
) {
}
