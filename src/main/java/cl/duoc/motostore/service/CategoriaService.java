
package cl.duoc.motostore.service;

import cl.duoc.motostore.dto.CategoriaDTO;
import cl.duoc.motostore.dto.CategoriaRequest;
import cl.duoc.motostore.model.Categoria;
import cl.duoc.motostore.repository.CategoriaRepository;
import cl.duoc.motostore.repository.ProductoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    public CategoriaService(
            CategoriaRepository categoriaRepository,
            ProductoRepository productoRepository) {

        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
    }

    public List<CategoriaDTO> listarTodas() {

        return categoriaRepository.findAll()
                .stream()
                .map(this::convertirDTO)
                .toList();
    }

    public CategoriaDTO buscarPorId(Integer id) {

        return convertirDTO(obtenerCategoria(id));
    }

    @Transactional
    public CategoriaDTO crear(CategoriaRequest request) {

        String nombre = request.nombreCategoria().trim();

        if (categoriaRepository
                .existsByNombreCategoriaIgnoreCase(nombre)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La categoria ya existe"
            );
        }

        Categoria categoria = new Categoria();

        categoria.setNombreCategoria(nombre);
        categoria.setDescripcion(request.descripcion());

        return guardar(categoria);
    }

    @Transactional
    public CategoriaDTO actualizar(
            Integer id,
            CategoriaRequest request) {

        Categoria categoria = obtenerCategoria(id);

        String nombre = request.nombreCategoria().trim();

        if (categoriaRepository
                .existsByNombreCategoriaIgnoreCaseAndIdCategoriaNot(
                        nombre, id)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El nombre de categoria ya existe"
            );
        }

        categoria.setNombreCategoria(nombre);
        categoria.setDescripcion(request.descripcion());

        return guardar(categoria);
    }

    @Transactional
    public void eliminar(Integer id) {

        Categoria categoria = obtenerCategoria(id);

        if (productoRepository.existsByCategoria_IdCategoria(id)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se puede eliminar una categoria con productos asociados"
            );
        }

        try {
            categoriaRepository.delete(categoria);
            categoriaRepository.flush();

        } catch (DataIntegrityViolationException e) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La categoria tiene registros asociados"
            );
        }
    }

    private CategoriaDTO guardar(Categoria categoria) {

        try {
            return convertirDTO(
                    categoriaRepository.saveAndFlush(categoria)
            );

        } catch (DataIntegrityViolationException e) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se pudo guardar la categoria"
            );
        }
    }

    private Categoria obtenerCategoria(Integer id) {

        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Categoria no encontrada"
                ));
    }

    private CategoriaDTO convertirDTO(Categoria categoria) {

        return new CategoriaDTO(
                categoria.getIdCategoria(),
                categoria.getNombreCategoria(),
                categoria.getDescripcion()
        );
    }
}
