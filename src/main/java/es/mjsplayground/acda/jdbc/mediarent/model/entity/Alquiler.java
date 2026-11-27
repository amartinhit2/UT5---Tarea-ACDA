package es.mjsplayground.acda.jdbc.mediarent.model.entity;

import es.mjsplayground.acda.jdbc.mediarent.model.dto.EstadoAlquiler;

import java.util.Date;

public record Alquiler(long id, long materialId, int unidades, int dias, long tarifaDia, long importe, Date fechaRegistro, EstadoAlquiler estado) {}
