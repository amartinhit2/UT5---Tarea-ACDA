package es.mjsplayground.acda.jdbc.mediarent.controller;

import es.mjsplayground.acda.jdbc.mediarent.model.dto.AlquilerResumen;
import es.mjsplayground.acda.jdbc.mediarent.model.dto.EstadoAlquiler;
import es.mjsplayground.acda.jdbc.mediarent.model.entity.Material;
import es.mjsplayground.acda.jdbc.mediarent.model.repository.PersistenciaException;
import es.mjsplayground.acda.jdbc.mediarent.model.service.FuncionalidadPendienteException;
import es.mjsplayground.acda.jdbc.mediarent.model.service.MaterialService;
import es.mjsplayground.acda.jdbc.mediarent.model.service.ReglaNegocioException;
import es.mjsplayground.acda.jdbc.mediarent.view.Dialogos;
import es.mjsplayground.acda.jdbc.mediarent.view.MediaRentView;

import java.util.List;

/** Conecta los eventos de los controles con el servicio y actualiza la presentación. */
public final class MediaRentController {
    private final MaterialService servicio;
    private final MediaRentView vista;
    private final Dialogos dialogos;

    public MediaRentController(MaterialService servicio, MediaRentView vista) {
        this.servicio = servicio;
        this.vista = vista;
        this.dialogos = new Dialogos(vista);
    }

    public void iniciar() {
        vista.cambiarTarifa.setDisable(true);
        vista.eliminar.setDisable(true);
        vista.devolver.setDisable(true);
        vista.registrar.disableProperty().bind(vista.selectorMaterial.valueProperty().isNull());

        vista.tablaMateriales.getSelectionModel().selectedItemProperty().addListener(
                (observable, anterior, seleccionado) -> {
                    vista.cambiarTarifa.setDisable(seleccionado == null);
                    vista.eliminar.setDisable(seleccionado == null);
                });
        vista.tablaAlquileres.getSelectionModel().selectedItemProperty().addListener(
                (observable, anterior, seleccionado) -> vista.devolver.setDisable(
                        seleccionado == null || seleccionado.estado() != EstadoAlquiler.ACTIVO));

        vista.buscar.setOnAction(evento -> buscar());
        vista.textoBusqueda.setOnAction(evento -> buscar());
        vista.actualizarMaterial.setOnAction(evento -> buscar());
        vista.nuevo.setOnAction(evento -> nuevoMaterial());
        vista.cambiarTarifa.setOnAction(evento -> cambiarTarifa());
        vista.eliminar.setOnAction(evento -> eliminarMaterial());
        vista.registrar.setOnAction(evento -> ejecutar(this::registrarAlquiler));
        vista.devolver.setOnAction(evento -> ejecutar(this::devolverAlquiler));
        vista.actualizarAlquileres.setOnAction(evento -> actualizarAlquileres());
        vista.pestanas.getSelectionModel().selectedItemProperty().addListener(
                (observable, anterior, seleccionada) -> {
                    if (seleccionada == vista.alquileres) {
                        actualizarAlquileres();
                    } else {
                        buscar();
                    }
                });
        buscar();
    }

    private void buscar() {
        ejecutar(() -> {
            int filas = recargarCatalogo();
            String texto = filas == 1 ? "1 material en el listado." : filas + " materiales en el listado.";
            vista.mostrarMensaje(texto, false);
        });
    }

    private int recargarCatalogo() {
        List<Material> materiales = servicio.buscarMateriales(vista.textoBusqueda.getText());
        vista.tablaMateriales.getSelectionModel().clearSelection();
        vista.tablaMateriales.getItems().setAll(materiales);
        vista.tablaMateriales.sort();
        return materiales.size();
    }

    private void nuevoMaterial() {
        dialogos.nuevoMaterial(datos -> {
            long id = servicio.crearMaterial(datos.nombre(), leerTarifa(datos.tarifa()),
                    leerEntero(datos.stock(), "las unidades disponibles"));
            vista.textoBusqueda.clear();
            trasGuardar("Material registrado · ID " + id, this::recargarCatalogo);
        });
    }

