package es.mjsplayground.acda.jdbc.mediarent.model.dto;

import java.time.LocalDateTime;

/** Fila del historial preparada para su presentación. */
public record AlquilerResumen(long id, long materialId, String nombreMaterial, int cantidad,
                              int dias, long tarifaCentimos, long totalCentimos,
                              LocalDateTime fechaRegistro, EstadoAlquiler estado) {
}
