package cl.duoc.motostore.repository;

import cl.duoc.motostore.model.MensajeContacto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MensajeContactoRepository
        extends JpaRepository<MensajeContacto, Integer> {

    List<MensajeContacto> findAllByOrderByFechaEnvioDesc();
}