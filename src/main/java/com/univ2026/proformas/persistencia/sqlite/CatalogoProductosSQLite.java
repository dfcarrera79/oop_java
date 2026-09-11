package com.univ2026.proformas.persistencia.sqlite;

import com.univ2026.proformas.dominio.Estado;
import com.univ2026.proformas.dominio.producto.AtributosDigitales;
import com.univ2026.proformas.dominio.producto.AtributosFisicos;
import com.univ2026.proformas.dominio.producto.AtributosProducto;
import com.univ2026.proformas.dominio.producto.CatalogoProductos;
import com.univ2026.proformas.dominio.producto.Producto;
import com.univ2026.proformas.dominio.producto.Talla;
import com.univ2026.proformas.dominio.valor.Monto;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Catalogo de productos cuya unica fuente de verdad es SQLite. */
public final class CatalogoProductosSQLite extends ColeccionSQLite<Producto> implements CatalogoProductos {
    private static final String COLUMNAS =
            "codigo, nombre, descripcion, precio, iva_pct, estado, tipo_atributos, peso_kg, talla, tamanio_mb";

    public CatalogoProductosSQLite(Path archivoBaseDatos) {
        super(archivoBaseDatos);
    }

    @Override
    public void registrar(Producto producto) {
        if (producto == null) {
            throw new IllegalArgumentException("El producto no puede ser null");
        }
        String sql = "INSERT INTO productos (" + COLUMNAS + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conexion = abrirConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, producto.getCodigo());
            sentencia.setString(2, producto.getNombre());
            sentencia.setString(3, producto.getDescripcion());
            sentencia.setString(4, producto.getPrecio().toString());
            sentencia.setDouble(5, producto.getIvaPct());
            sentencia.setString(6, producto.getEstado().name());
            asignarAtributos(sentencia, producto.getExtras());
            sentencia.executeUpdate();
        } catch (SQLException error) {
            if (error.getErrorCode() == 19) {
                throw new IllegalArgumentException("Ya existe un producto con codigo " + producto.getCodigo(), error);
            }
            throw new IllegalStateException("No se pudo registrar el producto", error);
        }
    }

    @Override
    public Producto buscarPorCodigo(String codigo) {
        String normalizado = textoObligatorio(codigo, "El codigo no puede estar vacio");
        List<Producto> resultados = consultarLista(
                "SELECT " + COLUMNAS + " FROM productos WHERE codigo = ?",
                sentencia -> sentencia.setString(1, normalizado));
        return resultados.isEmpty() ? null : resultados.get(0);
    }

    @Override
    public List<Producto> listar() {
        return consultarLista("SELECT " + COLUMNAS + " FROM productos ORDER BY codigo", sentencia -> {});
    }

    @Override
    public List<Producto> buscar(String texto) {
        String patron = "%" + (texto == null ? "" : texto.trim()) + "%";
        return consultarLista(
                "SELECT " + COLUMNAS
                        + " FROM productos WHERE codigo LIKE ? COLLATE NOCASE OR nombre LIKE ? COLLATE NOCASE"
                        + " OR descripcion LIKE ? COLLATE NOCASE ORDER BY codigo",
                sentencia -> {
                    sentencia.setString(1, patron);
                    sentencia.setString(2, patron);
                    sentencia.setString(3, patron);
                });
    }

    @Override
    public Producto cambiarEstado(String codigo, Estado estado) {
        if (estado == null) {
            throw new IllegalArgumentException("El estado no puede ser null");
        }
        String normalizado = textoObligatorio(codigo, "El codigo no puede estar vacio");
        try (Connection conexion = abrirConexion();
                PreparedStatement sentencia =
                        conexion.prepareStatement("UPDATE productos SET estado = ? WHERE codigo = ?")) {
            sentencia.setString(1, estado.name());
            sentencia.setString(2, normalizado);
            if (sentencia.executeUpdate() == 0) {
                throw new IllegalArgumentException("No existe el producto " + normalizado);
            }
        } catch (SQLException error) {
            throw new IllegalStateException("No se pudo cambiar el estado del producto", error);
        }
        return buscarPorCodigo(normalizado);
    }

    @Override
    public Map<String, Producto> indexarPorCodigo() {
        Map<String, Producto> indice = new HashMap<>();
        listar().forEach(producto -> indice.put(producto.getCodigo(), producto));
        return Map.copyOf(indice);
    }

    @Override
    public Set<String> listarCodigos() {
        Set<String> codigos = new HashSet<>();
        listar().forEach(producto -> codigos.add(producto.getCodigo()));
        return Set.copyOf(codigos);
    }

    @Override
    protected Producto convertirFila(ResultSet fila) throws SQLException {
        return new Producto(
                fila.getString("codigo"),
                fila.getString("nombre"),
                fila.getString("descripcion"),
                new Monto(fila.getString("precio")),
                fila.getDouble("iva_pct"),
                Estado.valueOf(fila.getString("estado")),
                leerAtributos(fila));
    }

    private static void asignarAtributos(PreparedStatement sentencia, AtributosProducto atributos) throws SQLException {
        if (atributos instanceof AtributosFisicos fisicos) {
            sentencia.setString(7, "FISICO");
            sentencia.setDouble(8, fisicos.pesoKg());
            if (fisicos.talla() == null) {
                sentencia.setNull(9, Types.VARCHAR);
            } else {
                sentencia.setString(9, fisicos.talla().name());
            }
            sentencia.setNull(10, Types.REAL);
        } else if (atributos instanceof AtributosDigitales digitales) {
            sentencia.setString(7, "DIGITAL");
            sentencia.setNull(8, Types.REAL);
            sentencia.setNull(9, Types.VARCHAR);
            sentencia.setDouble(10, digitales.tamanioMb());
        } else {
            sentencia.setNull(7, Types.VARCHAR);
            sentencia.setNull(8, Types.REAL);
            sentencia.setNull(9, Types.VARCHAR);
            sentencia.setNull(10, Types.REAL);
        }
    }

    private static AtributosProducto leerAtributos(ResultSet fila) throws SQLException {
        String tipo = fila.getString("tipo_atributos");
        if ("FISICO".equals(tipo)) {
            String talla = fila.getString("talla");
            return new AtributosFisicos(fila.getDouble("peso_kg"), talla == null ? null : Talla.valueOf(talla));
        }
        if ("DIGITAL".equals(tipo)) {
            return new AtributosDigitales(fila.getDouble("tamanio_mb"));
        }
        return null;
    }
}
