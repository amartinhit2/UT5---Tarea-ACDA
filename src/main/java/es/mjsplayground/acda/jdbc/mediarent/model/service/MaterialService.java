package es.mjsplayground.acda.jdbc.mediarent.model.service;

import es.mjsplayground.acda.jdbc.mediarent.config.Database;
import es.mjsplayground.acda.jdbc.mediarent.model.dto.AlquilerResumen;
import es.mjsplayground.acda.jdbc.mediarent.model.entity.Material;
import es.mjsplayground.acda.jdbc.mediarent.model.repository.JdbcMaterialRepository;
import es.mjsplayground.acda.jdbc.mediarent.model.repository.PersistenciaException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Aplica las reglas del catálogo y coordina los casos de uso de alquiler. */
public final class MaterialService {
    private static final Logger LOG = LoggerFactory.getLogger(MaterialService.class);
    private static final long TARIFA_MAXIMA = 100_000_000L;

    private final Database database;
    private final JdbcMaterialRepository materiales = new JdbcMaterialRepository();

    public MaterialService(Database database) {
        this.database = database;
    }

    public List<Material> listarMateriales() {
        try (Connection conexion = database.abrir()) {
            return materiales.listar(conexion);
        } catch (SQLException fallo) {
            throw falloDePersistencia("consultar materiales", fallo);
        }
    }

    /** Consulta por un fragmento del nombre. Un filtro en blanco muestra todo el catálogo. */
    public List<Material> buscarMateriales(String texto) {
        if (texto == null) {
            throw new ReglaNegocioException("Introduce un texto para buscar.");
        }
        if (texto.isBlank()) {
            return listarMateriales();
        }

        try (Connection conexion = database.abrir()) {
            return materiales.buscarPorNombre(conexion, texto);
        } catch (SQLException fallo) {
            throw falloDePersistencia("buscar materiales", fallo);
        }
    }

    /** Valida los datos de alta y devuelve el identificador asignado al material. */
    public long crearMaterial(String nombre, long tarifaCentimos, int unidadesDisponibles) {
        if (nombre == null) {
            throw new ReglaNegocioException("El nombre del material es obligatorio.");
        }

        String nombreLimpio = nombre.strip();
        int longitud = nombreLimpio.codePointCount(0, nombreLimpio.length());
        if (longitud < 1 || longitud > 80 || nombreLimpio.indexOf(0) >= 0) {
            throw new ReglaNegocioException("El nombre debe tener entre 1 y 80 caracteres válidos.");
        }
        validarTarifa(tarifaCentimos);

        if (unidadesDisponibles < 0) {
            throw new ReglaNegocioException("Las unidades disponibles no pueden ser negativas.");
        }

        try (Connection conexion = database.abrir()) {
            long id = materiales.insertar(
                    conexion, nombreLimpio, tarifaCentimos, unidadesDisponibles);
            LOG.info("Material registrado con ID {}", id);
            return id;
        } catch (SQLException fallo) {
            throw falloDePersistencia("crear el material", fallo);
        }
    }

    public void cambiarTarifa(long materialId, long tarifaCentimos) {
        validarId(materialId, "material");
        validarTarifa(tarifaCentimos);

        try (Connection conexion = database.abrir()) {
            exigirMaterial(conexion, materialId);
            int filasActualizadas = materiales.actualizarTarifa(conexion, materialId, tarifaCentimos);
            if (filasActualizadas != 1) {
                throw new ReglaNegocioException("No se pudo actualizar la tarifa del material.");
            }

            LOG.info("Tarifa actualizada para el material {}", materialId);
        } catch (SQLException fallo) {
            throw falloDePersistencia("cambiar la tarifa", fallo);
        }
    }

    public void eliminarMaterial(long materialId) {
        validarId(materialId, "material");

        try (Connection conexion = database.abrir()) {
            if (materiales.eliminar(conexion, materialId) != 1) {
                throw new ReglaNegocioException("El material seleccionado no existe.");
            }
            LOG.info("Material eliminado con ID {}", materialId);
        } catch (SQLException fallo) {
            throw falloDePersistencia("eliminar el material", fallo);
        }
    }

    public long registrarAlquiler(long materialId, int cantidad, int dias) {
        throw new FuncionalidadPendienteException("El registro de alquileres está pendiente de desarrollo.");
    }

    public List<AlquilerResumen> listarAlquileres() {
        throw new FuncionalidadPendienteException("La consulta del historial está pendiente de desarrollo.");
    }

    public void devolverAlquiler(long alquilerId) {
        throw new FuncionalidadPendienteException("La devolución de alquileres está pendiente de desarrollo.");
    }

    private void validarId(long id, String elemento) {
        if (id <= 0) {
            throw new ReglaNegocioException("Selecciona un " + elemento + " válido.");
        }
    }

    private void validarTarifa(long tarifaCentimos) {
        if (tarifaCentimos < 1 || tarifaCentimos > TARIFA_MAXIMA) {
            throw new ReglaNegocioException("La tarifa debe estar entre 1 y 100.000.000 céntimos.");
        }
    }

    private Material exigirMaterial(Connection conexion, long materialId) throws SQLException {
        Material material = materiales.buscarPorId(conexion, materialId);
        if (material == null) {
            throw new ReglaNegocioException("El material seleccionado no existe.");
        }
        return material;
    }

    private PersistenciaException falloDePersistencia(String operacion, SQLException fallo) {
        LOG.error("No se pudo {}", operacion, fallo);
        return new PersistenciaException(
                "No se pudo " + operacion + ". Consulta el registro técnico.", fallo);
    }
}
