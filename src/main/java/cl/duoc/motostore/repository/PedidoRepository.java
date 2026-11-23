package cl.duoc.motostore.repository;

import cl.duoc.motostore.model.Pedido;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PedidoRepository
        extends JpaRepository<Pedido, Integer> {

    List<Pedido> findAllByOrderByFechaPedidoDesc();

    List<Pedido> findByUsuario_IdUsuarioOrderByFechaPedidoDesc(
            Integer idUsuario
    );

    Optional<Pedido> findByIdPedidoAndUsuario_IdUsuario(
            Integer idPedido,
            Integer idUsuario
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Pedido p WHERE p.idPedido = :id")
    Optional<Pedido> buscarPorIdConBloqueo(
            @Param("id") Integer id
    );
}