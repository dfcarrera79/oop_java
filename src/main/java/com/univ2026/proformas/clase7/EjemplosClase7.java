package com.univ2026.proformas.clase7;

import com.univ2026.proformas.dominio.producto.Producto;

/** Ejemplos autocontenidos de TDA lineales y del patron Repository. */
public final class EjemplosClase7 {
    private EjemplosClase7() {}

    /** Pila de capacidad fija implementada manualmente sobre un arreglo. */
    public static final class PilaProductos {
        private final Producto[] elementos;
        private int cantidad;

        public PilaProductos(int capacidad) {
            validarCapacidad(capacidad);
            elementos = new Producto[capacidad];
        }

        public void apilar(Producto producto) {
            validarProducto(producto);
            if (cantidad == elementos.length) {
                throw new IllegalStateException("La pila esta llena");
            }
            elementos[cantidad] = producto;
            cantidad++;
        }

        public Producto desapilar() {
            if (estaVacia()) {
                throw new IllegalStateException("La pila esta vacia");
            }
            cantidad--;
            Producto tope = elementos[cantidad];
            elementos[cantidad] = null;
            return tope;
        }

        public boolean estaVacia() {
            return cantidad == 0;
        }

        public int getCantidad() {
            return cantidad;
        }
    }

    /** Cola circular de capacidad fija implementada manualmente sobre un arreglo. */
    public static final class ColaProductos {
        private final Producto[] elementos;
        private int frente;
        private int cantidad;

        public ColaProductos(int capacidad) {
            validarCapacidad(capacidad);
            elementos = new Producto[capacidad];
        }

        public void encolar(Producto producto) {
            validarProducto(producto);
            if (cantidad == elementos.length) {
                throw new IllegalStateException("La cola esta llena");
            }
            int posicionFinal = (frente + cantidad) % elementos.length;
            elementos[posicionFinal] = producto;
            cantidad++;
        }

        public Producto desencolar() {
            if (estaVacia()) {
                throw new IllegalStateException("La cola esta vacia");
            }
            Producto primero = elementos[frente];
            elementos[frente] = null;
            frente = (frente + 1) % elementos.length;
            cantidad--;
            return primero;
        }

        public boolean estaVacia() {
            return cantidad == 0;
        }

        public int getCantidad() {
            return cantidad;
        }
    }

    /** Oculta la pila y ofrece operaciones expresadas en el lenguaje del dominio. */
    public static final class RepositorioProductos {
        private final PilaProductos pila;

        public RepositorioProductos(int capacidad) {
            pila = new PilaProductos(capacidad);
        }

        public void agregar(Producto producto) {
            pila.apilar(producto);
        }

        public Producto retirarUltimo() {
            return pila.desapilar();
        }

        public boolean estaVacio() {
            return pila.estaVacia();
        }

        public int cantidad() {
            return pila.getCantidad();
        }
    }

    /** Permite observar en consola la diferencia entre LIFO y FIFO. */
    public static void main(String[] args) {
        Producto mouse = new Producto("P-001", "Mouse");
        Producto teclado = new Producto("P-002", "Teclado");

        PilaProductos pila = new PilaProductos(2);
        pila.apilar(mouse);
        pila.apilar(teclado);
        System.out.println("Pila (LIFO): " + pila.desapilar().getNombre());

        ColaProductos cola = new ColaProductos(2);
        cola.encolar(mouse);
        cola.encolar(teclado);
        System.out.println("Cola (FIFO): " + cola.desencolar().getNombre());

        RepositorioProductos repositorio = new RepositorioProductos(2);
        repositorio.agregar(mouse);
        System.out.println("Repository: " + repositorio.retirarUltimo().getNombre());
    }

    private static void validarCapacidad(int capacidad) {
        if (capacidad <= 0) {
            throw new IllegalArgumentException("La capacidad debe ser mayor que cero");
        }
    }

    private static void validarProducto(Producto producto) {
        if (producto == null) {
            throw new IllegalArgumentException("El producto no puede ser null");
        }
    }
}
