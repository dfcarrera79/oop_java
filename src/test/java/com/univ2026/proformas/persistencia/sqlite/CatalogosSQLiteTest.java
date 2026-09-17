package com.univ2026.proformas.persistencia.sqlite;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.univ2026.proformas.dominio.Estado;
import com.univ2026.proformas.dominio.cliente.Cliente;
import com.univ2026.proformas.dominio.cliente.TipoCliente;
import com.univ2026.proformas.dominio.producto.AtributosDigitales;
import com.univ2026.proformas.dominio.producto.AtributosFisicos;
import com.univ2026.proformas.dominio.producto.Producto;
import com.univ2026.proformas.dominio.producto.Talla;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CatalogosSQLiteTest {
    @TempDir
    Path temporal;

    @Test
    void persisteProductosFisicosYDigitalesAlReabrir() {
        Path base = temporal.resolve("productos.db");
        CatalogoProductosSQLite catalogo = new CatalogoProductosSQLite(base);
        catalogo.registrar(new Producto(
                "F-001",
                "Faja lumbar",
                "Soporte clinico",
                85.5,
                15,
                Estado.ACTIVO,
                new AtributosFisicos(0.4, Talla.M)));
        catalogo.registrar(
                new Producto("D-001", "Manual digital", "PDF", 10, 0, Estado.ACTIVO, new AtributosDigitales(12.5)));

        CatalogoProductosSQLite reabierto = new CatalogoProductosSQLite(base);
        Producto fisico = reabierto.buscarPorCodigo("F-001");
        Producto digital = reabierto.buscarPorCodigo("D-001");

        AtributosFisicos atributosFisicos = assertInstanceOf(AtributosFisicos.class, fisico.getExtras());
        assertEquals(0.4, atributosFisicos.pesoKg());
        assertEquals(Talla.M, atributosFisicos.talla());
        assertEquals(
                12.5,
                assertInstanceOf(AtributosDigitales.class, digital.getExtras()).tamanioMb());
        assertEquals("85.50", fisico.getPrecio().toString());
    }

    @Test
    void rechazaCodigosDuplicadosYRetornaResultadosVacios() {
        CatalogoProductosSQLite catalogo = catalogoProductos();
        catalogo.registrar(new Producto(" p-001 ", "Teclado"));

        assertThrows(IllegalArgumentException.class, () -> catalogo.registrar(new Producto("p-001", "Otro")));
        assertEquals("Teclado", catalogo.buscarPorCodigo(" p-001 ").getNombre());
        assertNull(catalogo.buscarPorCodigo("P-999"));
        assertTrue(catalogo.buscar("inexistente").isEmpty());
    }

    @Test
    void buscaProductosSinDistinguirMayusculasYPersisteLaBaja() {
        Path base = temporal.resolve("busqueda-productos.db");
        CatalogoProductosSQLite catalogo = new CatalogoProductosSQLite(base);
        catalogo.registrar(new Producto("MED-001", "Faja Lumbar", "Soporte Clínico", 50, 15, Estado.ACTIVO, null));

        assertEquals("MED-001", catalogo.buscar("LUMBAR").get(0).getCodigo());
        assertEquals("MED-001", catalogo.buscar("clínico").get(0).getCodigo());
        catalogo.cambiarEstado("MED-001", Estado.INACTIVO);

        assertEquals(
                Estado.INACTIVO,
                new CatalogoProductosSQLite(base).buscarPorCodigo("MED-001").getEstado());
    }

    @Test
    void derivaListMapYSetDesdeSQLite() {
        CatalogoProductosSQLite catalogo = catalogoProductos();
        catalogo.registrar(new Producto("P-002", "Mouse"));
        catalogo.registrar(new Producto("P-001", "Teclado"));

        List<Producto> lista = catalogo.listar();
        Map<String, Producto> mapa = catalogo.indexarPorCodigo();
        Set<String> conjunto = catalogo.listarCodigos();

        assertEquals(
                List.of("P-001", "P-002"),
                lista.stream().map(Producto::getCodigo).toList());
        assertEquals("Mouse", mapa.get("P-002").getNombre());
        assertEquals(Set.of("P-001", "P-002"), conjunto);
        assertThrows(UnsupportedOperationException.class, () -> lista.clear());
        assertThrows(UnsupportedOperationException.class, () -> mapa.clear());
        assertThrows(UnsupportedOperationException.class, () -> conjunto.clear());
    }

    @Test
    void persisteClientesCompletosBuscaYRechazaDuplicados() {
        Path base = temporal.resolve("clientes.db");
        RegistroClientesSQLite registro = new RegistroClientesSQLite(base);
        Cliente cliente = new Cliente(
                "1790012345001",
                "Clínica Central",
                "Av. Loja 123",
                "0991234567",
                "ventas@clinica.ec",
                TipoCliente.MAYORISTA,
                Estado.ACTIVO);
        registro.registrar(cliente);

        RegistroClientesSQLite reabierto = new RegistroClientesSQLite(base);
        Cliente recuperado = reabierto.buscarPorIdentificacion("1790012345001");
        assertEquals("Av. Loja 123", recuperado.getDireccion());
        assertEquals("0991234567", recuperado.getTelefono());
        assertEquals(TipoCliente.MAYORISTA, recuperado.getTipo());
        assertEquals(
                "1790012345001",
                reabierto.buscar("CENTRAL").get(0).getIdentificacion().valor());
        assertThrows(IllegalArgumentException.class, () -> reabierto.registrar(cliente));
    }

    @Test
    void derivaColeccionesDeClientesYPersisteSuBaja() {
        Path base = temporal.resolve("colecciones-clientes.db");
        RegistroClientesSQLite registro = new RegistroClientesSQLite(base);
        registro.registrar(new Cliente("1100001234", "Ana"));

        assertEquals(1, registro.listar().size());
        assertEquals(
                "Ana", registro.indexarPorIdentificacion().get("1100001234").getNombre());
        assertEquals(Set.of("1100001234"), registro.listarIdentificaciones());
        assertTrue(registro.buscar("nadie").isEmpty());
        registro.cambiarEstado("1100001234", Estado.INACTIVO);
        assertEquals(
                Estado.INACTIVO,
                new RegistroClientesSQLite(base)
                        .buscarPorIdentificacion("1100001234")
                        .getEstado());
    }

    @Test
    void buscaIdentificacionPorPrefijoYNombrePorFragmentoSinElegirImplicitamente() {
        RegistroClientesSQLite registro = new RegistroClientesSQLite(temporal.resolve("busqueda-clientes.db"));
        registro.registrar(new Cliente("1104747629001", "Clinica Norte"));
        registro.registrar(new Cliente("1104747629002", "Clinica Sur"));
        registro.registrar(new Cliente("1790012345001", "Otra Clinica"));

        assertEquals(2, registro.buscar("1104747629").size());
        assertEquals(3, registro.buscar("CLINICA").size());
        assertTrue(registro.buscar("4747629").isEmpty());
        assertTrue(registro.buscar("%").isEmpty());
    }

    @Test
    void creaLasTablasProductosYClientes() throws SQLException {
        Path base = temporal.resolve("esquema.db");
        new CatalogoProductosSQLite(base);

        try (var conexion = DriverManager.getConnection("jdbc:sqlite:" + base.toAbsolutePath());
                var sentencia = conexion.prepareStatement(
                        "SELECT name FROM sqlite_master WHERE type = 'table' AND name IN (?, ?) ORDER BY name")) {
            sentencia.setString(1, "productos");
            sentencia.setString(2, "clientes");
            try (ResultSet filas = sentencia.executeQuery()) {
                assertTrue(filas.next());
                assertEquals("clientes", filas.getString("name"));
                assertTrue(filas.next());
                assertEquals("productos", filas.getString("name"));
            }
        }
    }

    private CatalogoProductosSQLite catalogoProductos() {
        return new CatalogoProductosSQLite(temporal.resolve("catalogo.db"));
    }
}
