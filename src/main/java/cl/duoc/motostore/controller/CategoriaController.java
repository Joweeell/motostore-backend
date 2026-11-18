
package cl.duoc.motostore.controller;

import cl.duoc.motostore.dto.CategoriaDTO;
import cl.duoc.motostore.dto.CategoriaRequest;
import cl.duoc.motostore.service.CategoriaService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(
            CategoriaService categoriaService) {

        this.categoriaService = categoriaService;
    }

    @GetMapping
    public ResponseEntity<List<CategoriaDTO>> listarTodas() {

        return ResponseEntity.ok(
                categoriaService.listarTodas()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaDTO> buscarPorId(
            @PathVariable("id") Integer id) {

        return ResponseEntity.ok(
                categoriaService.buscarPorId(id)
        );
    }

    @PostMapping
    public ResponseEntity<CategoriaDTO> crear(
            @Valid @RequestBody CategoriaRequest request) {

        CategoriaDTO creada = categoriaService.crear(request);

        return ResponseEntity
                .created(URI.create(
                        "/api/v1/categorias/" + creada.idCategoria()
                ))
                .body(creada);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoriaDTO> actualizar(
            @PathVariable("id") Integer id,
            @Valid @RequestBody CategoriaRequest request) {

        return ResponseEntity.ok(
                categoriaService.actualizar(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable("id") Integer id) {

        categoriaService.eliminar(id);

        return ResponseEntity.noContent().build();
    }
}
