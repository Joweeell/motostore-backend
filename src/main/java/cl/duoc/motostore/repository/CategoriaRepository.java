
package cl.duoc.motostore.repository;

import cl.duoc.motostore.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoriaRepository
        extends JpaRepository<Categoria, Integer> {

    Optional<Categoria> findByNombreCategoria(
        String nombreCategoria
    );

    boolean existsByNombreCategoriaIgnoreCase(
        String nombreCategoria
    );
}
