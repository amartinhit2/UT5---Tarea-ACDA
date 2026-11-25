package es.mjsplayground.acda.jdbc.mediarent.model.repository;

import es.mjsplayground.acda.jdbc.mediarent.model.entity.Material;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Ejecuta las operaciones de catálogo con la conexión prestada por el servicio. */
public final class JdbcMaterialRepository {
    public List<Material> listar(Connection conexion) throws SQLException {
        String sql = "SELECT id, nombre, tarifa_centimos, stock FROM material ORDER BY nombre, id";
        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            return leerLista(consulta);
        }
    }

    /** Recupera las coincidencias utilizando la conexión prestada por el servicio. */
    public List<Material> buscarPorNombre(Connection conexion, String texto) throws SQLException {

        String filtro = texto.trim();
        if (filtro.isEmpty()) {
            filtro = "%";
        } else {
            filtro = filtro.replace("\\","\\\\").replace("%","\\%").replace("_","\\n");
            filtro = "%" + filtro + "%";
        }

        String sql = """
                SELECT id, nombre, tarifa_centimos, stock
                FROM material
                WHERE lower(nombre) LIKE
                    lower(?) ESCAPE "\\"
                ORDER BY nombre, id
                """;
        // Ejecutamos la consulta de búsqueda por nombre.
        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setString(1, filtro);
            return leerLista(consulta);
        }
    }

    /** Devuelve el material solicitado o null si el identificador no existe. */
    public Material buscarPorId(Connection conexion, long id) throws SQLException {
        String sql = "SELECT id, nombre, tarifa_centimos, stock FROM material WHERE id = ?";
        try (PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setLong(1, id);
            try (ResultSet resultado = consulta.executeQuery()) {
                if (resultado.next()) {
                    return leerMaterial(resultado);
                }
                return null;
            }
        }
    }

    /** Inserta un material y devuelve el identificador generado por SQLite. */
    public long insertar(Connection conexion, String nombre, long tarifaCentimos, int stock)
            throws SQLException {
        String sql = "INSERT INTO material (nombre, tarifa_centimos, stock) VALUES (?, ?, ?)";
        try (PreparedStatement sentencia =
                     conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            sentencia.setString(1, nombre);
            sentencia.setLong(2, tarifaCentimos);
            sentencia.setInt(3, stock);
            if (sentencia.executeUpdate() != 1) {
                throw new SQLException("No se insertó el material.");
            }
            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (claves.next()) {
                    return claves.getLong(1);
                }
                throw new SQLException("No se obtuvo el ID del material.");
            }
        }
    }

    /** Devuelve el número de filas afectadas por la sentencia. */
    public int actualizarTarifa(Connection conexion, long id, long tarifaCentimos)
            throws SQLException {
        String sql = "UPDATE material SET tarifa_centimos = ? WHERE id = ?";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, tarifaCentimos);
            sentencia.setLong(2, id);
            return sentencia.executeUpdate();
        }
    }

    public int eliminar(Connection conexion, long id) throws SQLException {
        String sql = "DELETE FROM material WHERE id = ?";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setLong(1, id);
            return sentencia.executeUpdate();
        }
    }

    private List<Material> leerLista(PreparedStatement consulta) throws SQLException {
        List<Material> materiales = new ArrayList<>();
        try (ResultSet resultado = consulta.executeQuery()) {
            while (resultado.next()) {
                materiales.add(leerMaterial(resultado));
            }
        }
        return materiales;
    }

    private Material leerMaterial(ResultSet resultado) throws SQLException {
        long id = resultado.getLong("id");
        String nombre = resultado.getString("nombre");
        long tarifaCentimos = resultado.getLong("tarifa_centimos");
        int unidadesDisponibles = resultado.getInt("stock");

        return new Material(id, nombre, tarifaCentimos, unidadesDisponibles);
    }
}
