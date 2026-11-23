
package cl.duoc.motostore.controller;

import cl.duoc.motostore.dto.ProductoDTO;
import cl.duoc.motostore.dto.ProductoRequest;
import cl.duoc.motostore.service.ProductoService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(
            ProductoService productoService) {

        this.productoService = productoService;
    }

    @GetMapping
    public ResponseEntity<List<ProductoDTO>> listarTodos() {

        return ResponseEntity.ok(
                productoService.listarTodos()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoDTO> buscarPorId(
            @PathVariable("id") Integer id) {

        return ResponseEntity.ok(
                productoService.buscarPorId(id)
        );
    }

    @GetMapping("/categoria/{id}")
    public ResponseEntity<List<ProductoDTO>> buscarPorCategoria(
            @PathVariable("id") Integer id) {

        return ResponseEntity.ok(
                productoService.buscarPorCategoria(id)
        );
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<ProductoDTO>> buscarPorNombre(
            @RequestParam("nombre") String nombre) {

        return ResponseEntity.ok(
                productoService.buscarPorNombre(nombre)
        );
    }

    @GetMapping("/disponibles")
    public ResponseEntity<List<ProductoDTO>> listarDisponibles() {

        return ResponseEntity.ok(
                productoService.listarDisponibles()
        );
    }

    @GetMapping("/ofertas")
    public ResponseEntity<List<ProductoDTO>> listarOfertas() {

        return ResponseEntity.ok(
                productoService.listarOfertas()
        );
    }

    @PostMapping
    public ResponseEntity<ProductoDTO> crear(
            @Valid @RequestBody ProductoRequest request) {

        ProductoDTO creado = productoService.crear(request);

        return ResponseEntity
                .created(URI.create(
                        "/api/v1/productos/" + creado.idProducto()
                ))
                .body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductoDTO> actualizar(
            @PathVariable("id") Integer id,
            @Valid @RequestBody ProductoRequest request) {

        return ResponseEntity.ok(
                productoService.actualizar(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable("id") Integer id) {

        productoService.eliminar(id);

        return ResponseEntity.noContent().build();
    }
}
