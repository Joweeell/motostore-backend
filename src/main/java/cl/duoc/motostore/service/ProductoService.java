package cl.duoc.motostore.service;

import cl.duoc.motostore.dto.ProductoDTO;
import cl.duoc.motostore.dto.CategoriaDTO;
import cl.duoc.motostore.dto.ProductoRequest;
import cl.duoc.motostore.model.Producto;
import cl.duoc.motostore.model.Categoria;
import cl.duoc.motostore.repository.ProductoRepository;
import cl.duoc.motostore.repository.CategoriaRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public ProductoService(
            ProductoRepository productoRepository,
            CategoriaRepository categoriaRepository) {

        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    // Listar todos los productos
    public List<ProductoDTO> listarTodos() {

        return productoRepository.findAll()
                .stream()
                .map(this::convertirDTO)
                .toList();
    }

    // Buscar producto por ID
    public ProductoDTO buscarPorId(Integer id) {

        return convertirDTO(obtenerProducto(id));
    }

    // Buscar productos por categoria
    public List<ProductoDTO> buscarPorCategoria(Integer id) {

        return productoRepository
                .findByCategoria_IdCategoria(id)
                .stream()
                .map(this::convertirDTO)
                .toList();
    }

    // Buscar productos por nombre
    public List<ProductoDTO> buscarPorNombre(String nombre) {

        return productoRepository
                .findByNombreProductoContainingIgnoreCase(nombre)
                .stream()
                .map(this::convertirDTO)
                .toList();
    }

    // Listar productos con stock disponible
    public List<ProductoDTO> listarDisponibles() {

        return productoRepository
                .findByStockGreaterThan(0)
                .stream()
                .map(this::convertirDTO)
                .toList();
    }

    // Listar productos en oferta
    public List<ProductoDTO> listarOfertas() {

        return productoRepository
                .findByPrecioOfertaIsNotNull()
                .stream()
                .map(this::convertirDTO)
                .toList();
    }

    // Crear producto
    @Transactional
    public ProductoDTO crear(ProductoRequest request) {

        Producto producto = new Producto();

        asignarDatos(producto, request);

        Producto guardado =
                productoRepository.saveAndFlush(producto);

        entityManager.refresh(guardado);

        return convertirDTO(guardado);
    }

    // Actualizar producto con bloqueo de registro
    @Transactional
    public ProductoDTO actualizar(
            Integer id,
            ProductoRequest request) {

        Producto producto = obtenerProductoConBloqueo(id);

        asignarDatos(producto, request);

        return convertirDTO(
                productoRepository.saveAndFlush(producto)
        );
    }

    // Eliminar producto con bloqueo de registro
    @Transactional
    public void eliminar(Integer id) {

        Producto producto = obtenerProductoConBloqueo(id);

        try {

            productoRepository.delete(producto);
            productoRepository.flush();

        } catch (DataIntegrityViolationException e) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se puede eliminar un producto asociado a pedidos"
            );
        }
    }

    // Obtener producto para operaciones de lectura
    private Producto obtenerProducto(Integer id) {

        return productoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Producto no encontrado"
                ));
    }

    // Obtener producto con bloqueo para operaciones de escritura
    private Producto obtenerProductoConBloqueo(Integer id) {

        return productoRepository
                .buscarPorIdConBloqueo(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Producto no encontrado"
                ));
    }

    // Validar y asignar los datos del producto
    private void asignarDatos(
            Producto producto,
            ProductoRequest request) {

        if (request.precioOferta() != null
                && request.precioOferta()
                        .compareTo(request.precio()) >= 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El precio de oferta debe ser menor al precio normal"
            );
        }

        Categoria categoria = categoriaRepository
                .findById(request.idCategoria())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Categoria no encontrada"
                ));

        producto.setCategoria(categoria);

        producto.setNombreProducto(
                request.nombreProducto().trim()
        );

        producto.setDescripcion(request.descripcion());
        producto.setPrecio(request.precio());
        producto.setPrecioOferta(request.precioOferta());
        producto.setStock(request.stock());
        producto.setImagenUrl(request.imagenUrl());
    }

    // Convertir entidad Producto a DTO
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