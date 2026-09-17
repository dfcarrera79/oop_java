package com.univ2026.proformas.persistencia.sqlite;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.univ2026.proformas.dominio.Estado;
import com.univ2026.proformas.dominio.cliente.Cliente;
import com.univ2026.proformas.dominio.cliente.TipoCliente;
import com.univ2026.proformas.dominio.pago.CuentaPago;
import com.univ2026.proformas.dominio.producto.AtributosFisicos;
import com.univ2026.proformas.dominio.producto.Producto;
import com.univ2026.proformas.dominio.producto.Talla;
import com.univ2026.proformas.dominio.proforma.ItemProforma;
import com.univ2026.proformas.dominio.proforma.Proforma;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PersistenciaFase6Test {
    @TempDir
    Path temporal;

    @Test
    void migraFase5NormalizaCodigosYConservaFilas() throws Exception {
        Path archivo = temporal.resolve("fase5.db");
        try (var conexion = DriverManager.getConnection("jdbc:sqlite:" + archivo);
                var crear = conexion.prepareStatement("""
                        CREATE TABLE productos (
                            codigo TEXT PRIMARY KEY, nombre TEXT NOT NULL, descripcion TEXT NOT NULL,
                            precio TEXT NOT NULL, iva_pct REAL NOT NULL, estado TEXT NOT NULL,
                            tipo_atributos TEXT, peso_kg REAL, talla TEXT, tamanio_mb REAL)
                        """)) {
            crear.execute();
            try (var insertar = conexion.prepareStatement(
                    "INSERT INTO productos VALUES (?, ?, '', '10.00', 15, 'ACTIVO', NULL, NULL, NULL, NULL)")) {
                insertar.setString(1, " abc-1 ");
                insertar.setString(2, "Anterior");
                insertar.executeUpdate();
            }
        }

        CatalogoProductosSQLite catalogo = new CatalogoProductosSQLite(archivo);

        assertEquals("ABC-1", catalogo.listar().get(0).getCodigo());
        assertEquals("Anterior", catalogo.buscarPorCodigo("abc-1").getNombre());
        try (var conexion = DriverManager.getConnection("jdbc:sqlite:" + archivo);
                var version = conexion.prepareStatement("PRAGMA user_version");
                var fila = version.executeQuery()) {
            assertTrue(fila.next());
            assertEquals(6, fila.getInt(1));
        }
    }

    @Test
    void crudFisicoMantieneClavesYReabreDatos() {
        Path archivo = temporal.resolve("crud.db");
        CatalogoProductosSQLite productos = new CatalogoProductosSQLite(archivo);
        Producto producto =
                new Producto("p-1", "Faja", "Inicial", 10, 15, Estado.ACTIVO, new AtributosFisicos(1, Talla.M));
        productos.registrar(producto);
        producto.setNombre("Faja nueva");
        productos.actualizar(producto);

        RegistroClientesSQLite clientes = new RegistroClientesSQLite(archivo);
        Cliente cliente = new Cliente("1100000001", "Ana");
        clientes.registrar(cliente);
        cliente.setNombre("Ana Nueva");
        clientes.actualizar(cliente);

        assertEquals(
                "Faja nueva",
                new CatalogoProductosSQLite(archivo).buscarPorCodigo("P-1").getNombre());
        assertEquals(
                "Ana Nueva",
                new RegistroClientesSQLite(archivo)
                        .buscarPorIdentificacion("1100000001")
                        .getNombre());
        productos.eliminar("p-1");
        clientes.eliminar("1100000001");
        assertNull(productos.buscarPorCodigo("P-1"));
        assertNull(clientes.buscarPorIdentificacion("1100000001"));
    }

    @Test
    void cuentasPersistenCrudYBusquedaAlReabrir() {
        Path archivo = temporal.resolve("cuentas.db");
        RepositorioCuentasPagoSQLite repositorio = new RepositorioCuentasPagoSQLite(archivo);
        CuentaPago cuenta = repositorio.registrar(new CuentaPago("Banco Loja", "Cuenta 1"));
        assertNotNull(cuenta.getId());
        cuenta.setInstrucciones("Cuenta 2");
        repositorio.actualizar(cuenta);

        RepositorioCuentasPagoSQLite reabierto = new RepositorioCuentasPagoSQLite(archivo);
        assertEquals("Cuenta 2", reabierto.buscarPorId(cuenta.getId()).getInstrucciones());
        assertEquals(1, reabierto.buscar("LOJA").size());
        reabierto.eliminar(cuenta.getId());
        assertTrue(reabierto.listar().isEmpty());
    }

    @Test
    void guardaSnapshotsPermiteBorrarProductoYRestringeCliente() {
        Contexto contexto = contexto("documentos.db");
        Proforma proforma = proforma(contexto.cliente(), contexto.producto());
        contexto.proformas().guardar(proforma);

        contexto.productos().eliminar("P-001");
        IllegalStateException errorCliente = assertThrows(
                IllegalStateException.class, () -> contexto.clientes().eliminar("1100000001"));
        assertTrue(errorCliente.getMessage().contains("tiene proformas asociadas"));

        Proforma recuperada = new RepositorioProformasSQLite(contexto.archivo()).buscarPorNumero("PRO-000001");
        assertEquals(
                "Producto historico", recuperada.getItems().get(0).getProducto().getNombre());
        assertEquals("FISICO", recuperada.getItems().get(0).getTipoProducto());
        assertEquals(Talla.L, recuperada.getItems().get(0).getTalla());
        assertEquals(TipoCliente.PUBLICO, recuperada.getItems().get(0).getTipoCliente());
        assertEquals(new BigDecimal("9.78"), recuperada.calcularTotal());
    }

    @Test
    void secuenciaNoReutilizaNumerosYEliminacionHaceCascada() throws Exception {
        Contexto contexto = contexto("secuencia.db");
        Proforma primera = proforma(contexto.cliente(), contexto.producto());
        contexto.proformas().guardar(primera);
        contexto.proformas().eliminar(primera.getNumero());
        Proforma segunda = proforma(contexto.cliente(), contexto.producto());
        contexto.proformas().guardar(segunda);

        assertEquals("PRO-000002", segunda.getNumero());
        contexto.proformas().eliminar(segunda.getNumero());
        try (var conexion = DriverManager.getConnection("jdbc:sqlite:" + contexto.archivo());
                var contar = conexion.prepareStatement("SELECT COUNT(*) FROM items_proforma");
                var fila = contar.executeQuery()) {
            assertTrue(fila.next());
            assertEquals(0, fila.getInt(1));
        }
    }

    @Test
    void falloDeItemRevierteCabeceraYSecuencia() throws Exception {
        Contexto contexto = contexto("rollback.db");
        try (var conexion = DriverManager.getConnection("jdbc:sqlite:" + contexto.archivo());
                var trigger = conexion.prepareStatement("""
                        CREATE TRIGGER fallar_item BEFORE INSERT ON items_proforma
                        BEGIN SELECT RAISE(ABORT, 'fallo provocado'); END
                        """)) {
            trigger.execute();
        }

        assertThrows(
                IllegalStateException.class,
                () -> contexto.proformas().guardar(proforma(contexto.cliente(), contexto.producto())));

        assertTrue(contexto.proformas().listar().isEmpty());
        try (var conexion = DriverManager.getConnection("jdbc:sqlite:" + contexto.archivo());
                var consultar = conexion.prepareStatement("SELECT valor FROM secuencias WHERE nombre = 'proformas'");
                var fila = consultar.executeQuery()) {
            assertTrue(fila.next());
            assertEquals(0, fila.getInt(1));
        }
    }

    @Test
    void migraTipoClienteFaltanteYLoReconstruyeSinCambiarDescuento() throws Exception {
        Contexto contexto = contexto("migracion-item.db");
        contexto.proformas().guardar(proforma(contexto.cliente(), contexto.producto()));
        try (var conexion = DriverManager.getConnection("jdbc:sqlite:" + contexto.archivo());
                var eliminarColumna =
                        conexion.prepareStatement("ALTER TABLE items_proforma DROP COLUMN tipo_cliente")) {
            eliminarColumna.execute();
        }

        new BaseDatosSQLite(contexto.archivo());
        Proforma recuperada = new RepositorioProformasSQLite(contexto.archivo()).buscarPorNumero("PRO-000001");

        assertEquals(TipoCliente.PUBLICO, recuperada.getItems().get(0).getTipoCliente());
        assertEquals(new BigDecimal("15"), recuperada.getItems().get(0).getDescuentoPct());
        assertNotNull(new BaseDatosSQLite(contexto.archivo()));
    }

    @Test
    void guardarRechazaConMensajeClaroUnClienteEliminadoAntesDeEmitir() {
        Contexto contexto = contexto("cliente-eliminado.db");
        Proforma proforma = proforma(contexto.cliente(), contexto.producto());
        contexto.clientes().eliminar("1100000001");

        IllegalStateException error = assertThrows(
                IllegalStateException.class, () -> contexto.proformas().guardar(proforma));

        assertTrue(error.getMessage().contains("cliente ya no existe"));
        assertTrue(contexto.proformas().listar().isEmpty());
    }

    @Test
    void esquemaUsaDineroTextYClavesForaneasPorConexion() throws Exception {
        Path archivo = temporal.resolve("esquema-f6.db");
        BaseDatosSQLite base = new BaseDatosSQLite(archivo);
        try (var conexion = base.abrirConexion();
                var pragma = conexion.prepareStatement("PRAGMA foreign_keys");
                var fila = pragma.executeQuery()) {
            assertTrue(fila.next());
            assertEquals(1, fila.getInt(1));
        }
        try (var conexion = DriverManager.getConnection("jdbc:sqlite:" + archivo);
                var columnas = conexion.prepareStatement("PRAGMA table_info(proformas)");
                var filas = columnas.executeQuery()) {
            boolean totalText = false;
            while (filas.next()) {
                if ("total".equals(filas.getString("name"))) {
                    totalText = "TEXT".equals(filas.getString("type"));
                }
            }
            assertTrue(totalText);
        }
    }

    @Test
    void rutasSonInyectablesYRespetanXdgOFallback() {
        assertEquals(Path.of("data", "proformas.db"), RutasDatos.desarrollo());
        assertEquals(
                Path.of("/datos", "ec.edu.univ.proformas", "proformas.db"),
                RutasDatos.produccion(Map.of("XDG_DATA_HOME", "/datos"), "/home/ana"));
        assertEquals(
                Path.of("/home/ana", ".local", "share", "ec.edu.univ.proformas", "proformas.db"),
                RutasDatos.produccion(Map.of(), "/home/ana"));
    }

    private Contexto contexto(String nombreArchivo) {
        Path archivo = temporal.resolve(nombreArchivo);
        BaseDatosSQLite base = new BaseDatosSQLite(archivo);
        CatalogoProductosSQLite productos = new CatalogoProductosSQLite(base);
        RegistroClientesSQLite clientes = new RegistroClientesSQLite(base);
        Producto producto = new Producto(
                "P-001", "Producto historico", "", 10, 15, Estado.ACTIVO, new AtributosFisicos(1, Talla.M));
        Cliente cliente = new Cliente("1100000001", "Cliente historico");
        productos.registrar(producto);
        clientes.registrar(cliente);
        return new Contexto(archivo, productos, clientes, new RepositorioProformasSQLite(base), producto, cliente);
    }

    private static Proforma proforma(Cliente cliente, Producto producto) {
        Proforma proforma = new Proforma(cliente, LocalDate.of(2026, 9, 13), "Observacion", "Transferencia");
        proforma.agregarItem(new ItemProforma(producto, 1, cliente.getTipo(), Talla.L));
        return proforma;
    }

    private record Contexto(
            Path archivo,
            CatalogoProductosSQLite productos,
            RegistroClientesSQLite clientes,
            RepositorioProformasSQLite proformas,
            Producto producto,
            Cliente cliente) {}
}
