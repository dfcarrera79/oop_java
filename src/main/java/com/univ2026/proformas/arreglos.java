package com.univ2026.proformas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Clase 5: Arreglos vs Colecciones y Genericos en Java.
 * Archivo didactico para demostrar los conceptos clave de la clase:
 * - Arreglos de tamano fijo vs ArrayList dinamico.
 * - List (ArrayList): orden y duplicados permitidos.
 * - Map (HashMap): acceso rapido por clave unica.
 * - Set (HashSet): coleccion de elementos unicos (sin duplicados).
 * - Genericos: seguridad de tipos en tiempo de compilacion y clase
 * parametrizada.
 * - Catalogo integrado combinando List, Set y Map.
 * - Ejercicio de registro de estudiantes.
 */
public class arreglos {

    public static void main(String[] args) {
        demostrarArregloVsColeccion();
        demostrarArrayList();
        demostrarHashMap();
        demostrarHashSet();
        demostrarGenericos();
        demostrarCatalogoIntegrado();
        demostrarRegistroEstudiantes();
    }

    // -------------------------------------------------------------------------
    // 1. Arreglos fijos vs Colecciones
    // -------------------------------------------------------------------------
    private static void demostrarArregloVsColeccion() {
        System.out.println("--- 1. ARREGLO DE TAMANO FIJO VS LISTA DINAMICA ---");

        // Arreglo tradicional: tamano inmutable una vez creado
        String[] arregloEstatico = new String[2];
        arregloEstatico[0] = "Mouse";
        arregloEstatico[1] = "Teclado";
        System.out.println("Arreglo fijo (longitud " + arregloEstatico.length + "): [" + arregloEstatico[0] + ", "
                + arregloEstatico[1] + "]");
        System.out.println("Problema: no podemos agregar un 3er producto directamente sin recrear el arreglo.");

        // ArrayList: crece dinamicamente segun la necesidad
        List<String> listaDinamica = new ArrayList<>();
        listaDinamica.add("Mouse");
        listaDinamica.add("Teclado");
        listaDinamica.add("Monitor");
        listaDinamica.add("Escritorio");
        listaDinamica.add("Silla");
        System.out.println("ArrayList dinamico (size " + listaDinamica.size() + "): " + listaDinamica + "\n");
    }

    // -------------------------------------------------------------------------
    // 2. List / ArrayList
    // -------------------------------------------------------------------------
    private static void demostrarArrayList() {
        System.out.println("--- 2. LISTAS: ArrayList<ProductoSimple> ---");
        List<ProductoSimple> catalogo = new ArrayList<>();

        // Agregar elementos (al final, conservando orden)
        catalogo.add(new ProductoSimple("P001", "Mouse inalambrico", 15.5));
        catalogo.add(new ProductoSimple("P002", "Teclado mecanico", 45.0));
        catalogo.add(new ProductoSimple("P003", "Monitor 24 pulgadas", 180.0));
        catalogo.add(new ProductoSimple("P001", "Mouse inalambrico (duplicado)", 15.5)); // Lista permite duplicados

        // Acceso por indice
        ProductoSimple primero = catalogo.get(0);
        System.out.println("Primer producto (indice 0): " + primero.getNombre());

        // Recorrido con bucle for-each
        System.out.println("Recorrido de la lista:");
        for (ProductoSimple p : catalogo) {
            System.out.println("  - " + p.getNombre() + ": $" + p.getPrecio());
        }

        // Eliminar por posicion
        catalogo.remove(1); // Elimina 'Teclado mecanico'
        System.out.println("Tamano despues de eliminar posicion 1: " + catalogo.size() + "\n");
    }

