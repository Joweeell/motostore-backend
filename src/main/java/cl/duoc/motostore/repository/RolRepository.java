
package cl.duoc.motostore.repository;

import cl.duoc.motostore.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RolRepository
        extends JpaRepository<Rol, Integer> {

    Optional<Rol> findByNombreRolIgnoreCase(
            String nombreRol
    );
}
