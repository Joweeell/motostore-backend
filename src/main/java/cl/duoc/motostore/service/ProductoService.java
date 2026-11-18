
package cl.duoc.motostore.service;

import cl.duoc.motostore.dto.ProductoDTO;
import cl.duoc.motostore.dto.CategoriaDTO;
import cl.duoc.motostore.model.Producto;
import cl.duoc.motostore.model.Categoria;
import cl.duoc.motostore.repository.ProductoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(
            ProductoRepository productoRepository) {

        this.productoRepository = productoRepository;
    }

    public List<ProductoDTO> listarTodos() {

        return productoRepository.findAll()
                .stream()
                .map(this::convertirDTO)
                .toList();
    }

    public ProductoDTO buscarPorId(Integer id) {

        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Producto no encontrado"
                ));

        return convertirDTO(producto);
    }

    public List<ProductoDTO> buscarPorCategoria(Integer id) {

        return productoRepository
                .findByCategoria_IdCategoria(id)
                .stream()
                .map(this::convertirDTO)
                .toList();
    }

    public List<ProductoDTO> buscarPorNombre(String nombre) {

        return productoRepository
                .findByNombreProductoContainingIgnoreCase(nombre)
                .stream()
                .map(this::convertirDTO)
                .toList();
    }

    public List<ProductoDTO> listarDisponibles() {

        return productoRepository
                .findByStockGreaterThan(0)
                .stream()
                .map(this::convertirDTO)
                .toList();
    }

    public List<ProductoDTO> listarOfertas() {

        return productoRepository
                .findByPrecioOfertaIsNotNull()
                .stream()
                .map(this::convertirDTO)
                .toList();
    }

    private ProductoDTO convertirDTO(Producto producto) {

        Categoria categoria = producto.getCategoria();

        CategoriaDTO categoriaDTO = new CategoriaDTO(
                categoria.getIdCategoria(),
                categoria.getNombreCategoria(),
                categoria.getDescripcion()
        );

        return new ProductoDTO(
                producto.getIdProducto(),
                producto.getNombreProducto(),
                producto.getDescripcion(),
                producto.getPrecio(),
                producto.getPrecioOferta(),
                producto.getStock(),
                producto.getImagenUrl(),
                producto.getFechaCreacion(),
                categoriaDTO
        );
    }
}
