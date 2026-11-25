# MediaRent

Aplicación JavaFX para gestionar material audiovisual y alquileres con SQLite.

## Ejecutar en IntelliJ IDEA

Usa IntelliJ IDEA 2025.2 o posterior con el SDK de Java 25. [Compatibilidad de IntelliJ IDEA 2025.2](https://www.jetbrains.com/idea/whatsnew/2025-2/).

1. Abre esta carpeta como proyecto Maven mediante `pom.xml`.
2. Selecciona un JDK 25 para el proyecto y espera a que Maven importe las dependencias.
3. Abre `es.mjsplayground.acda.jdbc.mediarent.app.Launcher` y ejecuta su método `main` con **Run**. Para depurar, usa **Debug** sobre ese mismo método.

La primera ejecución prepara `datos/mediarent.db`. Al volver a abrir la aplicación se conserva el catálogo y cualquier cambio guardado. Para trabajar sobre un archivo independiente, añade a **VM options** de la configuración de `Launcher` la opción `-Dmediarent.db=datos/prueba.db`. Las rutas relativas parten del directorio de trabajo de la configuración.

Los precios se presentan en euros. El formulario de tarifa y el almacenamiento trabajan en céntimos. Consulta el enunciado para reproducir las incidencias y comprobar el comportamiento esperado.

## Maven Wrapper

Windows

```powershell
.\mvnw.cmd clean package
.\mvnw.cmd "-Dmediarent.db=datos/prueba.db" javafx:run
```

Linux y macOS

```bash
./mvnw clean package
./mvnw -Dmediarent.db=datos/prueba.db javafx:run
```

Requiere JDK 25 y conexión a los repositorios Maven al resolver dependencias por primera vez. SQLite trabaja con un archivo local.
