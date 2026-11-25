package es.mjsplayground.acda.jdbc.mediarent.view;

import es.mjsplayground.acda.jdbc.mediarent.model.dto.AlquilerResumen;
import es.mjsplayground.acda.jdbc.mediarent.model.entity.Material;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.function.Function;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.util.StringConverter;

/** Construye los controles y da formato a los valores que muestra cada tabla. */
public final class MediaRentView {
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");

    public final BorderPane raiz = new BorderPane();
    public final TabPane pestanas = new TabPane();
    public final Tab material = new Tab("Material");
    public final Tab alquileres = new Tab("Alquileres");
    public final TableView<Material> tablaMateriales = new TableView<>();
    public final TableView<AlquilerResumen> tablaAlquileres = new TableView<>();
    public final TextField textoBusqueda = new TextField();
    public final Button buscar = new Button("Buscar");
    public final Button actualizarMaterial = new Button("Actualizar");
    public final Button nuevo = new Button("Nuevo material");
    public final Button cambiarTarifa = new Button("Cambiar tarifa");
    public final Button eliminar = new Button("Eliminar");
    public final ComboBox<Material> selectorMaterial = new ComboBox<>();
    public final TextField cantidad = new TextField("1");
    public final TextField dias = new TextField("1");
    public final Button registrar = new Button("Registrar alquiler");
    public final Button devolver = new Button("Devolver alquiler");
    public final Button actualizarAlquileres = new Button("Actualizar");
    public final Label mensaje = new Label();
    public final Label historialVacio = new Label("Todavía no hay alquileres.");

    public MediaRentView() {
        raiz.getStyleClass().add("aplicacion");
        raiz.setTop(cabecera());
        raiz.setCenter(pestanas);
        mensaje.setId("mensaje");
        mensaje.setWrapText(true);
        mensaje.setMaxWidth(Double.MAX_VALUE);
        Region separador = new Region();
        separador.getStyleClass().add("indicador");
        HBox pie = new HBox(9, separador, mensaje);
        pie.setAlignment(Pos.CENTER_LEFT);
        pie.getStyleClass().add("estado");
        HBox.setHgrow(mensaje, Priority.ALWAYS);
        raiz.setBottom(pie);

        material.setContent(panelMaterial());
        alquileres.setContent(panelAlquileres());
        pestanas.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        pestanas.getTabs().addAll(material, alquileres);
    }

    public void mostrarMensaje(String texto, boolean aviso) {
        mensaje.setText(texto);
        mensaje.setAccessibleText(texto);
        mensaje.getStyleClass().remove("aviso");
        if (aviso) {
            mensaje.getStyleClass().add("aviso");
        }
    }

    private HBox cabecera() {
        ImageView marca = new ImageView(new Image(getClass()
                .getResourceAsStream("/branding/mjs-playground.png")));
        marca.setFitWidth(36);
        marca.setFitHeight(36);
        marca.setPreserveRatio(true);

        Label nombre = new Label("MediaRent");
        nombre.getStyleClass().add("marca");
        Label descripcion = new Label("Alquiler de material audiovisual");
        descripcion.getStyleClass().add("subtitulo");
        HBox cabecera = new HBox(13, marca, new VBox(1, nombre, descripcion));
        cabecera.getStyleClass().add("cabecera");
        cabecera.setAlignment(Pos.CENTER_LEFT);
        return cabecera;
    }

