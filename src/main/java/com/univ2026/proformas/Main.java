package com.univ2026.proformas;

import com.univ2026.proformas.modelo.Cliente;
import com.univ2026.proformas.modelo.Producto;
import com.univ2026.proformas.presentacion.FormateadorProforma;
import com.univ2026.proformas.presentacion.MenuConsola;
import java.util.Scanner;

/** Punto de entrada del sistema de gestion de proformas. */
public final class Main {
    private Main() {}

    /** Inicia el menu o ejecuta una demostracion automatica con --demo. */
    public static void main(String[] args) {
        AplicacionProformas aplicacion = crearAplicacionInicial();
        if (args.length > 0 && "--demo".equals(args[0])) {
            System.out.println(FormateadorProforma.formatearDetalle(aplicacion.crearProformaDemo()));
            return;
        }
        new MenuConsola(aplicacion, new Scanner(System.in), System.out).ejecutar();
    }

    private static AplicacionProformas crearAplicacionInicial() {
        AplicacionProformas aplicacion = new AplicacionProformas();
        aplicacion.registrarProducto(new Producto("P-001", "Teclado", "", 25.0, 12.0, true));
        aplicacion.registrarCliente(new Cliente("1100001234", "Ana", "", "", "ana@correo.com", true));
        return aplicacion;
    }
}
