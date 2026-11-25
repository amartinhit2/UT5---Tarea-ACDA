package es.mjsplayground.acda.jdbc.mediarent.model.repository;

import java.sql.SQLException;

public class PersistenciaException extends RuntimeException {
    public PersistenciaException(String mensaje, SQLException causa) {
        super(mensaje, causa);
    }
}