    private VBox panelMaterial() {
        tablaMateriales.setId("tabla-materiales");
        tablaMateriales.setAccessibleText("Catálogo de material");
        configurarTabla(tablaMateriales, "No hay materiales para mostrar.");
        tablaMateriales.getColumns().add(columnaNumerica("ID", 65, 54, Material::id));
        tablaMateriales.getColumns().add(columnaTexto("Nombre del material", 455, 220, Material::nombre));
        tablaMateriales.getColumns().add(columnaMoneda("Tarifa diaria", 160, 120, Material::tarifaCentimos));
        tablaMateriales.getColumns().add(columnaNumerica("Disponibles", 150, 112, Material::unidadesDisponibles));

        textoBusqueda.setId("busqueda");
        textoBusqueda.setPromptText("Parte del nombre");
        HBox.setHgrow(textoBusqueda, Priority.ALWAYS);
        Label etiquetaBuscar = etiqueta("Buscar por nombre", textoBusqueda);
        HBox filtro = new HBox(8, etiquetaBuscar, textoBusqueda, buscar, actualizarMaterial);
        filtro.setAlignment(Pos.CENTER_LEFT);

        nuevo.getStyleClass().add("primario");
        eliminar.getStyleClass().add("peligro");
        FlowPane acciones = new FlowPane(8, 8, nuevo, cambiarTarifa, eliminar);
        VBox contenido = new VBox(13, titulo("Catálogo"), filtro, acciones, tablaMateriales);
        VBox.setVgrow(tablaMateriales, Priority.ALWAYS);
        contenido.getStyleClass().add("panel");
        return contenido;
    }

    private VBox panelAlquileres() {
        tablaAlquileres.setId("tabla-alquileres");
        tablaAlquileres.setAccessibleText("Historial de alquileres");
        configurarTabla(tablaAlquileres, "Todavía no hay alquileres.");
        tablaAlquileres.setPlaceholder(historialVacio);
        tablaAlquileres.getColumns().add(columnaNumerica("ID", 57, 47, AlquilerResumen::id));
        tablaAlquileres.getColumns().add(columnaTexto("Material", 200, 134, AlquilerResumen::nombreMaterial));
        tablaAlquileres.getColumns().add(columnaNumerica("Unid.", 64, 54, AlquilerResumen::cantidad));
        tablaAlquileres.getColumns().add(columnaNumerica("Días", 56, 49, AlquilerResumen::dias));
        tablaAlquileres.getColumns().add(columnaMoneda("Tarifa / día", 112, 98, AlquilerResumen::tarifaCentimos));
        tablaAlquileres.getColumns().add(columnaMoneda("Importe", 120, 103, AlquilerResumen::totalCentimos));
        TableColumn<AlquilerResumen, LocalDateTime> fecha = columnaFormateada(
                "Fecha de registro", 143, 124, AlquilerResumen::fechaRegistro, FECHA::format);
        tablaAlquileres.getColumns().add(fecha);
        TableColumn<AlquilerResumen, String> estado =
                columnaTexto("Estado", 99, 88, fila -> fila.estado().name());
        estado.setCellFactory(columna -> new TableCell<>() {
            private final Label etiqueta = new Label();

            @Override
            protected void updateItem(String valor, boolean vacia) {
                super.updateItem(valor, vacia);
                if (vacia || valor == null) {
                    setGraphic(null);
                } else {
                    etiqueta.setText(valor);
                    etiqueta.getStyleClass().setAll("etiqueta-estado",
                            valor.equals("ACTIVO") ? "activo" : "devuelto");
                    setGraphic(etiqueta);
                }
            }
        });
        tablaAlquileres.getColumns().add(estado);

        selectorMaterial.setId("selector-material");
        selectorMaterial.setMaxWidth(Double.MAX_VALUE);
        selectorMaterial.setPromptText("Selecciona un material");
        selectorMaterial.setConverter(new StringConverter<>() {
            @Override
            public String toString(Material dato) {
                if (dato == null) {
                    return "";
                }
                return dato.nombre() + " · ID " + dato.id() + " · " + dato.unidadesDisponibles() + " disp.";
            }

            @Override
            public Material fromString(String valor) {
                return null;
            }
        });
        cantidad.setId("cantidad");
        cantidad.setPrefColumnCount(5);
        dias.setId("dias");
        dias.setPrefColumnCount(5);
        registrar.getStyleClass().add("primario");
        registrar.setMinWidth(Region.USE_PREF_SIZE);

        VBox campoMaterial = campo("Material", selectorMaterial);
        campoMaterial.setMinWidth(220);
        HBox.setHgrow(campoMaterial, Priority.ALWAYS);
        VBox campoCantidad = campo("Unidades", cantidad);
        campoCantidad.setMinWidth(Region.USE_PREF_SIZE);
        VBox campoDias = campo("Días (1–30)", dias);
        campoDias.setMinWidth(Region.USE_PREF_SIZE);
        HBox controles = new HBox(12, campoMaterial, campoCantidad, campoDias, registrar);
        controles.setAlignment(Pos.BOTTOM_LEFT);
        VBox formulario = new VBox(10, titulo("Nuevo alquiler"), controles);
        formulario.getStyleClass().add("formulario");

        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);
        HBox accionesHistorial = new HBox(8, titulo("Historial"), espacio, devolver, actualizarAlquileres);
        accionesHistorial.setAlignment(Pos.CENTER_LEFT);

