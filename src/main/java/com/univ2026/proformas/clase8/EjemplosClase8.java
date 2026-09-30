package com.univ2026.proformas.clase8;

import java.util.*;

/**
 * Clase 8 - Tipos de Datos No Lineales en Java
 * Implementaciones de: Árbol Binario de Búsqueda, Grafo (lista de adyacencia)
 * y Heap Mínimo, aplicados al dominio de Productos.
 */

// =============================================================================
// DOMINIO: Clase Producto (encapsulada y validada)
// =============================================================================
class Producto {
    private String nombre;
    private double precio;
    private int stock;

    /**
     * Constructor con validaciones.
     * @param nombre Nombre del producto (no vacío)
     * @param precio Precio del producto (mayor a 0)
     * @param stock  Stock disponible (mayor o igual a 0)
     */
    public Producto(String nombre, double precio, int stock) {
        setNombre(nombre);
        setPrecio(precio);
        setStock(stock);
    }

    public String getNombre() { return nombre; }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del producto no puede estar vacío.");
        }
        this.nombre = nombre.trim();
    }

    public double getPrecio() { return precio; }

    public void setPrecio(double precio) {
        if (precio <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor a 0.");
        }
        this.precio = precio;
    }

    public int getStock() { return stock; }

    public void setStock(int stock) {
        if (stock < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo.");
        }
        this.stock = stock;
    }

    @Override
    public String toString() {
        return String.format("Producto{nombre='%s', precio=%.2f, stock=%d}", nombre, precio, stock);
    }
}

// =============================================================================
// TDA A: Árbol Binario de Búsqueda de Productos (ordenado por precio)
// =============================================================================
class ArbolProductos {

    /** Nodo interno del árbol binario. */
    private static class NodoArbol {
        Producto producto;
        NodoArbol izquierdo;
        NodoArbol derecho;

        NodoArbol(Producto producto) {
            this.producto = producto;
            this.izquierdo = null;
            this.derecho = null;
        }
    }

    private NodoArbol raiz;

    public ArbolProductos() {
        this.raiz = null;
    }

    /**
     * Inserta un producto en el árbol, ordenado por precio.
     */
    public void insertar(Producto producto) {
        raiz = insertarRecursivo(raiz, producto);
    }

    /**
     * Método recursivo auxiliar para insertar.
     * Si el precio es menor, va a la izquierda; si es mayor o igual, a la derecha.
     */
    private NodoArbol insertarRecursivo(NodoArbol nodo, Producto producto) {
        if (nodo == null) {
            return new NodoArbol(producto); // Caso base: posición encontrada
        }
        if (producto.getPrecio() < nodo.producto.getPrecio()) {
            nodo.izquierdo = insertarRecursivo(nodo.izquierdo, producto);
        } else {
            nodo.derecho = insertarRecursivo(nodo.derecho, producto);
        }
        return nodo;
    }

    /**
     * Verifica si existe algún producto con el precio indicado.
     * @return true si se encontró un producto con ese precio exacto.
     */
    public boolean contienePrecio(double precio) {
        return buscarRecursivo(raiz, precio);
    }

    /**
     * Búsqueda recursiva por precio en el árbol.
     */
    private boolean buscarRecursivo(NodoArbol nodo, double precio) {
        if (nodo == null) {
            return false; // Caso base: no encontrado
        }
        if (Double.compare(precio, nodo.producto.getPrecio()) == 0) {
            return true; // Precio encontrado
        } else if (precio < nodo.producto.getPrecio()) {
            return buscarRecursivo(nodo.izquierdo, precio);
        } else {
            return buscarRecursivo(nodo.derecho, precio);
        }
    }

    /**
     * Cuenta el total de productos en el árbol mediante recorrido recursivo.
     * @return Número total de nodos en el árbol.
     */
    public int contarProductos() {
        return contarRecursivo(raiz);
    }

    /**
     * Cuenta nodos recursivamente: 1 (nodo actual) + izquierda + derecha.
     */
    private int contarRecursivo(NodoArbol nodo) {
        if (nodo == null) {
            return 0; // Caso base: árbol vacío o subárbol nulo
        }
        return 1 + contarRecursivo(nodo.izquierdo) + contarRecursivo(nodo.derecho);
    }

    /**
     * Imprime todos los productos ordenados de menor a mayor precio.
     * El recorrido in-orden en un BST produce los elementos en orden ascendente.
     */
    public void recorrerInOrden() {
        System.out.println("  [Árbol - Recorrido In-Orden (precio ascendente)]:");
        if (raiz == null) {
            System.out.println("  El árbol está vacío.");
            return;
        }
        inOrdenRecursivo(raiz);
    }

    private void inOrdenRecursivo(NodoArbol nodo) {
        if (nodo == null) {
            return; // Caso base
        }
        inOrdenRecursivo(nodo.izquierdo);           // Visitar izquierda
        System.out.println("  -> " + nodo.producto); // Visitar nodo actual
        inOrdenRecursivo(nodo.derecho);              // Visitar derecha
    }
}

