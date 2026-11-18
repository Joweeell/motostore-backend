
package cl.duoc.motostore.repository;

import cl.duoc.motostore.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoRepository
        extends JpaRepository<Producto, Integer> {

    List<Producto> findByCategoria_IdCategoria(
        Integer idCategoria
    );

    List<Producto> findByNombreProductoContainingIgnoreCase(
        String nombreProducto
    );

    List<Producto> findByStockGreaterThan(
        Integer stock
    );

    List<Producto> findByPrecioOfertaIsNotNull();

    boolean existsByCategoria_IdCategoria(
        Integer idCategoria
    );
}
