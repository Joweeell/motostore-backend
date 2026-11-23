package cl.duoc.motostore.service;

import cl.duoc.motostore.dto.AdminUsuarioCreateRequest;
import cl.duoc.motostore.dto.AdminUsuarioUpdateRequest;
import cl.duoc.motostore.dto.RegistroRequest;
import cl.duoc.motostore.dto.UsuarioDTO;
import cl.duoc.motostore.model.Rol;
import cl.duoc.motostore.model.Usuario;
import cl.duoc.motostore.repository.RolRepository;
import cl.duoc.motostore.repository.UsuarioRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

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

        String email = normalizarEmail(request.email());

        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El correo ya se encuentra registrado"
            );
        }

        validarLongitudPassword(request.password(), false);

        Rol rol = obtenerRol("cliente");

        Usuario usuario = new Usuario();
        usuario.setNombre(request.nombre().trim());
        usuario.setApellido(request.apellido().trim());
        usuario.setEmail(email);
        usuario.setTelefono(normalizarTelefono(request.telefono()));
        usuario.setRol(rol);
        usuario.setPasswordHash(
                passwordEncoder.encode(request.password())
        );

        return guardarYRefrescar(usuario);
    }

    public List<UsuarioDTO> listarTodos() {
        return usuarioRepository
                .findAllByOrderByIdUsuarioAsc()
                .stream()
                .map(this::convertirDTO)
                .toList();
    }

    public UsuarioDTO buscarPorId(Integer idUsuario) {
        Usuario usuario = usuarioRepository
                .findWithRolByIdUsuario(idUsuario)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuario no encontrado"
                ));

        return convertirDTO(usuario);
    }

    @Transactional
    public UsuarioDTO crearPorAdmin(AdminUsuarioCreateRequest request) {

        String email = normalizarEmail(request.email());

        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El correo ya se encuentra registrado"
            );
        }

        validarLongitudPassword(request.password(), false);

        Rol rol = obtenerRol(request.rol());

        Usuario usuario = new Usuario();
        usuario.setNombre(request.nombre().trim());
        usuario.setApellido(request.apellido().trim());
        usuario.setEmail(email);
        usuario.setTelefono(normalizarTelefono(request.telefono()));
        usuario.setRol(rol);
        usuario.setPasswordHash(
                passwordEncoder.encode(request.password())
        );

        return guardarYRefrescar(usuario);
    }

    @Transactional
    public UsuarioDTO actualizarPorAdmin(
            Integer idUsuario,
            AdminUsuarioUpdateRequest request,
            Integer idAdminActual) {

        Usuario usuario = usuarioRepository
                .findWithRolByIdUsuario(idUsuario)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuario no encontrado"
                ));

        String email = normalizarEmail(request.email());

        if (usuarioRepository.existsByEmailIgnoreCaseAndIdUsuarioNot(
                email, idUsuario)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El correo ya esta en uso por otro usuario"
            );
        }

        Rol rol = obtenerRol(request.rol());

        if (idUsuario.equals(idAdminActual)
                && !"admin".equalsIgnoreCase(rol.getNombreRol())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No puedes quitar tu propio rol de administrador"
            );
        }

        usuario.setNombre(request.nombre().trim());
        usuario.setApellido(request.apellido().trim());
        usuario.setEmail(email);
        usuario.setTelefono(normalizarTelefono(request.telefono()));
        usuario.setRol(rol);

        if (request.password() != null && !request.password().isBlank()) {
            validarLongitudPassword(request.password(), true);
            usuario.setPasswordHash(
                    passwordEncoder.encode(request.password())
            );
        }

        return guardarYRefrescar(usuario);
    }

    @Transactional
    public void eliminarPorAdmin(
            Integer idUsuario,
            Integer idAdminActual) {

        if (idUsuario.equals(idAdminActual)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No puedes eliminar tu propio usuario"
            );
        }

        Usuario usuario = usuarioRepository
                .findById(idUsuario)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuario no encontrado"
                ));

        try {
            usuarioRepository.delete(usuario);
            usuarioRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se puede eliminar el usuario porque tiene pedidos asociados"
            );
        }
    }

    private UsuarioDTO guardarYRefrescar(Usuario usuario) {
        try {
            Usuario guardado = usuarioRepository.saveAndFlush(usuario);
            entityManager.refresh(guardado);
            return convertirDTO(guardado);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se pudo guardar el usuario"
            );
        }
    }

    private Rol obtenerRol(String nombreRol) {
        String limpio = nombreRol == null
                ? ""
                : nombreRol.trim().toLowerCase(Locale.ROOT);

        return rolRepository
                .findByNombreRolIgnoreCase(limpio)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Rol no valido: " + nombreRol
                ));
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizarTelefono(String telefono) {
        if (telefono == null || telefono.isBlank()) {
            return null;
        }
        return telefono.trim();
    }

    private void validarLongitudPassword(
            String password,
            boolean validarMinimo) {

        if (validarMinimo && password.trim().length() < 6) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La contrasena debe tener al menos 6 caracteres"
            );
        }

        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La contrasena supera el limite permitido"
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