    // -------------------------------------------------------------------------
    // 3. Map / HashMap
    // -------------------------------------------------------------------------
    private static void demostrarHashMap() {
        System.out.println("--- 3. MAPAS: HashMap<String, ProductoSimple> ---");
        Map<String, ProductoSimple> inventarioPorCodigo = new HashMap<>();

        // put(clave, valor): asocia clave unica con valor
        inventarioPorCodigo.put("P001", new ProductoSimple("P001", "Mouse inalambrico", 15.5));
        inventarioPorCodigo.put("P002", new ProductoSimple("P002", "Teclado mecanico", 45.0));

        // Acceso directo por clave (O(1) promedio)
        ProductoSimple mouse = inventarioPorCodigo.get("P001");
        System.out.println("Producto encontrado por clave 'P001': " + mouse.getNombre());

        // Verificacion con containsKey()
        String codigoBuscado = "P003";
        if (inventarioPorCodigo.containsKey(codigoBuscado)) {
            System.out.println("Existe " + codigoBuscado);
        } else {
            System.out.println("Clave '" + codigoBuscado + "' no esta en el mapa (get devuelve: "
                    + inventarioPorCodigo.get(codigoBuscado) + ")");
        }

        // Recorrido de pares clave-valor
        System.out.println("Entradas en el mapa (clave -> valor):");
        for (Map.Entry<String, ProductoSimple> entrada : inventarioPorCodigo.entrySet()) {
            System.out.println(
                    "  " + entrada.getKey() + " -> " + entrada.getValue().getNombre());
        }
        System.out.println();
    }

    // -------------------------------------------------------------------------
    // 4. Set / HashSet
    // -------------------------------------------------------------------------
    private static void demostrarHashSet() {
        System.out.println("--- 4. CONJUNTOS: HashSet<String> ---");
        Set<String> categorias = new HashSet<>();

        boolean agregado1 = categorias.add("Electronica");
        boolean agregado2 = categorias.add("Hogar");
        boolean agregadoDuplicado = categorias.add("Electronica"); // Se ignora silenciosamente

        System.out.println("Agrego 'Electronica': " + agregado1);
        System.out.println("Agrego 'Hogar': " + agregado2);
        System.out.println("Intento duplicar 'Electronica': " + agregadoDuplicado);
        System.out.println("Cantidad de categorias unicas: " + categorias.size());

        if (categorias.contains("Hogar")) {
            System.out.println("La categoria 'Hogar' existe en el conjunto");
        }

        System.out.println("Categorias registradas:");
        for (String cat : categorias) {
            System.out.println("  * " + cat);
        }
        System.out.println();
    }

    // -------------------------------------------------------------------------
    // 5. Genericos (<T>)
    // -------------------------------------------------------------------------
    private static void demostrarGenericos() {
        System.out.println("--- 5. GENERICOS (<T>) Y SEGURIDAD DE TIPOS ---");

        // Con genericos: el compilador garantiza que solo haya Strings
        List<String> nombres = new ArrayList<>();
        nombres.add("Laptop");
        // nombres.add(100); // <- Error de compilacion inmediato si se descomenta

        String item = nombres.get(0); // Sin necesidad de casting manual (String)
        System.out.println("Elemento recuperado de List<String>: " + item);

        // Uso de nuestra clase generica Contenedor<T>
        Contenedor<ProductoSimple> cajaProducto = new Contenedor<>();
        cajaProducto.guardar(new ProductoSimple("P099", "Impresora Laser", 210.0));
        ProductoSimple prod = cajaProducto.obtener();
        System.out.println("Obtenido de Contenedor<ProductoSimple>: " + prod.getNombre());

        Contenedor<Double> cajaNumero = new Contenedor<>();
        cajaNumero.guardar(99.99);
        System.out.println("Obtenido de Contenedor<Double>: " + cajaNumero.obtener() + "\n");
    }

