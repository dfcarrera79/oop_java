package com.univ2026.proformas.clase7;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.univ2026.proformas.clase7.EjemplosClase7.ColaProductos;
import com.univ2026.proformas.clase7.EjemplosClase7.PilaProductos;
import com.univ2026.proformas.clase7.EjemplosClase7.RepositorioProductos;
import com.univ2026.proformas.dominio.producto.Producto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EjemplosClase7Test {
    private Producto mouse;
    private Producto teclado;

    @BeforeEach
    void configurar() {
        mouse = new Producto("P-001", "Mouse");
        teclado = new Producto("P-002", "Teclado");
    }

    @Test
    void pilaNuevaEstaVacia() {
        PilaProductos pila = new PilaProductos(2);

        assertTrue(pila.estaVacia());
        assertEquals(0, pila.getCantidad());
    }

    @Test
    void pilaRespetaOrdenLifo() {
        PilaProductos pila = new PilaProductos(2);
        pila.apilar(mouse);
        pila.apilar(teclado);

        assertSame(teclado, pila.desapilar());
        assertSame(mouse, pila.desapilar());
        assertTrue(pila.estaVacia());
    }

    @Test
    void pilaInformaDesbordamientoYSubdesbordamiento() {
        PilaProductos pila = new PilaProductos(1);
        pila.apilar(mouse);

        assertThrows(IllegalStateException.class, () -> pila.apilar(teclado));
        pila.desapilar();
        assertThrows(IllegalStateException.class, pila::desapilar);
    }

    @Test
    void colaRespetaOrdenFifoInclusoAlDarLaVuelta() {
        ColaProductos cola = new ColaProductos(2);
        cola.encolar(mouse);
        cola.encolar(teclado);

        assertSame(mouse, cola.desencolar());
        Producto monitor = new Producto("P-003", "Monitor");
        cola.encolar(monitor);

        assertSame(teclado, cola.desencolar());
        assertSame(monitor, cola.desencolar());
        assertTrue(cola.estaVacia());
    }

    @Test
    void colaInformaDesbordamientoYSubdesbordamiento() {
        ColaProductos cola = new ColaProductos(1);
        cola.encolar(mouse);

        assertThrows(IllegalStateException.class, () -> cola.encolar(teclado));
        cola.desencolar();
        assertThrows(IllegalStateException.class, cola::desencolar);
    }

    @Test
    void repositorioAgregaYRetiraElProductoMasReciente() {
        RepositorioProductos repositorio = new RepositorioProductos(2);
        repositorio.agregar(mouse);
        repositorio.agregar(teclado);

        assertEquals(2, repositorio.cantidad());
        assertSame(teclado, repositorio.retirarUltimo());
        assertSame(mouse, repositorio.retirarUltimo());
        assertTrue(repositorio.estaVacio());
    }

    @Test
    void estructurasRechazanCapacidadInvalidaYProductosNulos() {
        assertThrows(IllegalArgumentException.class, () -> new PilaProductos(0));
        assertThrows(IllegalArgumentException.class, () -> new ColaProductos(-1));

        PilaProductos pila = new PilaProductos(1);
        ColaProductos cola = new ColaProductos(1);
        assertThrows(IllegalArgumentException.class, () -> pila.apilar(null));
        assertThrows(IllegalArgumentException.class, () -> cola.encolar(null));
    }
}