    private void cambiarTarifa() {
        Material material = vista.tablaMateriales.getSelectionModel().getSelectedItem();
        dialogos.cambiarTarifa(material, texto -> {
            servicio.cambiarTarifa(material.id(), leerTarifa(texto));
            trasGuardar("Tarifa actualizada.", this::recargarCatalogo);
        });
    }

    private void eliminarMaterial() {
        Material material = vista.tablaMateriales.getSelectionModel().getSelectedItem();
        boolean confirmado = dialogos.confirmar("Eliminar material",
                "¿Eliminar «" + material.nombre() + "» (ID " + material.id() + ")?", "Eliminar");
        if (confirmado) {
            ejecutar(() -> {
                servicio.eliminarMaterial(material.id());
                trasGuardar("Material eliminado.", this::recargarCatalogo);
            });
        }
    }

    private void registrarAlquiler() {
        Material material = vista.selectorMaterial.getValue();
        long id = servicio.registrarAlquiler(material.id(),
                leerEntero(vista.cantidad.getText(), "las unidades"),
                leerEntero(vista.dias.getText(), "los días"));
        vista.cantidad.setText("1");
        vista.dias.setText("1");
        trasGuardar("Alquiler registrado · ID " + id, this::recargarAlquileres);
    }

    private void devolverAlquiler() {
        AlquilerResumen alquiler = vista.tablaAlquileres.getSelectionModel().getSelectedItem();
        boolean confirmado = dialogos.confirmar("Devolver alquiler",
                "¿Confirmar la devolución completa del alquiler " + alquiler.id() + " de «"
                        + alquiler.nombreMaterial() + "»?", "Devolver");
        if (confirmado) {
            servicio.devolverAlquiler(alquiler.id());
            trasGuardar("Devolución registrada.", this::recargarAlquileres);
        }
    }

    private void actualizarAlquileres() {
        ejecutar(() -> {
            recargarAlquileres();
            int filas = vista.tablaAlquileres.getItems().size();
            String texto = filas == 1 ? "1 alquiler en el historial." : filas + " alquileres en el historial.";
            vista.mostrarMensaje(texto, false);
        });
    }

    private void recargarAlquileres() {
        Material seleccionado = vista.selectorMaterial.getValue();
        List<Material> catalogo = servicio.listarMateriales();
        vista.selectorMaterial.getSelectionModel().clearSelection();
        vista.selectorMaterial.setValue(null);
        vista.selectorMaterial.getItems().setAll(catalogo);
        if (seleccionado != null) {
            for (Material material : vista.selectorMaterial.getItems()) {
                if (material.id() == seleccionado.id()) {
                    vista.selectorMaterial.getSelectionModel().select(material);
                    break;
                }
            }
        }

        try {
            List<AlquilerResumen> historial = servicio.listarAlquileres();
            vista.historialVacio.setText("Todavía no hay alquileres.");
            vista.tablaAlquileres.getSelectionModel().clearSelection();
            vista.tablaAlquileres.getItems().setAll(historial);
            vista.tablaAlquileres.sort();
        } catch (PersistenciaException | FuncionalidadPendienteException fallo) {
            vista.historialVacio.setText(fallo.getMessage());
            throw fallo;
        }
    }

    private long leerTarifa(String texto) {
        try {
            return Long.parseLong(texto.strip());
        } catch (NumberFormatException fallo) {
            throw new ReglaNegocioException("Introduce la tarifa como un entero en céntimos.");
        }
    }

    private int leerEntero(String texto, String campo) {
        try {
            return Integer.parseInt(texto.strip());
        } catch (NumberFormatException fallo) {
            throw new ReglaNegocioException("Introduce un número entero para " + campo + ".");
        }
    }

    private void trasGuardar(String mensaje, Runnable recargar) {
        try {
            recargar.run();
            vista.mostrarMensaje(mensaje, false);
        } catch (ReglaNegocioException | PersistenciaException | FuncionalidadPendienteException fallo) {
            vista.mostrarMensaje(mensaje + " La actualización del listado falló. " + fallo.getMessage(), true);
        }
    }

    private void ejecutar(Runnable accion) {
        try {
            accion.run();
        } catch (ReglaNegocioException | PersistenciaException | FuncionalidadPendienteException fallo) {
            vista.mostrarMensaje(fallo.getMessage(), true);
        }
    }
}
