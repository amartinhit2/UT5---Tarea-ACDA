package es.mjsplayground.acda.jdbc.mediarent.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** Prepara el archivo SQLite y configura las conexiones que usarán los servicios. */
public final class Database {
    private final Path ruta;

    public Database(Path ruta) {
        this.ruta = ruta.toAbsolutePath().normalize();
    }

    /**
     * Quien pide esta conexión es responsable de cerrarla. Las claves foráneas se activan
     * antes de que el llamador pueda iniciar una transacción.
     */
    public Connection abrir() throws SQLException {
        Connection conexion = DriverManager.getConnection("jdbc:sqlite:" + ruta);
        try (Statement sentencia = conexion.createStatement()) {
            sentencia.execute("PRAGMA foreign_keys = ON");
            sentencia.execute("PRAGMA busy_timeout = 3000");
        } catch (SQLException fallo) {
            try {
                conexion.close();
            } catch (SQLException falloCierre) {
                fallo.addSuppressed(falloCierre);
            }
            throw fallo;
        }
        return conexion;
    }

    /** Prepara el esquema y carga el catálogo de partida en la primera inicialización. */
    public void preparar() throws SQLException {
        try {
            Files.createDirectories(ruta.getParent());
        } catch (IOException fallo) {
            throw new SQLException("No se pudo preparar la carpeta de datos.", fallo);
        }

        try (Connection conexion = abrir();
             Statement sentencia = conexion.createStatement()) {
            conexion.setAutoCommit(false);
            try {
                boolean catalogoNuevo = !existeCatalogo(sentencia);
                crearEsquema(sentencia);
                if (catalogoNuevo) {
                    insertarCatalogoInicial(sentencia);
                }
                conexion.commit();
            } catch (SQLException fallo) {
                try {
                    conexion.rollback();
                } catch (SQLException falloRollback) {
                    fallo.addSuppressed(falloRollback);
                }
                throw fallo;
            }
        }
    }

    private boolean existeCatalogo(Statement sentencia) throws SQLException {
        String sql = """
                SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = 'material'
                """;
        try (ResultSet resultado = sentencia.executeQuery(sql)) {
            return resultado.next();
        }
    }

    private void crearEsquema(Statement sentencia) throws SQLException {
        sentencia.executeUpdate("""
                CREATE TABLE IF NOT EXISTS material (
                    id INTEGER PRIMARY KEY,
                    nombre TEXT NOT NULL CHECK (length(nombre) BETWEEN 1 AND 80),
                    tarifa_centimos INTEGER NOT NULL CHECK (tarifa_centimos BETWEEN 1 AND 100000000),
                    stock INTEGER NOT NULL CHECK (stock BETWEEN 0 AND 2147483647)
                )
                """);

        sentencia.executeUpdate("""
                CREATE TABLE IF NOT EXISTS alquiler (
                id INTEGER PRIMARY KEY,
                material_id INTEGER NOT NULL,
                unidades INTEGER NOT NULL CHECK (unidades > 0),
                dias INTEGER NOT NULL CHECK (dias > 0),
                tarifa_dia INTEGER NOT NULL CHECK (tarifa_dia BETWEEN 1 AND 100000000),
                importe INTEGER NOT NULL CHECK (importe >= 0),
                fecha_registro TEXT NOT NULL,
                estado TEXT NOT NULL CHECK (length(estado) BETWEEN 1 AND 30),
                FOREIGN KEY (material_id) REFERENCES material(id)
                    ON DELETE RESTRICT
                    ON UPDATE CASCADE
                )
                """);
    }

    private void insertarCatalogoInicial(Statement sentencia) throws SQLException {
        sentencia.executeUpdate("""
                INSERT INTO material (id, nombre, tarifa_centimos, stock) VALUES
                    (1, 'Foco LED', 1200, 8),
                    (2, 'Micrófono', 850, 5),
                    (3, 'Trípode', 500, 4),
                    (4, 'Cable d''audio', 250, 12)
                """);
    }
}
