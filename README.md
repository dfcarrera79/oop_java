# Sistema de Gestion de Proformas

Aplicacion de escritorio de la clase 6 para administrar clientes, productos y
proformas con Java 17, JavaFX 21, SQLite y PDFBox 3.

## Requisitos

- JDK 17 o superior.
- Un entorno grafico compatible con JavaFX.
- Conexion a Internet durante la primera ejecucion para descargar las
  dependencias de Maven.

El proyecto incluye Maven Wrapper, por lo que no es necesario instalar Maven.

## Ejecutar la aplicacion

Desde la raiz del repositorio:

```bash
./mvnw javafx:run
```

En Windows:

```bat
mvnw.cmd javafx:run
```

## Ejecutar desde Antigravity IDE

Abra en Antigravity IDE la carpeta completa del proyecto:

```text
/home/dfcarrera/Documents/Lectures/oop_java
```

Si la opcion **Run Java** de `Main.java` no reconoce JavaFX, vuelva a importar
el proyecto Maven:

1. Presione `Ctrl+Shift+P` para abrir la paleta de comandos de Antigravity.
2. Escriba y seleccione `Java: Clean Java Language Server Workspace`.
3. Confirme la opcion **Restart and delete**.
4. Espere a que Maven termine de importar las dependencias.
5. Haga clic derecho en `Main.java` y seleccione **Run Java**.

`Java: Clean Java Language Server Workspace` es una accion de la paleta de
Antigravity, no un comando para ejecutar en Bash.

La base de datos se crea automaticamente en `data/proformas.db`. Para usar otra
ruta:

```bash
./mvnw javafx:run -Dproformas.db=/ruta/escribible/proformas.db
```

## Ejecutar las pruebas

```bash
./mvnw test
```

## Verificar formato y estilo

```bash
./mvnw spotless:check
./mvnw checkstyle:check
```

Si Spotless informa errores de formato, se pueden corregir con:

```bash
./mvnw spotless:apply
```
