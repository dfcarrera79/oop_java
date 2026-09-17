package com.univ2026.proformas.persistencia.sqlite;

import com.univ2026.proformas.dominio.Estado;
import com.univ2026.proformas.dominio.cliente.Cliente;
import com.univ2026.proformas.dominio.cliente.TipoCliente;
import com.univ2026.proformas.dominio.proforma.ItemProforma;
import com.univ2026.proformas.dominio.proforma.Proforma;
import com.univ2026.proformas.dominio.proforma.RepositorioProformas;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Persistencia transaccional de documentos y snapshots historicos. */
public final class RepositorioProformasSQLite implements RepositorioProformas {
    private static final String COLUMNAS_CABECERA = "numero, fecha, cliente_identificacion, cliente_nombre,"
            + " cliente_direccion, cliente_telefono, cliente_email, cliente_tipo, cliente_estado,"
            + " observaciones, instrucciones_pago";

    private final BaseDatosSQLite baseDatos;

    public RepositorioProformasSQLite(Path archivoBaseDatos) {
        this(new BaseDatosSQLite(archivoBaseDatos));
    }

    public RepositorioProformasSQLite(BaseDatosSQLite baseDatos) {
        if (baseDatos == null) {
            throw new IllegalArgumentException("La base de datos no puede ser null");
        }
        this.baseDatos = baseDatos;
    }

    @Override
    public Proforma guardar(Proforma proforma) {
        validarNueva(proforma);
        String numero;
        try (Connection conexion = baseDatos.abrirConexion()) {
            conexion.setAutoCommit(false);
            try {
                numero = siguienteNumero(conexion);
                insertarCabecera(conexion, numero, proforma);
                insertarItems(conexion, numero, proforma.getItems());
                conexion.commit();
            } catch (SQLException | RuntimeException error) {
                conexion.rollback();
                throw error;
            }
        } catch (SQLException error) {
            if (error.getErrorCode() == 19 && error.getMessage().contains("FOREIGN KEY")) {
                throw new IllegalStateException(
                        "No se puede guardar la proforma porque el cliente ya no existe", error);
            }
            throw new IllegalStateException("No se pudo guardar la proforma; no se realizo ningun cambio", error);
        }
        proforma.asignarNumero(numero);
        return proforma;
    }

    @Override
    public Proforma buscarPorNumero(String numero) {
        String normalizado = textoObligatorio(numero, "El numero no puede estar vacio");
        List<Proforma> resultados = consultarCabeceras(
                "SELECT " + COLUMNAS_CABECERA + " FROM proformas WHERE numero = ?",
                sentencia -> sentencia.setString(1, normalizado));
        return resultados.isEmpty() ? null : resultados.get(0);
    }

    @Override
    public List<Proforma> listar() {
        return consultarCabeceras(
                "SELECT " + COLUMNAS_CABECERA + " FROM proformas ORDER BY numero DESC", sentencia -> {});
    }

    @Override
    public List<Proforma> buscar(String texto) {
        String patron = patronLike(texto);
        return consultarCabeceras(
                "SELECT " + COLUMNAS_CABECERA + " FROM proformas"
                        + " WHERE numero LIKE ? ESCAPE '\\' COLLATE NOCASE"
                        + " OR cliente_identificacion LIKE ? ESCAPE '\\'"
                        + " OR cliente_nombre LIKE ? ESCAPE '\\' COLLATE NOCASE ORDER BY numero DESC",
                sentencia -> {
                    sentencia.setString(1, patron);
                    sentencia.setString(2, patron);
                    sentencia.setString(3, patron);
                });
    }

    @Override
    public void eliminar(String numero) {
        String normalizado = textoObligatorio(numero, "El numero no puede estar vacio");
        try (Connection conexion = baseDatos.abrirConexion();
                PreparedStatement sentencia = conexion.prepareStatement("DELETE FROM proformas WHERE numero = ?")) {
            sentencia.setString(1, normalizado);
            if (sentencia.executeUpdate() == 0) {
                throw new IllegalArgumentException("No existe la proforma " + normalizado);
            }
        } catch (SQLException error) {
            throw new IllegalStateException("No se pudo eliminar la proforma", error);
        }
    }

    private static void validarNueva(Proforma proforma) {
        if (proforma == null) {
            throw new IllegalArgumentException("La proforma no puede ser null");
        }
        if (proforma.getNumero() != null) {
            throw new IllegalArgumentException("La proforma ya tiene numero");
        }
        if (proforma.getItems().isEmpty()) {
            throw new IllegalArgumentException("La proforma debe tener al menos un item");
        }
    }

    private static String siguienteNumero(Connection conexion) throws SQLException {
        try (PreparedStatement actualizar =
                conexion.prepareStatement("UPDATE secuencias SET valor = valor + 1 WHERE nombre = 'proformas'")) {
            if (actualizar.executeUpdate() != 1) {
                throw new SQLException("No existe la secuencia de proformas");
            }
        }
        try (PreparedStatement consultar =
                        conexion.prepareStatement("SELECT valor FROM secuencias WHERE nombre = 'proformas'");
                ResultSet fila = consultar.executeQuery()) {
            if (!fila.next()) {
                throw new SQLException("No se pudo leer la secuencia de proformas");
            }
            return "PRO-%06d".formatted(fila.getLong(1));
        }
    }

