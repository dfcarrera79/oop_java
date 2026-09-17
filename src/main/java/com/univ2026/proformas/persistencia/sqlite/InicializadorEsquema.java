package com.univ2026.proformas.persistencia.sqlite;

import com.univ2026.proformas.dominio.producto.Producto;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class InicializadorEsquema {
    public static final int VERSION = 6;

    private static final String[] TABLAS = {"""
            CREATE TABLE IF NOT EXISTS productos (
                codigo TEXT PRIMARY KEY NOT NULL, nombre TEXT NOT NULL, descripcion TEXT NOT NULL,
                precio TEXT NOT NULL CHECK (CAST(precio AS REAL) >= 0),
                iva_pct INTEGER NOT NULL CHECK (iva_pct IN (0, 15)),
                estado TEXT NOT NULL CHECK (estado IN ('ACTIVO', 'INACTIVO')),
                tipo_atributos TEXT CHECK (tipo_atributos IN ('FISICO', 'DIGITAL') OR tipo_atributos IS NULL),
                peso_kg REAL CHECK (peso_kg > 0 OR peso_kg IS NULL), talla TEXT,
                tamanio_mb REAL CHECK (tamanio_mb > 0 OR tamanio_mb IS NULL))
            """, """
            CREATE TABLE IF NOT EXISTS clientes (
                identificacion TEXT PRIMARY KEY NOT NULL, nombre TEXT NOT NULL, direccion TEXT NOT NULL,
                telefono TEXT NOT NULL, email TEXT NOT NULL,
                tipo TEXT NOT NULL CHECK (tipo IN ('PUBLICO', 'MAYORISTA', 'MEDICO')),
                estado TEXT NOT NULL CHECK (estado IN ('ACTIVO', 'INACTIVO')))
            """, """
            CREATE TABLE IF NOT EXISTS cuentas_pago (
                id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT NOT NULL UNIQUE, instrucciones TEXT NOT NULL)
            """, """
            CREATE TABLE IF NOT EXISTS secuencias (
                nombre TEXT PRIMARY KEY NOT NULL, valor INTEGER NOT NULL CHECK (valor >= 0))
            """, """
            CREATE TABLE IF NOT EXISTS proformas (
                numero TEXT PRIMARY KEY NOT NULL, fecha TEXT NOT NULL,
                cliente_identificacion TEXT NOT NULL,
                cliente_nombre TEXT NOT NULL, cliente_direccion TEXT NOT NULL,
                cliente_telefono TEXT NOT NULL, cliente_email TEXT NOT NULL,
                cliente_tipo TEXT NOT NULL, cliente_estado TEXT NOT NULL,
                observaciones TEXT NOT NULL, instrucciones_pago TEXT NOT NULL,
                subtotal TEXT NOT NULL, iva TEXT NOT NULL, total TEXT NOT NULL,
                FOREIGN KEY (cliente_identificacion) REFERENCES clientes(identificacion) ON DELETE RESTRICT)
            """, """
            CREATE TABLE IF NOT EXISTS items_proforma (
                id INTEGER PRIMARY KEY AUTOINCREMENT, proforma_numero TEXT NOT NULL, posicion INTEGER NOT NULL,
                producto_codigo TEXT, producto_nombre TEXT NOT NULL, producto_descripcion TEXT NOT NULL,
                tipo_producto TEXT NOT NULL, talla TEXT NOT NULL,
                precio_base TEXT NOT NULL, iva_pct INTEGER NOT NULL, cantidad INTEGER NOT NULL CHECK (cantidad > 0),
                tipo_cliente TEXT CHECK (tipo_cliente IN ('PUBLICO', 'MAYORISTA', 'MEDICO') OR tipo_cliente IS NULL),
                descuento_pct TEXT NOT NULL, descuento TEXT NOT NULL,
                subtotal TEXT NOT NULL, iva TEXT NOT NULL, total TEXT NOT NULL,
                UNIQUE (proforma_numero, posicion),
                FOREIGN KEY (proforma_numero) REFERENCES proformas(numero) ON DELETE CASCADE)
            """};

    private InicializadorEsquema() {}

    static void inicializar(BaseDatosSQLite baseDatos) {
        try (Connection conexion = baseDatos.abrirConexion()) {
            conexion.setAutoCommit(false);
            try {
                for (String tabla : TABLAS) {
                    try (PreparedStatement sentencia = conexion.prepareStatement(tabla)) {
                        sentencia.execute();
                    }
                }
                try (PreparedStatement sentencia = conexion.prepareStatement(
                        "INSERT OR IGNORE INTO secuencias (nombre, valor) VALUES ('proformas', 0)")) {
                    sentencia.executeUpdate();
                }
                asegurarTipoClienteEnItems(conexion);
                if (leerVersion(conexion) < VERSION) {
                    normalizarCodigosExistentes(conexion);
                    try (PreparedStatement sentencia = conexion.prepareStatement("PRAGMA user_version = " + VERSION)) {
                        sentencia.execute();
                    }
                }
                conexion.commit();
            } catch (SQLException | RuntimeException error) {
                conexion.rollback();
                throw error;
            }
        } catch (SQLException error) {
            throw new IllegalStateException("No se pudo inicializar el esquema SQLite", error);
        }
    }

    private static int leerVersion(Connection conexion) throws SQLException {
        try (PreparedStatement sentencia = conexion.prepareStatement("PRAGMA user_version");
                ResultSet fila = sentencia.executeQuery()) {
            return fila.next() ? fila.getInt(1) : 0;
        }
    }

    private static void asegurarTipoClienteEnItems(Connection conexion) throws SQLException {
        boolean existe = false;
        try (PreparedStatement sentencia = conexion.prepareStatement("PRAGMA table_info(items_proforma)");
                ResultSet columnas = sentencia.executeQuery()) {
            while (columnas.next()) {
                if ("tipo_cliente".equals(columnas.getString("name"))) {
                    existe = true;
                    break;
                }
            }
        }
        if (!existe) {
            try (PreparedStatement sentencia = conexion.prepareStatement("ALTER TABLE items_proforma"
                    + " ADD COLUMN tipo_cliente TEXT CHECK (tipo_cliente IN"
                    + " ('PUBLICO', 'MAYORISTA', 'MEDICO') OR tipo_cliente IS NULL)")) {
                sentencia.execute();
            }
            try (PreparedStatement sentencia = conexion.prepareStatement("UPDATE items_proforma SET tipo_cliente = ("
                    + "SELECT cliente_tipo FROM proformas WHERE proformas.numero = items_proforma.proforma_numero)"
                    + " WHERE tipo_cliente IS NULL")) {
                sentencia.executeUpdate();
            }
        }
    }

    private static void normalizarCodigosExistentes(Connection conexion) throws SQLException {
        Map<String, String> cambios = new HashMap<>();
        Map<String, String> originalesPorNormalizado = new HashMap<>();
        try (PreparedStatement sentencia = conexion.prepareStatement("SELECT codigo FROM productos");
                ResultSet filas = sentencia.executeQuery()) {
            while (filas.next()) {
                String original = filas.getString(1);
                String normalizado = Producto.normalizarCodigo(original);
                String anterior = originalesPorNormalizado.putIfAbsent(normalizado, original);
                if (anterior != null && !anterior.equals(original)) {
                    throw new IllegalStateException(
                            "No se pueden normalizar los codigos duplicados " + anterior + " y " + original);
                }
                if (!original.equals(normalizado)) {
                    cambios.put(original, normalizado);
                }
            }
        }
        List<Map.Entry<String, String>> pendientes = List.copyOf(cambios.entrySet());
        cambios.clear();
        int indice = 0;
        for (Map.Entry<String, String> cambio : pendientes) {
            String temporal = "__MIGRACION_F6_" + indice++ + "__";
            actualizarCodigo(conexion, cambio.getKey(), temporal);
            cambios.put(temporal, cambio.getValue());
        }
        cambios.forEach((temporal, normalizado) -> actualizarCodigoSinComprobacion(conexion, temporal, normalizado));
    }

    private static void actualizarCodigo(Connection conexion, String actual, String nuevo) throws SQLException {
        try (PreparedStatement sentencia =
                conexion.prepareStatement("UPDATE productos SET codigo = ? WHERE codigo = ?")) {
            sentencia.setString(1, nuevo);
            sentencia.setString(2, actual);
            sentencia.executeUpdate();
        }
    }

    private static void actualizarCodigoSinComprobacion(Connection conexion, String actual, String nuevo) {
        try {
            actualizarCodigo(conexion, actual, nuevo);
        } catch (SQLException error) {
            throw new IllegalStateException("No se pudo normalizar el codigo " + actual, error);
        }
    }
}
