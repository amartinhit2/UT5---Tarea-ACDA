package es.mjsplayground.acda.jdbc.mediarent.view;

import es.mjsplayground.acda.jdbc.mediarent.model.entity.Material;
import es.mjsplayground.acda.jdbc.mediarent.model.repository.PersistenciaException;
import es.mjsplayground.acda.jdbc.mediarent.model.service.ReglaNegocioException;
import java.util.function.Consumer;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;

/** Formularios modales. Si el servicio rechaza una entrada, conserva lo que se escribió. */
public final class Dialogos {
    private final MediaRentView vista;

    public Dialogos(MediaRentView vista) {
        this.vista = vista;
    }

    public void nuevoMaterial(Consumer<DatosMaterial> guardar) {
        TextField nombre = new TextField();
        nombre.setId("nombre-material");
        TextField tarifa = new TextField();
        tarifa.setId("tarifa-material");
        TextField stock = new TextField("0");
        stock.setId("stock-material");
        GridPane campos = campos();
        agregar(campos, 0, "Nombre", nombre);
        agregar(campos, 1, "Tarifa diaria (céntimos)", tarifa);
        agregar(campos, 2, "Unidades disponibles", stock);

        editar("Nuevo material", campos, nombre,
                () -> guardar.accept(new DatosMaterial(nombre.getText(), tarifa.getText(), stock.getText())));
    }

    public void cambiarTarifa(Material material, Consumer<String> guardar) {
        TextField tarifa = new TextField(Long.toString(material.tarifaCentimos()));
        tarifa.setId("tarifa-material");
        GridPane campos = campos();
        Label seleccionado = new Label(material.nombre() + " · ID " + material.id());
        seleccionado.setWrapText(true);
        seleccionado.setMaxWidth(385);
        campos.add(seleccionado, 0, 0, 2, 1);
        agregar(campos, 1, "Tarifa diaria (céntimos)", tarifa);
        editar("Cambiar tarifa", campos, tarifa, () -> guardar.accept(tarifa.getText()));
    }

    public boolean confirmar(String titulo, String detalle, String accion) {
        Dialog<ButtonType> dialogo = dialogo(titulo);
        Label contenido = new Label(detalle);
        contenido.setWrapText(true);
        contenido.setMaxWidth(385);
        dialogo.getDialogPane().setContent(contenido);

        ButtonType confirmar = new ButtonType(accion, ButtonData.OK_DONE);
        ButtonType cancelar = new ButtonType("Cancelar", ButtonData.CANCEL_CLOSE);
        dialogo.getDialogPane().getButtonTypes().addAll(cancelar, confirmar);
        Button botonConfirmar = (Button) dialogo.getDialogPane().lookupButton(confirmar);
        Button botonCancelar = (Button) dialogo.getDialogPane().lookupButton(cancelar);
        botonConfirmar.setDefaultButton(false);
        botonCancelar.setDefaultButton(true);
        dialogo.setOnShown(evento -> botonCancelar.requestFocus());
        return dialogo.showAndWait().orElse(cancelar) == confirmar;
    }

    private void editar(String titulo, GridPane campos, TextField focoInicial, Runnable guardar) {
        Dialog<ButtonType> dialogo = dialogo(titulo);
        Label error = new Label();
        error.setId("error-formulario");
        error.setWrapText(true);
        error.setMaxWidth(390);
        error.getStyleClass().add("aviso");
        error.managedProperty().bind(error.visibleProperty());
        error.setVisible(false);
        campos.add(error, 0, campos.getRowCount(), 2, 1);
        dialogo.getDialogPane().setContent(campos);

        ButtonType aceptar = new ButtonType("Guardar", ButtonData.OK_DONE);
        dialogo.getDialogPane().getButtonTypes().addAll(
                new ButtonType("Cancelar", ButtonData.CANCEL_CLOSE), aceptar);
        Node botonGuardar = dialogo.getDialogPane().lookupButton(aceptar);
        botonGuardar.getStyleClass().add("primario");
        botonGuardar.addEventFilter(ActionEvent.ACTION, evento -> {
            try {
                guardar.run();
            } catch (ReglaNegocioException | PersistenciaException fallo) {
                error.setText(fallo.getMessage());
                error.setVisible(true);
                dialogo.getDialogPane().getScene().getWindow().sizeToScene();
                evento.consume();
            }
        });
        dialogo.setOnShown(evento -> {
            focoInicial.requestFocus();
            focoInicial.selectAll();
        });
        dialogo.showAndWait();
    }

    private Dialog<ButtonType> dialogo(String titulo) {
        Dialog<ButtonType> dialogo = new Dialog<>();
        Window ventana = vista.raiz.getScene().getWindow();
        dialogo.initOwner(ventana);
        dialogo.setTitle("MediaRent");
        dialogo.setHeaderText(titulo);
        dialogo.getDialogPane().setMinWidth(390);
        dialogo.getDialogPane().getStylesheets().addAll(vista.raiz.getScene().getStylesheets());
        return dialogo;
    }

    private GridPane campos() {
        GridPane campos = new GridPane();
        campos.setHgap(12);
        campos.setVgap(10);
        campos.setPadding(new Insets(4, 12, 2, 12));
        return campos;
    }

    private void agregar(GridPane campos, int fila, String texto, TextField entrada) {
        campos.addRow(fila, MediaRentView.etiqueta(texto, entrada), entrada);
    }

    public record DatosMaterial(String nombre, String tarifa, String stock) {
    }
}