    private static void insertarCabecera(Connection conexion, String numero, Proforma proforma) throws SQLException {
        String sql = "INSERT INTO proformas (" + COLUMNAS_CABECERA
                + ", subtotal, iva, total) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Cliente cliente = proforma.getCliente();
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, numero);
            sentencia.setString(2, proforma.getFecha().toString());
            sentencia.setString(3, cliente.getIdentificacion().valor());
            sentencia.setString(4, cliente.getNombre());
            sentencia.setString(5, cliente.getDireccion());
            sentencia.setString(6, cliente.getTelefono());
            sentencia.setString(7, cliente.getEmail().valor());
            sentencia.setString(8, cliente.getTipo().name());
            sentencia.setString(9, cliente.getEstado().name());
            sentencia.setString(10, proforma.getObservaciones());
            sentencia.setString(11, proforma.getInstruccionesPago());
            sentencia.setString(12, proforma.calcularSubtotal().toPlainString());
            sentencia.setString(13, proforma.calcularImpuesto().toPlainString());
            sentencia.setString(14, proforma.calcularTotal().toPlainString());
            sentencia.executeUpdate();
        }
    }

    private static void insertarItems(Connection conexion, String numero, List<ItemProforma> items)
            throws SQLException {
        String sql = "INSERT INTO items_proforma (proforma_numero, posicion, producto_codigo, producto_nombre,"
                + " producto_descripcion, tipo_producto, talla, precio_base, iva_pct, cantidad, tipo_cliente,"
                + " descuento_pct, descuento, subtotal, iva, total) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            for (int indice = 0; indice < items.size(); indice++) {
                ItemProforma item = items.get(indice);
                sentencia.setString(1, numero);
                sentencia.setInt(2, indice + 1);
                sentencia.setString(3, item.getProducto().getCodigo());
                sentencia.setString(4, item.getProducto().getNombre());
                sentencia.setString(5, item.getProducto().getDescripcion());
                sentencia.setString(6, item.getTipoProducto());
                sentencia.setString(
                        7, item.getTalla() == null ? "" : item.getTalla().name());
                sentencia.setString(8, item.getProducto().getPrecio().toString());
                sentencia.setInt(9, item.getProducto().getIvaPct());
                sentencia.setInt(10, item.getCantidad());
                if (item.getTipoCliente() == null) {
                    sentencia.setNull(11, java.sql.Types.VARCHAR);
                } else {
                    sentencia.setString(11, item.getTipoCliente().name());
                }
                sentencia.setString(12, item.getDescuentoPct().toPlainString());
                sentencia.setString(13, item.calcularDescuento().toPlainString());
                sentencia.setString(14, item.calcularSubtotal().toPlainString());
                sentencia.setString(15, item.calcularImpuesto().toPlainString());
                sentencia.setString(16, item.calcularTotal().toPlainString());
                sentencia.executeUpdate();
            }
        }
    }

    private List<Proforma> consultarCabeceras(String sql, PreparadorSQL preparador) {
        List<Proforma> resultados = new ArrayList<>();
        try (Connection conexion = baseDatos.abrirConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            preparador.preparar(sentencia);
            try (ResultSet filas = sentencia.executeQuery()) {
                while (filas.next()) {
                    Proforma proforma = convertirCabecera(filas);
                    cargarItems(conexion, proforma);
                    resultados.add(proforma);
                }
            }
            return List.copyOf(resultados);
        } catch (SQLException error) {
            throw new IllegalStateException("No se pudieron consultar las proformas", error);
        }
    }

    private static Proforma convertirCabecera(ResultSet fila) throws SQLException {
        Cliente cliente = new Cliente(
                fila.getString("cliente_identificacion"),
                fila.getString("cliente_nombre"),
                fila.getString("cliente_direccion"),
                fila.getString("cliente_telefono"),
                fila.getString("cliente_email"),
                TipoCliente.valueOf(fila.getString("cliente_tipo")),
                Estado.valueOf(fila.getString("cliente_estado")));
        Proforma proforma = new Proforma(
                cliente,
                LocalDate.parse(fila.getString("fecha")),
                fila.getString("observaciones"),
                fila.getString("instrucciones_pago"));
        proforma.asignarNumero(fila.getString("numero"));
        return proforma;
    }

    private static void cargarItems(Connection conexion, Proforma proforma) throws SQLException {
        String sql =
                "SELECT producto_codigo, producto_nombre, producto_descripcion, tipo_producto, talla, tipo_cliente,"
                        + " precio_base, iva_pct, cantidad, descuento_pct FROM items_proforma"
                        + " WHERE proforma_numero = ? ORDER BY posicion";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, proforma.getNumero());
            try (ResultSet filas = sentencia.executeQuery()) {
                while (filas.next()) {
                    proforma.agregarItem(ItemProforma.restaurar(
                            filas.getString("producto_codigo"),
                            filas.getString("producto_nombre"),
                            filas.getString("producto_descripcion"),
                            filas.getString("tipo_producto"),
                            filas.getString("talla"),
                            leerTipoCliente(filas.getString("tipo_cliente")),
                            new BigDecimal(filas.getString("precio_base")),
                            filas.getInt("iva_pct"),
                            filas.getInt("cantidad"),
                            new BigDecimal(filas.getString("descuento_pct"))));
                }
            }
        }
    }

    private static TipoCliente leerTipoCliente(String valor) {
        return valor == null ? null : TipoCliente.valueOf(valor);
    }

    private static String textoObligatorio(String valor, String mensaje) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensaje);
        }
        return valor.trim();
    }

    private static String patronLike(String texto) {
        String valor = texto == null ? "" : texto.trim();
        valor = valor.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        return "%" + valor + "%";
    }
}