// =============================================================================
// TDA B: Grafo de Categorías (lista de adyacencia, no dirigido)
// =============================================================================
class GrafoCategorias {

    /** Representación: mapa de categoría -> lista de categorías relacionadas. */
    private Map<String, List<String>> listaAdyacencia;

    public GrafoCategorias() {
        this.listaAdyacencia = new HashMap<>();
    }

    /**
     * Agrega una nueva categoría al grafo (sin relaciones iniciales).
     * Si ya existe, no hace nada.
     */
    public void agregarCategoria(String categoria) {
        if (categoria == null || categoria.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la categoría no puede estar vacío.");
        }
        listaAdyacencia.putIfAbsent(categoria, new ArrayList<>());
    }

    /**
     * Crea una relación bidireccional (simétrica) entre dos categorías.
     * Ambas categorías deben existir previamente en el grafo.
     */
    public void relacionar(String categoriaA, String categoriaB) {
        if (!existeCategoria(categoriaA)) {
            throw new IllegalArgumentException("Categoría no encontrada: " + categoriaA);
        }
        if (!existeCategoria(categoriaB)) {
            throw new IllegalArgumentException("Categoría no encontrada: " + categoriaB);
        }
        // Agregar arista en ambas direcciones (grafo no dirigido)
        listaAdyacencia.get(categoriaA).add(categoriaB);
        listaAdyacencia.get(categoriaB).add(categoriaA);
    }

    /**
     * Retorna la lista de categorías directamente relacionadas con la indicada.
     * @return Lista de categorías adyacentes, o lista vacía si no tiene relaciones.
     */
    public List<String> obtenerRelacionadas(String categoria) {
        if (!existeCategoria(categoria)) {
            System.out.println("  Advertencia: Categoría '" + categoria + "' no encontrada.");
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(listaAdyacencia.get(categoria));
    }

    /**
     * Verifica si una categoría existe en el grafo.
     */
    public boolean existeCategoria(String categoria) {
        return listaAdyacencia.containsKey(categoria);
    }
}

// =============================================================================
// TDA C: Heap Mínimo de Reabastecimiento (menor stock = mayor prioridad)
// =============================================================================
class HeapReabastecimiento {

    /** Almacenamiento interno del heap como ArrayList (índice 0 es la raíz). */
    private List<Producto> elementos;

    public HeapReabastecimiento() {
        this.elementos = new ArrayList<>();
    }

    /**
     * Inserta un producto en el heap.
     * Se agrega al final y luego "flota hacia arriba" para mantener la propiedad del heap.
     */
    public void insertar(Producto producto) {
        elementos.add(producto);                    // 1. Agregar al final
        flotarHaciaArriba(elementos.size() - 1);   // 2. Restaurar propiedad del heap
    }

    /**
     * "Flotar hacia arriba": compara el nodo en 'indice' con su padre.
     * Si tiene menor stock (mayor prioridad), intercambia y sube.
     * Continúa hasta llegar a la raíz o no necesitar subir más.
     */
    private void flotarHaciaArriba(int indice) {
        while (indice > 0) {
            int indicePadre = (indice - 1) / 2;
            if (elementos.get(indice).getStock() < elementos.get(indicePadre).getStock()) {
                intercambiar(indice, indicePadre); // Subir el elemento
                indice = indicePadre;              // Continuar desde la nueva posición
            } else {
                break; // La propiedad del heap ya se cumple
            }
        }
    }

    /**
     * Extrae el producto más urgente (menor stock = raíz del heap).
     * Mueve el último elemento a la raíz y luego "hunde hacia abajo".
     * @return El producto con menor stock, o null si el heap está vacío.
     */
    public Producto extraerMasUrgente() {
        if (estaVacio()) {
            System.out.println("  Advertencia: El heap está vacío, no hay producto que extraer.");
            return null;
        }
        Producto masUrgente = elementos.get(0);               // Guardar la raíz
        Producto ultimo = elementos.remove(elementos.size() - 1); // Quitar el último
        if (!estaVacio()) {
            elementos.set(0, ultimo);                         // Mover último a la raíz
            hundirHaciaAbajo(0);                              // Restaurar propiedad del heap
        }
        return masUrgente;
    }

    /**
     * "Hundir hacia abajo": compara el nodo en 'indice' con sus hijos.
     * Intercambia con el hijo de menor stock hasta quedar en posición correcta.
     */
    private void hundirHaciaAbajo(int indice) {
        int n = elementos.size();
        while (true) {
            int izquierdo = 2 * indice + 1; // Índice del hijo izquierdo
            int derecho   = 2 * indice + 2; // Índice del hijo derecho
            int menor     = indice;          // Asumimos que el actual es el menor

            // Verificar si el hijo izquierdo tiene menor stock
            if (izquierdo < n && elementos.get(izquierdo).getStock() < elementos.get(menor).getStock()) {
                menor = izquierdo;
            }
            // Verificar si el hijo derecho tiene menor stock que el actual mínimo
            if (derecho < n && elementos.get(derecho).getStock() < elementos.get(menor).getStock()) {
                menor = derecho;
            }

            if (menor != indice) {
                intercambiar(indice, menor); // Bajar el elemento
                indice = menor;              // Continuar desde la nueva posición
            } else {
                break; // El nodo está en su lugar correcto
            }
        }
    }

