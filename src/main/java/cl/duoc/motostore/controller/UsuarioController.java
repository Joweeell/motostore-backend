
package cl.duoc.motostore.controller;

import cl.duoc.motostore.dto.RegistroRequest;
import cl.duoc.motostore.dto.UsuarioDTO;
import cl.duoc.motostore.service.UsuarioService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(
            UsuarioService usuarioService) {

        this.usuarioService = usuarioService;
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
}
