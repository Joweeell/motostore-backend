package cl.duoc.motostore.service;

import cl.duoc.motostore.dto.MensajeContactoDTO;
import cl.duoc.motostore.dto.MensajeContactoRequest;
import cl.duoc.motostore.model.MensajeContacto;
import cl.duoc.motostore.repository.MensajeContactoRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class MensajeContactoService {

    private final MensajeContactoRepository contactoRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public MensajeContactoService(
            MensajeContactoRepository contactoRepository) {
        this.contactoRepository = contactoRepository;
    }

    @Transactional
    public MensajeContactoDTO crear(MensajeContactoRequest request) {
        MensajeContacto entity = new MensajeContacto();
        entity.setNombre(request.nombre().trim());
        entity.setEmail(request.email().trim().toLowerCase(Locale.ROOT));
        entity.setAsunto(
                request.asunto() == null || request.asunto().isBlank()
                        ? null
                        : request.asunto().trim()
        );
        entity.setMensaje(request.mensaje().trim());

        MensajeContacto guardado = contactoRepository.saveAndFlush(entity);
        entityManager.refresh(guardado);

        return convertirDTO(guardado);
    }

    public List<MensajeContactoDTO> listarTodos() {
        return contactoRepository
                .findAllByOrderByFechaEnvioDesc()
                .stream()
                .map(this::convertirDTO)
                .toList();
    }

    public MensajeContactoDTO buscarPorId(Integer idMensaje) {
        MensajeContacto entity = contactoRepository
                .findById(idMensaje)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Mensaje de contacto no encontrado"
                ));

        return convertirDTO(entity);
    }

    @Transactional
    public void eliminar(Integer idMensaje) {
        if (!contactoRepository.existsById(idMensaje)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Mensaje de contacto no encontrado"
            );
        }

        contactoRepository.deleteById(idMensaje);
    }

    private MensajeContactoDTO convertirDTO(MensajeContacto entity) {
        return new MensajeContactoDTO(
                entity.getIdMensaje(),
                entity.getNombre(),
                entity.getEmail(),
                entity.getAsunto(),
                entity.getMensaje(),
                entity.getFechaEnvio()
        );
    }
}