    // -------------------------------------------------------------------------
    // 6. Ejemplo Integrado: Catalogo con List, Set y Map
    // -------------------------------------------------------------------------
    private static void demostrarCatalogoIntegrado() {
        System.out.println("--- 6. CATALOGO INTEGRADO (List + Set + Map) ---");
        CatalogoDemo catalogo = new CatalogoDemo();

        catalogo.agregarProducto("P001", new ProductoSimple("P001", "Mouse inalambrico", 15.5), "Electronica");
        catalogo.agregarProducto("P002", new ProductoSimple("P002", "Silla de oficina", 120.0), "Mobiliario");
        catalogo.agregarProducto("P003", new ProductoSimple("P003", "Teclado USB", 25.0), "Electronica");

        catalogo.mostrarCatalogoCompleto();

        ProductoSimple buscado = catalogo.buscarPorCodigo("P002");
        System.out.println(
                "Busqueda rapida por codigo 'P002': " + (buscado != null ? buscado.getNombre() : "No encontrado"));
        System.out.println();
    }

    // -------------------------------------------------------------------------
    // 7. Ejercicio Practico: RegistroEstudiantes (Set)
    // -------------------------------------------------------------------------
    private static void demostrarRegistroEstudiantes() {
        System.out.println("--- 7. EJERCICIO: RegistroEstudiantes con Set<String> ---");
        RegistroEstudiantes registro = new RegistroEstudiantes();

        registro.matricular("EST-001");
        registro.matricular("EST-002");
        registro.matricular("EST-001"); // intento duplicado
        System.out.println();
    }

    // =========================================================================
    // Clases auxiliares para la demostracion didactica
    // =========================================================================

    /**
     * Modelo simple de producto para los ejemplos didacticos.
     */
    public static class ProductoSimple {
        private final String codigo;
        private final String nombre;
        private final double precio;

        public ProductoSimple(String codigo, String nombre, double precio) {
            this.codigo = codigo;
            this.nombre = nombre;
            this.precio = precio;
        }

        public String getCodigo() {
            return codigo;
        }

        public String getNombre() {
            return nombre;
        }

        public double getPrecio() {
            return precio;
        }
    }

    /**
     * Ejemplo de clase generica propia con parametro <T>.
     */
    public static class Contenedor<T> {
        private T contenido;

        public void guardar(T contenido) {
            this.contenido = contenido;
        }

        public T obtener() {
            return contenido;
        }
    }

    /**
     * Catalogo que integra las 3 estructuras segun su proposito:
     * - List: conserva el orden de ingreso cronologico.
     * - Set: garantiza categorias unicas sin duplicados.
     * - Map: busqueda directa por codigo O(1).
     */
    public static class CatalogoDemo {
        private final List<ProductoSimple> productos = new ArrayList<>();
        private final Set<String> categorias = new HashSet<>();
        private final Map<String, ProductoSimple> indicePorCodigo = new HashMap<>();

        public void agregarProducto(String codigo, ProductoSimple producto, String categoria) {
            productos.add(producto);
            categorias.add(categoria);
            indicePorCodigo.put(codigo, producto);
        }

        public ProductoSimple buscarPorCodigo(String codigo) {
            return indicePorCodigo.get(codigo);
        }

        public void mostrarCatalogoCompleto() {
            System.out.println("=== Catalogo (" + productos.size() + " productos) ===");
            for (ProductoSimple p : productos) {
                System.out.println("  * [" + p.getCodigo() + "] " + p.getNombre() + ": $" + p.getPrecio());
            }
            System.out.println("Categorias unicas disponibles: " + categorias);
        }
    }

    /**
     * Registro de estudiantes con Set para garantizar que ningun codigo se repita.
     */
    public static class RegistroEstudiantes {
        private final Set<String> codigosMatriculados = new HashSet<>();

        public void matricular(String codigo) {
            boolean esNuevo = codigosMatriculados.add(codigo);
            if (esNuevo) {
                System.out.println("Matricula exitosa para: " + codigo);
            } else {
                System.out.println("Aviso: " + codigo + " ya estaba matriculado");
            }
        }
    }
}
