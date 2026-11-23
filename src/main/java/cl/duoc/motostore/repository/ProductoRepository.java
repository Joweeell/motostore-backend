package cl.duoc.motostore.repository;

import cl.duoc.motostore.model.Producto;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Producto p WHERE p.idProducto = :id")
    Optional<Producto> buscarPorIdConBloqueo(
            @Param("id") Integer id
    );
}