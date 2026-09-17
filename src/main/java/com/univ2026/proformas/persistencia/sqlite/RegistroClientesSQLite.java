package com.univ2026.proformas.persistencia.sqlite;

import com.univ2026.proformas.dominio.Estado;
import com.univ2026.proformas.dominio.cliente.Cliente;
import com.univ2026.proformas.dominio.cliente.RegistroClientes;
import com.univ2026.proformas.dominio.cliente.TipoCliente;
import com.univ2026.proformas.dominio.valor.Email;
import com.univ2026.proformas.dominio.valor.RUC;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Registro de clientes cuya unica fuente de verdad es SQLite. */
public final class RegistroClientesSQLite extends ColeccionSQLite<Cliente> implements RegistroClientes {
    private static final String COLUMNAS = "identificacion, nombre, direccion, telefono, email, tipo, estado";

    public RegistroClientesSQLite(Path archivoBaseDatos) {
        super(archivoBaseDatos);
    }

    public RegistroClientesSQLite(BaseDatosSQLite baseDatos) {
        super(baseDatos);
    }

    @Override
    public void registrar(Cliente cliente) {
        if (cliente == null) {
            throw new IllegalArgumentException("El cliente no puede ser null");
        }
        String sql = "INSERT INTO clientes (" + COLUMNAS + ") VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conexion = abrirConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, cliente.getIdentificacion().valor());
            sentencia.setString(2, cliente.getNombre());
            sentencia.setString(3, cliente.getDireccion());
            sentencia.setString(4, cliente.getTelefono());
            sentencia.setString(5, cliente.getEmail().valor());
            sentencia.setString(6, cliente.getTipo().name());
            sentencia.setString(7, cliente.getEstado().name());
            sentencia.executeUpdate();
        } catch (SQLException error) {
            if (error.getErrorCode() == 19) {
                throw new IllegalArgumentException(
                        "Ya existe un cliente con identificacion " + cliente.getIdentificacion(), error);
            }
            throw new IllegalStateException("No se pudo registrar el cliente", error);
        }
    }

    @Override
    public Cliente actualizar(Cliente cliente) {
        if (cliente == null) {
            throw new IllegalArgumentException("El cliente no puede ser null");
        }
        String sql = "UPDATE clientes SET nombre = ?, direccion = ?, telefono = ?, email = ?, tipo = ?, estado = ?"
                + " WHERE identificacion = ?";
        try (Connection conexion = abrirConexion();
                PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, cliente.getNombre());
            sentencia.setString(2, cliente.getDireccion());
            sentencia.setString(3, cliente.getTelefono());
            sentencia.setString(4, cliente.getEmail().valor());
            sentencia.setString(5, cliente.getTipo().name());
            sentencia.setString(6, cliente.getEstado().name());
            sentencia.setString(7, cliente.getIdentificacion().valor());
            if (sentencia.executeUpdate() == 0) {
                throw new IllegalArgumentException("No existe el cliente " + cliente.getIdentificacion());
            }
            return buscarPorIdentificacion(cliente.getIdentificacion());
        } catch (SQLException error) {
            throw new IllegalStateException("No se pudo actualizar el cliente", error);
        }
    }

    @Override
    public void eliminar(String identificacion) {
        RUC ruc = new RUC(identificacion);
        try (Connection conexion = abrirConexion();
                PreparedStatement sentencia =
                        conexion.prepareStatement("DELETE FROM clientes WHERE identificacion = ?")) {
            sentencia.setString(1, ruc.valor());
            if (sentencia.executeUpdate() == 0) {
                throw new IllegalArgumentException("No existe el cliente " + ruc);
            }
        } catch (SQLException error) {
            if (error.getErrorCode() == 19) {
                throw new IllegalStateException(
                        "No se puede eliminar el cliente porque tiene proformas asociadas", error);
            }
            throw new IllegalStateException("No se pudo eliminar el cliente", error);
        }
    }

    @Override
    public Cliente buscarPorIdentificacion(RUC identificacion) {
        if (identificacion == null) {
            throw new IllegalArgumentException("La identificacion no puede ser null");
        }
        List<Cliente> resultados = consultarLista(
                "SELECT " + COLUMNAS + " FROM clientes WHERE identificacion = ?",
                sentencia -> sentencia.setString(1, identificacion.valor()));
        return resultados.isEmpty() ? null : resultados.get(0);
    }

    @Override
    public List<Cliente> listar() {
        return consultarLista("SELECT " + COLUMNAS + " FROM clientes ORDER BY identificacion", sentencia -> {});
    }

    @Override
    public List<Cliente> buscar(String texto) {
        String patronIdentificacion = patronLike(texto, true);
        String patronNombre = patronLike(texto, false);
        return consultarLista(
                "SELECT " + COLUMNAS
                        + " FROM clientes WHERE identificacion LIKE ? ESCAPE '\\'"
                        + " OR nombre LIKE ? ESCAPE '\\' COLLATE NOCASE ORDER BY identificacion",
                sentencia -> {
                    sentencia.setString(1, patronIdentificacion);
                    sentencia.setString(2, patronNombre);
                });
    }

    @Override
    public Cliente cambiarEstado(String identificacion, Estado estado) {
        if (estado == null) {
            throw new IllegalArgumentException("El estado no puede ser null");
        }
        RUC ruc = new RUC(identificacion);
        try (Connection conexion = abrirConexion();
                PreparedStatement sentencia =
                        conexion.prepareStatement("UPDATE clientes SET estado = ? WHERE identificacion = ?")) {
            sentencia.setString(1, estado.name());
            sentencia.setString(2, ruc.valor());
            if (sentencia.executeUpdate() == 0) {
                throw new IllegalArgumentException("No existe el cliente " + ruc);
            }
        } catch (SQLException error) {
            throw new IllegalStateException("No se pudo cambiar el estado del cliente", error);
        }
        return buscarPorIdentificacion(ruc);
    }

    @Override
    public Map<String, Cliente> indexarPorIdentificacion() {
        Map<String, Cliente> indice = new HashMap<>();
        listar().forEach(cliente -> indice.put(cliente.getIdentificacion().valor(), cliente));
        return Map.copyOf(indice);
    }

    @Override
    public Set<String> listarIdentificaciones() {
        Set<String> identificaciones = new HashSet<>();
        listar().forEach(cliente ->
                identificaciones.add(cliente.getIdentificacion().valor()));
        return Set.copyOf(identificaciones);
    }

    @Override
    protected Cliente convertirFila(ResultSet fila) throws SQLException {
        return new Cliente(
                new RUC(fila.getString("identificacion")),
                fila.getString("nombre"),
                fila.getString("direccion"),
                fila.getString("telefono"),
                new Email(fila.getString("email")),
                TipoCliente.valueOf(fila.getString("tipo")),
                Estado.valueOf(fila.getString("estado")));
    }
}
