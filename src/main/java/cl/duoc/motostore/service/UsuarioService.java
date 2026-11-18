
package cl.duoc.motostore.service;

import cl.duoc.motostore.dto.RegistroRequest;
import cl.duoc.motostore.dto.UsuarioDTO;
import cl.duoc.motostore.model.Usuario;
import cl.duoc.motostore.model.Rol;
import cl.duoc.motostore.repository.UsuarioRepository;
import cl.duoc.motostore.repository.RolRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Locale;
import java.nio.charset.StandardCharsets;

@Service
@Transactional(readOnly = true)
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    @PersistenceContext
    private EntityManager entityManager;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            PasswordEncoder passwordEncoder) {

        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UsuarioDTO registrar(RegistroRequest request) {

        String email = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (usuarioRepository.existsByEmailIgnoreCase(email)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El correo ya se encuentra registrado"
            );
        }

        if (request.password()
                .getBytes(StandardCharsets.UTF_8).length > 72) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La contrasena supera el limite permitido"
            );
        }

        Rol rol = rolRepository
                .findByNombreRolIgnoreCase("cliente")
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "El rol cliente no esta configurado"
                ));

        Usuario usuario = new Usuario();

        usuario.setNombre(request.nombre().trim());
        usuario.setApellido(request.apellido().trim());
        usuario.setEmail(email);
        usuario.setTelefono(request.telefono());
        usuario.setRol(rol);

        usuario.setPasswordHash(
                passwordEncoder.encode(request.password())
        );

        try {

            Usuario guardado =
                    usuarioRepository.saveAndFlush(usuario);

            entityManager.refresh(guardado);

            return convertirDTO(guardado);

        } catch (DataIntegrityViolationException e) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se pudo registrar el usuario"
            );
        }
    }

    private UsuarioDTO convertirDTO(Usuario usuario) {

        return new UsuarioDTO(
                usuario.getIdUsuario(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getEmail(),
                usuario.getTelefono(),
                usuario.getRol().getNombreRol(),
                usuario.getFechaRegistro()
        );
    }
}
