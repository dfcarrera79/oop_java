package com.univ2026.proformas;

import com.univ2026.proformas.aplicacion.AplicacionProformas;
import com.univ2026.proformas.dominio.Estado;
import com.univ2026.proformas.dominio.cliente.Cliente;
import com.univ2026.proformas.dominio.cliente.TipoCliente;
import com.univ2026.proformas.dominio.producto.AtributosFisicos;
import com.univ2026.proformas.dominio.producto.Producto;
import com.univ2026.proformas.dominio.producto.Talla;
import com.univ2026.proformas.persistencia.sqlite.CatalogoProductosSQLite;
import com.univ2026.proformas.persistencia.sqlite.RegistroClientesSQLite;
import com.univ2026.proformas.presentacion.FormateadorProforma;
import com.univ2026.proformas.presentacion.MenuConsola;
import java.nio.file.Path;
import java.util.Scanner;

/** Punto de entrada del sistema de gestion de proformas. */
public final class Main {
    private Main() {}

    /** Inicia el menu o ejecuta una demostracion automatica con --demo. */
    public static void main(String[] args) {
        Path archivo = Path.of("data", "proformas.db");
        CatalogoProductosSQLite productos = new CatalogoProductosSQLite(archivo);
        RegistroClientesSQLite clientes = new RegistroClientesSQLite(archivo);
        AplicacionProformas aplicacion = new AplicacionProformas(productos, clientes);
        cargarDatosDemo(aplicacion, productos, clientes);
        if (args.length > 0 && "--demo".equals(args[0])) {
            System.out.println(FormateadorProforma.formatearDetalle(aplicacion.crearProformaDemo()));
            return;
        }
        new MenuConsola(aplicacion, new Scanner(System.in), System.out).ejecutar();
    }

    /** Inserta los datos iniciales solo cuando sus claves todavia no existen. */
    static void cargarDatosDemo(
            AplicacionProformas aplicacion, CatalogoProductosSQLite productos, RegistroClientesSQLite clientes) {
        if (productos.buscarPorCodigo("P-0001") == null) {
            aplicacion.registrarProducto(new Producto(
                    "P-0001",
                    "Faja Lumbar",
                    "Soporte lumbar ajustable",
                    85.0,
                    15.0,
                    Estado.ACTIVO,
                    new AtributosFisicos(0.4, Talla.M)));
        }
        if (clientes.buscarPorIdentificacion("1100001234") == null) {
            aplicacion.registrarCliente(new Cliente(
                    "1100001234", "Ana Torres", "", "", "ana.torres@correo.com", TipoCliente.MEDICO, Estado.ACTIVO));
        }
    }
}
