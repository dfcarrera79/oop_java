package com.univ2026.proformas.persistencia.sqlite;

import com.univ2026.proformas.dominio.pago.CuentaPago;
import com.univ2026.proformas.dominio.pago.RepositorioCuentasPago;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/** Repositorio SQLite de cuentas de pago. */
public final class RepositorioCuentasPagoSQLite extends ColeccionSQLite<CuentaPago> implements RepositorioCuentasPago {
    public RepositorioCuentasPagoSQLite(Path archivoBaseDatos) {
        super(archivoBaseDatos);
    }

    public RepositorioCuentasPagoSQLite(BaseDatosSQLite baseDatos) {
        super(baseDatos);
    }

    @Override
    public CuentaPago registrar(CuentaPago cuenta) {
        validarNueva(cuenta);
        String sql = "INSERT INTO cuentas_pago (nombre, instrucciones) VALUES (?, ?)";
        try (Connection conexion = abrirConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            sentencia.setString(1, cuenta.getNombre());
            sentencia.setString(2, cuenta.getInstrucciones());
            sentencia.executeUpdate();
            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (!claves.next()) {
                    throw new IllegalStateException("SQLite no retorno el id de la cuenta");
                }
                cuenta.setId(claves.getLong(1));
                return cuenta;
            }
        } catch (SQLException error) {
            if (error.getErrorCode() == 19) {
                throw new IllegalArgumentException("Ya existe una cuenta con ese nombre", error);
            }
            throw new IllegalStateException("No se pudo registrar la cuenta de pago", error);
        }
    }

    @Override
    public CuentaPago actualizar(CuentaPago cuenta) {
        if (cuenta == null || cuenta.getId() == null) {
            throw new IllegalArgumentException("La cuenta debe tener id para actualizarse");
        }
        try (Connection conexion = abrirConexion();
                PreparedStatement sentencia = conexion.prepareStatement(
                        "UPDATE cuentas_pago SET nombre = ?, instrucciones = ? WHERE id = ?")) {
            sentencia.setString(1, cuenta.getNombre());
            sentencia.setString(2, cuenta.getInstrucciones());
            sentencia.setLong(3, cuenta.getId());
            if (sentencia.executeUpdate() == 0) {
                throw new IllegalArgumentException("No existe la cuenta " + cuenta.getId());
            }
            return buscarPorId(cuenta.getId());
        } catch (SQLException error) {
            throw new IllegalStateException("No se pudo actualizar la cuenta de pago", error);
        }
    }

    @Override
    public CuentaPago buscarPorId(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("El id debe ser positivo");
        }
        List<CuentaPago> resultados = consultarLista(
                "SELECT id, nombre, instrucciones FROM cuentas_pago WHERE id = ?",
                sentencia -> sentencia.setLong(1, id));
        return resultados.isEmpty() ? null : resultados.get(0);
    }

    @Override
    public List<CuentaPago> listar() {
        return consultarLista("SELECT id, nombre, instrucciones FROM cuentas_pago ORDER BY nombre", sentencia -> {});
    }

    @Override
    public List<CuentaPago> buscar(String texto) {
        String patron = patronLike(texto, false);
        return consultarLista(
                "SELECT id, nombre, instrucciones FROM cuentas_pago"
                        + " WHERE nombre LIKE ? ESCAPE '\\' COLLATE NOCASE ORDER BY nombre",
                sentencia -> sentencia.setString(1, patron));
    }

    @Override
    public void eliminar(long id) {
        try (Connection conexion = abrirConexion();
                PreparedStatement sentencia = conexion.prepareStatement("DELETE FROM cuentas_pago WHERE id = ?")) {
            sentencia.setLong(1, id);
            if (sentencia.executeUpdate() == 0) {
                throw new IllegalArgumentException("No existe la cuenta " + id);
            }
        } catch (SQLException error) {
            throw new IllegalStateException("No se pudo eliminar la cuenta de pago", error);
        }
    }

    @Override
    protected CuentaPago convertirFila(ResultSet fila) throws SQLException {
        return new CuentaPago(fila.getLong("id"), fila.getString("nombre"), fila.getString("instrucciones"));
    }

    private static void validarNueva(CuentaPago cuenta) {
        if (cuenta == null) {
            throw new IllegalArgumentException("La cuenta no puede ser null");
        }
        if (cuenta.getId() != null) {
            throw new IllegalArgumentException("Una cuenta nueva no debe tener id");
        }
    }
}
