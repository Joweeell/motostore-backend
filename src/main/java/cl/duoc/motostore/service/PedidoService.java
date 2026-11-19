package cl.duoc.motostore.service;

import cl.duoc.motostore.dto.PedidoDTO;
import cl.duoc.motostore.dto.PedidoRequest;
import cl.duoc.motostore.dto.PedidoDetalleDTO;
import cl.duoc.motostore.dto.PedidoDetalleRequest;

import cl.duoc.motostore.model.Pedido;
import cl.duoc.motostore.model.PedidoDetalle;
import cl.duoc.motostore.model.Producto;
import cl.duoc.motostore.model.Usuario;

import cl.duoc.motostore.repository.PedidoRepository;
import cl.duoc.motostore.repository.PedidoDetalleRepository;
import cl.duoc.motostore.repository.ProductoRepository;
import cl.duoc.motostore.repository.UsuarioRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.LockModeType;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
@Transactional(readOnly = true)
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final PedidoDetalleRepository detalleRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public PedidoService(
            PedidoRepository pedidoRepository,
            PedidoDetalleRepository detalleRepository,
            ProductoRepository productoRepository,
            UsuarioRepository usuarioRepository) {

        this.pedidoRepository = pedidoRepository;
        this.detalleRepository = detalleRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // Crear un nuevo pedido
    @Transactional
    public PedidoDTO crearPedido(
            Integer idUsuario,
            PedidoRequest request) {

        Usuario usuario = usuarioRepository
                .findById(idUsuario)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuario no encontrado"
                ));

        if (!"cliente".equalsIgnoreCase(
                usuario.getRol().getNombreRol())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Solo los clientes pueden realizar compras"
            );
        }

        Map<Integer, Integer> cantidades = new TreeMap<>();

        for (PedidoDetalleRequest item : request.detalles()) {

            cantidades.merge(
                    item.idProducto(),
                    item.cantidad(),
                    Integer::sum
            );
        }

        Pedido pedido = new Pedido();

        pedido.setUsuario(usuario);

        pedido.setDireccionEnvio(
                request.direccionEnvio().trim()
        );

        pedido.setCiudad(request.ciudad().trim());
        pedido.setEstadoPago("pendiente");

        BigDecimal total = BigDecimal.ZERO;

        List<PedidoDetalle> detalles = new ArrayList<>();

        for (Map.Entry<Integer, Integer> item
                : cantidades.entrySet()) {

            Integer idProducto = item.getKey();
            Integer cantidad = item.getValue();

            if (cantidad <= 0 || cantidad > 1000) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Cantidad de producto no valida"
                );
            }

            Producto producto = productoRepository
                    .buscarPorIdConBloqueo(idProducto)
                    .orElseThrow(() ->
                            new ResponseStatusException(
                                    HttpStatus.NOT_FOUND,
                                    "Producto no encontrado: "
                                            + idProducto
                            ));

            // Validar stock disponible
            if (producto.getStock() < cantidad) {

                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Stock insuficiente para: "
                                + producto.getNombreProducto()
                );
            }

            BigDecimal precioUnitario = producto.getPrecio();

            // Aplicar precio de oferta
            if (producto.getPrecioOferta() != null
                    && producto.getPrecioOferta().signum() > 0
                    && producto.getPrecioOferta()
                            .compareTo(precioUnitario) < 0) {

                precioUnitario = producto.getPrecioOferta();
            }

            BigDecimal subtotal = precioUnitario.multiply(
                    BigDecimal.valueOf(cantidad)
            );

            total = total.add(subtotal);

            PedidoDetalle detalle = new PedidoDetalle();

            detalle.setPedido(pedido);
            detalle.setProducto(producto);
            detalle.setCantidad(cantidad);
            detalle.setPrecioUnitario(precioUnitario);

            detalles.add(detalle);

            // Descontar las unidades compradas
            producto.setStock(
                    producto.getStock() - cantidad
            );

            productoRepository.save(producto);
        }

        // Validar limite de la columna DECIMAL(10,2)
        if (total.compareTo(
                new BigDecimal("99999999.99")) > 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El total del pedido supera el limite permitido"
            );
        }

        pedido.setTotal(total);

        Pedido guardado =
                pedidoRepository.saveAndFlush(pedido);

        List<PedidoDetalle> detallesGuardados =
                detalleRepository.saveAllAndFlush(detalles);

        entityManager.refresh(guardado);

        return convertirDTO(
                guardado,
                detallesGuardados
        );
    }

    // Listar todos los pedidos
    public List<PedidoDTO> listarTodos() {

        return pedidoRepository
                .findAllByOrderByFechaPedidoDesc()
                .stream()
                .map(this::convertirPedidoCompleto)
                .toList();
    }

    // Listar pedidos pertenecientes a un usuario
    public List<PedidoDTO> listarPorUsuario(
            Integer idUsuario) {

        return pedidoRepository
                .findByUsuario_IdUsuarioOrderByFechaPedidoDesc(
                        idUsuario
                )
                .stream()
                .map(this::convertirPedidoCompleto)
                .toList();
    }

    // Buscar un pedido por ID
    public PedidoDTO buscarPorId(Integer idPedido) {

        Pedido pedido = pedidoRepository
                .findById(idPedido)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Pedido no encontrado"
                ));

        return convertirPedidoCompleto(pedido);
    }

    // Buscar un pedido que pertenezca al cliente autenticado
    public PedidoDTO buscarPedidoPropio(
            Integer idPedido,
            Integer idUsuario) {

        Pedido pedido = pedidoRepository
                .findByIdPedidoAndUsuario_IdUsuario(
                        idPedido,
                        idUsuario
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Pedido no encontrado"
                ));

        return convertirPedidoCompleto(pedido);
    }

    // Cambiar estado de pago o cancelar un pedido
    @Transactional
    public PedidoDTO cambiarEstado(
            Integer idPedido,
            String nuevoEstado) {

        // Validar los estados permitidos
        if (!"pagado".equals(nuevoEstado)
                && !"cancelado".equals(nuevoEstado)) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Estado no valido"
            );
        }

        // Bloquear el pedido durante la transaccion
        Pedido pedido = pedidoRepository
                .buscarPorIdConBloqueo(idPedido)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Pedido no encontrado"
                ));

        // Evitar modificaciones de pedidos finalizados
        if (!"pendiente".equals(pedido.getEstadoPago())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Solo se pueden modificar pedidos pendientes"
            );
        }

        // Devolver stock cuando el pedido es cancelado
        if ("cancelado".equals(nuevoEstado)) {

            List<PedidoDetalle> detalles = detalleRepository
                    .findByPedido_IdPedidoOrderByIdDetalleAsc(
                            idPedido
                    );

            Map<Integer, Integer> cantidades = new TreeMap<>();

            for (PedidoDetalle detalle : detalles) {

                cantidades.merge(
                        detalle.getProducto().getIdProducto(),
                        detalle.getCantidad(),
                        Integer::sum
                );
            }

            for (Map.Entry<Integer, Integer> item
                    : cantidades.entrySet()) {

                Producto producto = productoRepository
                        .buscarPorIdConBloqueo(item.getKey())
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Producto no encontrado"
                                ));

                // Obtener el stock actualizado manteniendo el bloqueo
                entityManager.refresh(
                        producto,
                        LockModeType.PESSIMISTIC_WRITE
                );

                long nuevoStock = (long) producto.getStock()
                        + item.getValue();

                if (nuevoStock > Integer.MAX_VALUE) {

                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Limite de stock excedido"
                    );
                }

                // Devolver las unidades al inventario
                producto.setStock((int) nuevoStock);

                productoRepository.save(producto);
            }
        }

        // Actualizar el estado del pedido
        pedido.setEstadoPago(nuevoEstado);

        Pedido actualizado =
                pedidoRepository.saveAndFlush(pedido);

        return convertirPedidoCompleto(actualizado);
    }

    // Obtener un pedido con todos sus detalles
    private PedidoDTO convertirPedidoCompleto(Pedido pedido) {

        List<PedidoDetalle> detalles = detalleRepository
                .findByPedido_IdPedidoOrderByIdDetalleAsc(
                        pedido.getIdPedido()
                );

        return convertirDTO(pedido, detalles);
    }

    // Convertir la entidad Pedido a DTO
    private PedidoDTO convertirDTO(
            Pedido pedido,
            List<PedidoDetalle> detalles) {

        List<PedidoDetalleDTO> detallesDTO = detalles
                .stream()
                .map(this::convertirDetalleDTO)
                .toList();

        return new PedidoDTO(
                pedido.getIdPedido(),
                pedido.getUsuario().getIdUsuario(),
                pedido.getFechaPedido(),
                pedido.getTotal(),
                pedido.getEstadoPago(),
                pedido.getDireccionEnvio(),
                pedido.getCiudad(),
                detallesDTO
        );
    }

    // Convertir cada detalle a DTO
    private PedidoDetalleDTO convertirDetalleDTO(
            PedidoDetalle detalle) {

        BigDecimal subtotal = detalle.getPrecioUnitario()
                .multiply(
                        BigDecimal.valueOf(
                                detalle.getCantidad()
                        )
                );

        return new PedidoDetalleDTO(
                detalle.getIdDetalle(),
                detalle.getProducto().getIdProducto(),
                detalle.getProducto().getNombreProducto(),
                detalle.getCantidad(),
                detalle.getPrecioUnitario(),
                subtotal
        );
    }
}