    /** Intercambia dos elementos en el ArrayList interno. */
    private void intercambiar(int i, int j) {
        Producto temp = elementos.get(i);
        elementos.set(i, elementos.get(j));
        elementos.set(j, temp);
    }

    /**
     * Retorna el producto más urgente sin extraerlo (peek).
     * @return El producto con menor stock, o null si el heap está vacío.
     */
    public Producto verProximoUrgente() {
        if (estaVacio()) {
            return null;
        }
        return elementos.get(0);
    }

    /** @return true si el heap no contiene elementos. */
    public boolean estaVacio() {
        return elementos.isEmpty();
    }
}

// =============================================================================
// DEMO PRINCIPAL
// =============================================================================
public class EjemplosClase8 {

    public static void main(String[] args) {
        System.out.println("============================================================");
        System.out.println("  CLASE 8 - Tipos de Datos No Lineales en Java");
        System.out.println("  Dominio: Gestión de Productos");
        System.out.println("============================================================\n");

        // ------------------------------------------------------------------
        // A. Árbol Binario de Búsqueda - Catálogo ordenado por precio
        // ------------------------------------------------------------------
        System.out.println("--- A. Árbol Binario de Búsqueda (ArbolProductos) ---");

        ArbolProductos catalogo = new ArbolProductos();
        catalogo.insertar(new Producto("Mouse",       15.5,  30));
        catalogo.insertar(new Producto("Monitor",    120.0,   4));
        catalogo.insertar(new Producto("Teclado",     25.0,  12));
        catalogo.insertar(new Producto("Auriculares", 45.0,   8));

        System.out.println("¿Existe precio 25.0?  -> " + catalogo.contienePrecio(25.0));
        System.out.println("¿Existe precio 99.9?  -> " + catalogo.contienePrecio(99.9));
        System.out.println("Total de productos    -> " + catalogo.contarProductos());
        catalogo.recorrerInOrden();

        System.out.println();

        // ------------------------------------------------------------------
        // B. Grafo de Categorías - Lista de adyacencia
        // ------------------------------------------------------------------
        System.out.println("--- B. Grafo de Categorías (GrafoCategorias) ---");

        GrafoCategorias grafo = new GrafoCategorias();
        grafo.agregarCategoria("Electrónica");
        grafo.agregarCategoria("Accesorios de computador");
        grafo.agregarCategoria("Audio");
        grafo.agregarCategoria("Accesorios para el hogar");

        grafo.relacionar("Electrónica", "Accesorios de computador");
        grafo.relacionar("Electrónica", "Audio");
        grafo.relacionar("Audio", "Accesorios para el hogar");

        System.out.println("¿Existe 'Electrónica'?         -> " + grafo.existeCategoria("Electrónica"));
        System.out.println("¿Existe 'Deportes'?            -> " + grafo.existeCategoria("Deportes"));
        System.out.println("Relacionadas con 'Electrónica' -> " + grafo.obtenerRelacionadas("Electrónica"));
        System.out.println("Relacionadas con 'Audio'       -> " + grafo.obtenerRelacionadas("Audio"));
        System.out.println("Relacionadas con 'Deportes'    -> " + grafo.obtenerRelacionadas("Deportes"));

        System.out.println();

        // ------------------------------------------------------------------
        // C. Heap Mínimo - Prioridad de reabastecimiento (menor stock primero)
        // ------------------------------------------------------------------
        System.out.println("--- C. Heap Mínimo de Reabastecimiento (HeapReabastecimiento) ---");

        HeapReabastecimiento heap = new HeapReabastecimiento();
        heap.insertar(new Producto("Mouse",       15.5, 30));
        heap.insertar(new Producto("Monitor",    120.0,  4)); // más urgente
        heap.insertar(new Producto("Teclado",     25.0, 12));
        heap.insertar(new Producto("Auriculares", 45.0,  8)); // 2do más urgente

        System.out.println("Próximo a reabastecer: " + heap.verProximoUrgente());
        System.out.println("Extrayendo productos en orden de urgencia:");
        while (!heap.estaVacio()) {
            System.out.println("  Reabastecer -> " + heap.extraerMasUrgente());
        }

        // Caso borde: extraer de heap vacío
        System.out.println("Intento de extracción con heap vacío:");
        heap.extraerMasUrgente();

        System.out.println("\n============================================================");
        System.out.println("  Fin de la demostración - Clase 8");
        System.out.println("============================================================");
    }
}
