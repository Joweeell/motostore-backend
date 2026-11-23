package cl.duoc.motostore.service;

import cl.duoc.motostore.dto.LoginRequest;
import cl.duoc.motostore.dto.LoginResponse;
import cl.duoc.motostore.dto.UsuarioDTO;
import cl.duoc.motostore.model.Usuario;
import cl.duoc.motostore.repository.UsuarioRepository;
import cl.duoc.motostore.security.JwtService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.UUID;
import java.nio.charset.StandardCharsets;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final String hashAuxiliar;

    public AuthService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.usuarioRepository = usuarioRepository;
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

    private ResponseStatusException credencialesInvalidas() {

        return new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Correo o contrasena incorrectos"
        );
    }
}