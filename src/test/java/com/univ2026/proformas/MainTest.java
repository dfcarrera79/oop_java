package com.univ2026.proformas;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.univ2026.proformas.aplicacion.AplicacionProformas;
import com.univ2026.proformas.persistencia.sqlite.CatalogoProductosSQLite;
import com.univ2026.proformas.persistencia.sqlite.RegistroClientesSQLite;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MainTest {
    @TempDir
    Path temporal;

    @Test
    void cargaDemoDosVecesSinDuplicarDatos() {
        Path base = temporal.resolve("demo.db");
        CatalogoProductosSQLite productos = new CatalogoProductosSQLite(base);
        RegistroClientesSQLite clientes = new RegistroClientesSQLite(base);
        AplicacionProformas aplicacion = new AplicacionProformas(productos, clientes);

        Main.cargarDatosDemo(aplicacion, productos, clientes);
        Main.cargarDatosDemo(aplicacion, productos, clientes);

        assertEquals(1, productos.listar().size());
        assertEquals(1, clientes.listar().size());
    }
}
