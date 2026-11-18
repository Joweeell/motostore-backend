
package cl.duoc.motostore.service;

import cl.duoc.motostore.dto.CategoriaDTO;
import cl.duoc.motostore.model.Categoria;
import cl.duoc.motostore.repository.CategoriaRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(
            CategoriaRepository categoriaRepository) {

        this.categoriaRepository = categoriaRepository;
    }

    public List<CategoriaDTO> listarTodas() {

        return categoriaRepository.findAll()
                .stream()
                .map(this::convertirDTO)
                .toList();
    }

    public CategoriaDTO buscarPorId(Integer id) {

        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Categoria no encontrada"
                ));

        return convertirDTO(categoria);
    }

    private CategoriaDTO convertirDTO(Categoria categoria) {

        return new CategoriaDTO(
                categoria.getIdCategoria(),
                categoria.getNombreCategoria(),
                categoria.getDescripcion()
        );
    }
}
