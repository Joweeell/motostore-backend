package cl.duoc.motostore.repository;

import cl.duoc.motostore.model.Usuario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository
        extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdUsuarioNot(
            String email,
            Integer idUsuario
    );

    @EntityGraph(attributePaths = "rol")
    List<Usuario> findAllByOrderByIdUsuarioAsc();

    @EntityGraph(attributePaths = "rol")
    Optional<Usuario> findWithRolByIdUsuario(Integer idUsuario);
}
