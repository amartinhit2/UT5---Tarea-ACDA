package es.mjsplayground.acda.jdbc.mediarent.app;

import es.mjsplayground.acda.jdbc.mediarent.config.Database;
import es.mjsplayground.acda.jdbc.mediarent.controller.MediaRentController;
import es.mjsplayground.acda.jdbc.mediarent.model.service.MaterialService;
import es.mjsplayground.acda.jdbc.mediarent.view.MediaRentView;
import java.nio.file.Path;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MediaRentApp extends Application {
    private static final Logger LOG = LoggerFactory.getLogger(MediaRentApp.class);

    @Override
    public void start(Stage ventana) {
        try {
            Path ruta = Path.of(System.getProperty("mediarent.db", "datos/mediarent.db"));
            Database database = new Database(ruta);
            database.preparar();

            MediaRentView vista = new MediaRentView();
            MediaRentController controlador = new MediaRentController(new MaterialService(database), vista);
            Rectangle2D pantalla = Screen.getPrimary().getVisualBounds();
            Scene escena = new Scene(vista.raiz, Math.min(1080, pantalla.getWidth() - 32),
                    Math.min(690, pantalla.getHeight() - 64));
            escena.getStylesheets().add(getClass().getResource("/mediarent.css").toExternalForm());
            ventana.getIcons().add(new Image(getClass().getResourceAsStream("/branding/mjs-playground.png")));
            ventana.setTitle("MediaRent");
            ventana.setMinWidth(820);
            ventana.setMinHeight(510);
            ventana.setScene(escena);
            controlador.iniciar();
            ventana.show();
        } catch (Exception fallo) {
            LOG.error("Error al iniciar MediaRent", fallo);
            Alert aviso = new Alert(Alert.AlertType.ERROR);
            aviso.setTitle("MediaRent");
            aviso.setHeaderText("MediaRent no ha podido iniciarse");
            aviso.setContentText("Comprueba la ruta de la base de datos y consulta el registro técnico.");
            aviso.showAndWait();
        }
    }
}
