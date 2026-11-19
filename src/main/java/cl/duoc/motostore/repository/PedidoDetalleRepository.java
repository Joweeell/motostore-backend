package cl.duoc.motostore.repository;

import cl.duoc.motostore.model.PedidoDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoDetalleRepository
        extends JpaRepository<PedidoDetalle, Integer> {

    List<PedidoDetalle> findByPedido_IdPedidoOrderByIdDetalleAsc(
            Integer idPedido
    );

    boolean existsByProducto_IdProducto(
            Integer idProducto
    );
}