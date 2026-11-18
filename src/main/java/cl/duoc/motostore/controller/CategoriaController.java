
package cl.duoc.motostore.controller;

import cl.duoc.motostore.dto.CategoriaDTO;
import cl.duoc.motostore.service.CategoriaService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            @PathVariable Integer id) {

        return ResponseEntity.ok(
                categoriaService.buscarPorId(id)
        );
    }
}
