package cl.duoc.motostore.controller;

import cl.duoc.motostore.dto.AdminUsuarioCreateRequest;
import cl.duoc.motostore.dto.AdminUsuarioUpdateRequest;
import cl.duoc.motostore.dto.UsuarioDTO;
import cl.duoc.motostore.service.UsuarioService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/usuarios")
public class AdminUsuarioController {

    private final UsuarioService usuarioService;

    public AdminUsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public ResponseEntity<List<UsuarioDTO>> listarTodos() {
        return ResponseEntity.ok(
                usuarioService.listarTodos()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioDTO> buscarPorId(
            @PathVariable("id") Integer id) {
        return ResponseEntity.ok(
                usuarioService.buscarPorId(id)
        );
    }

    @PostMapping
    public ResponseEntity<UsuarioDTO> crear(
            @Valid @RequestBody AdminUsuarioCreateRequest request) {

        UsuarioDTO creado = usuarioService.crearPorAdmin(request);

        return ResponseEntity
                .created(URI.create("/api/v1/usuarios/" + creado.idUsuario()))
                .body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioDTO> actualizar(
            @PathVariable("id") Integer id,
            @Valid @RequestBody AdminUsuarioUpdateRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        Integer idAdminActual = Integer.valueOf(jwt.getSubject());

        return ResponseEntity.ok(
                usuarioService.actualizarPorAdmin(
                        id,
                        request,
                        idAdminActual
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable("id") Integer id,
            @AuthenticationPrincipal Jwt jwt) {

        Integer idAdminActual = Integer.valueOf(jwt.getSubject());

        usuarioService.eliminarPorAdmin(id, idAdminActual);

        return ResponseEntity.noContent().build();
    }
}
