package cl.duoc.motostore.service;

import cl.duoc.motostore.dto.LoginRequest;
import cl.duoc.motostore.dto.LoginResponse;
import cl.duoc.motostore.dto.UsuarioDTO;
import cl.duoc.motostore.model.Rol;
import cl.duoc.motostore.model.Usuario;
import cl.duoc.motostore.repository.RolRepository;
import cl.duoc.motostore.repository.UsuarioRepository;
import cl.duoc.motostore.security.JwtService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.nio.charset.StandardCharsets;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final String hashAuxiliar;

    @PersistenceContext
    private EntityManager entityManager;

    public AuthService(
            UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;

        this.hashAuxiliar = passwordEncoder.encode(
                UUID.randomUUID().toString()
        );
    }

    public LoginResponse login(LoginRequest request) {

        String email = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (request.password()
                .getBytes(StandardCharsets.UTF_8).length > 72) {

            throw credencialesInvalidas();
        }

        Usuario usuario = usuarioRepository
                .findByEmailIgnoreCase(email)
                .orElse(null);

        String hash = usuario != null
                ? usuario.getPasswordHash()
                : hashAuxiliar;

        boolean passwordCorrecta = passwordEncoder.matches(
                request.password(),
                hash
        );

        if (usuario == null || !passwordCorrecta) {
            throw credencialesInvalidas();
        }

        String token = jwtService.generarToken(usuario);

        UsuarioDTO usuarioDTO = new UsuarioDTO(
                usuario.getIdUsuario(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getEmail(),
                usuario.getTelefono(),
                usuario.getRol().getNombreRol(),
                usuario.getFechaRegistro()
        );

        return new LoginResponse(
                token,
                "Bearer",
                3600,
                usuarioDTO
        );
    }

    @Transactional
    public Map<String, Object> loginConGoogle(String credential) {

        if (credential == null || credential.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No se recibió el token de Google"
            );
        }

        Map<?, ?> datosGoogle;
        try {
            RestTemplate restTemplate = new RestTemplate();
            String host = "oauth2." + "googleapis.com";
            String path = "/tokeninfo" + "?id_token=";
            String url = "https://" + host + path + credential;

            datosGoogle = restTemplate.getForObject(url, Map.class);
        } catch (RestClientException e) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Token de Google inválido o expirado"
            );
        }

        if (datosGoogle == null || datosGoogle.get("email") == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "No se pudo obtener el correo desde Google"
            );
        }

        String email = datosGoogle.get("email")
                .toString()
                .trim()
                .toLowerCase(Locale.ROOT);

        Usuario usuario = usuarioRepository
                .findByEmailIgnoreCase(email)
                .orElseGet(() -> registrarUsuarioDesdeGoogle(email, datosGoogle));

        String token = jwtService.generarToken(usuario);

        UsuarioDTO usuarioDTO = new UsuarioDTO(
                usuario.getIdUsuario(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getEmail(),
                usuario.getTelefono(),
                usuario.getRol().getNombreRol(),
                usuario.getFechaRegistro()
        );

        return Map.of(
                "ok", true,
                "token", token,
                "usuario", usuarioDTO
        );
    }

    private Usuario registrarUsuarioDesdeGoogle(String email, Map<?, ?> datosGoogle) {
        Rol rolCliente = rolRepository
                .findByNombreRolIgnoreCase("cliente")
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "No se encontró el rol cliente en la base de datos"
                ));

        String nombre = datosGoogle.get("given_name") != null
                ? datosGoogle.get("given_name").toString().trim()
                : email.split("@")[0];

        String apellido = datosGoogle.get("family_name") != null
                ? datosGoogle.get("family_name").toString().trim()
                : "Google";

        Usuario nuevo = new Usuario();
        nuevo.setNombre(nombre);
        nuevo.setApellido(apellido);
        nuevo.setEmail(email);
        nuevo.setTelefono(null);
        nuevo.setRol(rolCliente);
        nuevo.setPasswordHash(
                passwordEncoder.encode(UUID.randomUUID().toString())
        );

        Usuario guardado = usuarioRepository.saveAndFlush(nuevo);
        entityManager.refresh(guardado);
        return guardado;
    }

    private ResponseStatusException credencialesInvalidas() {

        return new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Correo o contrasena incorrectos"
        );
    }
}
