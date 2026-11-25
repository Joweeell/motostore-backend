package cl.duoc.motostore.controller;

import cl.duoc.motostore.dto.RegistroRequest;
import cl.duoc.motostore.dto.UsuarioDTO;
import cl.duoc.motostore.dto.LoginRequest;
import cl.duoc.motostore.dto.LoginResponse;

import cl.duoc.motostore.service.UsuarioService;
import cl.duoc.motostore.service.AuthService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final AuthService authService;

    public UsuarioController(
            UsuarioService usuarioService,
            AuthService authService) {

        this.usuarioService = usuarioService;
        this.authService = authService;
    }

    @PostMapping("/registro")
    public ResponseEntity<UsuarioDTO> registrar(
            @Valid @RequestBody RegistroRequest request) {

        UsuarioDTO usuario =
                usuarioService.registrar(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(usuario);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(
                authService.login(request)
        );
    }
        @PostMapping("/google")
    public ResponseEntity<Map<String, Object>> loginGoogle(
            @RequestBody Map<String, String> body) {

        String credential = body.get("credential");
        return ResponseEntity.ok(
                authService.loginConGoogle(credential)
        );
    }


    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(
            @AuthenticationPrincipal Jwt jwt) {

        return ResponseEntity.ok(Map.of(
                "idUsuario", jwt.getSubject(),
                "email", jwt.getClaimAsString("email"),
                "roles", jwt.getClaimAsStringList("roles")
        ));
    }
}