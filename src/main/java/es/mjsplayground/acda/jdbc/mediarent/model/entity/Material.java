package es.mjsplayground.acda.jdbc.mediarent.model.entity;

public record Material(long id, String nombre, long tarifaCentimos, int unidadesDisponibles) {
}