        VBox contenido = new VBox(13, formulario, accionesHistorial, tablaAlquileres);
        VBox.setVgrow(tablaAlquileres, Priority.ALWAYS);
        contenido.getStyleClass().add("panel");
        return contenido;
    }

    static Label etiqueta(String texto, javafx.scene.Node control) {
        Label etiqueta = new Label(texto);
        etiqueta.setLabelFor(control);
        etiqueta.getStyleClass().add("etiqueta");
        return etiqueta;
    }

    private VBox campo(String texto, javafx.scene.Node control) {
        return new VBox(4, etiqueta(texto, control), control);
    }

    private Label titulo(String texto) {
        Label titulo = new Label(texto);
        titulo.getStyleClass().add("titulo");
        return titulo;
    }

    private <T> void configurarTabla(TableView<T> tabla, String textoVacio) {
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tabla.setPlaceholder(new Label(textoVacio));
    }

    private <T> TableColumn<T, Number> columnaNumerica(String titulo, double anchura, double minima,
                                                       Function<T, Number> leer) {
        TableColumn<T, Number> columna = new TableColumn<>(titulo);
        columna.setPrefWidth(anchura);
        columna.setMinWidth(minima);
        columna.getStyleClass().add("numero");
        columna.setCellValueFactory(fila -> new ReadOnlyObjectWrapper<>(leer.apply(fila.getValue())));
        return columna;
    }

    private <T> TableColumn<T, String> columnaTexto(String titulo, double anchura, double minima,
                                                     Function<T, String> leer) {
        return columnaFormateada(titulo, anchura, minima, leer, Function.identity());
    }

    private <T> TableColumn<T, Long> columnaMoneda(String titulo, double anchura, double minima,
                                                   Function<T, Long> leer) {
        TableColumn<T, Long> columna = columnaFormateada(titulo, anchura, minima, leer, this::euros);
        columna.getStyleClass().add("numero");
        return columna;
    }

    private <T, V> TableColumn<T, V> columnaFormateada(String titulo, double anchura, double minima,
                                                        Function<T, V> leer, Function<V, String> formatear) {
        TableColumn<T, V> columna = new TableColumn<>(titulo);
        columna.setPrefWidth(anchura);
        columna.setMinWidth(minima);
        columna.setCellValueFactory(fila -> new ReadOnlyObjectWrapper<>(leer.apply(fila.getValue())));
        columna.setCellFactory(definicion -> new TableCell<>() {
            private final Tooltip tooltip = new Tooltip();

            @Override
            protected void updateItem(V valor, boolean vacia) {
                super.updateItem(valor, vacia);
                if (vacia || valor == null) {
                    setText(null);
                    setTooltip(null);
                } else {
                    String texto = formatear.apply(valor);
                    setText(texto);
                    tooltip.setText(texto);
                    setTooltip(tooltip);
                }
            }
        });
        return columna;
    }

    private String euros(long centimos) {
        return String.format(Locale.forLanguageTag("es-ES"), "%,d,%02d €", centimos / 100, centimos % 100);
    }
}
