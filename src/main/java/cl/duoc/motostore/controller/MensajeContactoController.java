package cl.duoc.motostore.controller;

import cl.duoc.motostore.dto.MensajeContactoDTO;
import cl.duoc.motostore.dto.MensajeContactoRequest;
import cl.duoc.motostore.service.MensajeContactoService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/contacto")
public class MensajeContactoController {

    private final MensajeContactoService contactoService;

    public MensajeContactoController(
            MensajeContactoService contactoService) {
        this.contactoService = contactoService;
    }

    // Enviar mensaje desde formulario publico
    @PostMapping
    public ResponseEntity<MensajeContactoDTO> enviar(
            @Valid @RequestBody MensajeContactoRequest request) {

        MensajeContactoDTO creado = contactoService.crear(request);

        return ResponseEntity
                .created(URI.create("/api/v1/contacto/" + creado.idMensaje()))
                .body(creado);
    }

    // Listar mensajes recibidos (Admin y Vendedor)
    @GetMapping
    public ResponseEntity<List<MensajeContactoDTO>> listarTodos() {
        return ResponseEntity.ok(
                contactoService.listarTodos()
        );
    }

    // Consultar detalle de un mensaje (Admin y Vendedor)
    @GetMapping("/{id}")
    public ResponseEntity<MensajeContactoDTO> buscarPorId(
            @PathVariable("id") Integer id) {
        return ResponseEntity.ok(
                contactoService.buscarPorId(id)
        );
    }

    // Eliminar mensaje (Solo Admin)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable("id") Integer id) {
        contactoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}