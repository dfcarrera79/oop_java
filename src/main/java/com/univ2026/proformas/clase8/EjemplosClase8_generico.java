package com.univ2026.proformas.clase8;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// ---------------------------------------------------------------
// 1. ARBOL BINARIO
// ---------------------------------------------------------------
class Nodo {
    int dato;
    Nodo izquierdo;
    Nodo derecho;

    public Nodo(int dato, Nodo izquierdo, Nodo derecho) {
        this.dato = dato;
        this.izquierdo = izquierdo;
        this.derecho = derecho;
    }

    public int show() {
        return this.dato;
    }
}

class Arbol {
    Nodo raiz;

    public void insertar(int dato) {
        raiz = insertarRecursivo(raiz, dato);
    }

    private Nodo insertarRecursivo(Nodo nodo, int dato) {
        if (nodo == null) {
            return new Nodo(dato, null, null);
        }
        if (dato < nodo.dato) {
            nodo.izquierdo = insertarRecursivo(nodo.izquierdo, dato);
        } else if (dato > nodo.dato) {
            nodo.derecho = insertarRecursivo(nodo.derecho, dato);
        }
        return nodo;
    }
}

// ---------------------------------------------------------------
// 2. GRAFO (lista de adyacencia)
// ---------------------------------------------------------------
class Grafo {
    private Map<Integer, List<Integer>> adyacencias = new HashMap<>();

    public void agregarNodo(int nodo) {
        adyacencias.putIfAbsent(nodo, new ArrayList<>());
    }

    public void relacionar(int nodoA, int nodoB) {
        adyacencias.get(nodoA).add(nodoB);
        adyacencias.get(nodoB).add(nodoA);
    }

    public List<Integer> obtenerVecinos(int nodo) {
        return adyacencias.getOrDefault(nodo, new ArrayList<>());
    }
}

// ---------------------------------------------------------------
// 3. HEAP MINIMO
// ---------------------------------------------------------------
class Heap {
    private List<Integer> elementos = new ArrayList<>();

    public void insertar(int valor) {
        elementos.add(valor);
        int actual = elementos.size() - 1;

        while (actual > 0) {
            int padre = (actual - 1) / 2;
            if (elementos.get(actual) < elementos.get(padre)) {
                intercambiar(actual, padre);
                actual = padre;
            } else {
                break;
            }
        }
    }

    public int extraerMinimo() {
        int raiz = elementos.get(0);
        int ultimo = elementos.remove(elementos.size() - 1);

        if (!elementos.isEmpty()) {
            elementos.set(0, ultimo);
            hundir(0);
        }
        return raiz;
    }

    private void hundir(int indice) {
        int menor = indice;
        int izquierdo = 2 * indice + 1;
        int derecho = 2 * indice + 2;

        if (izquierdo < elementos.size()
                && elementos.get(izquierdo) < elementos.get(menor)) {
            menor = izquierdo;
        }
        if (derecho < elementos.size()
                && elementos.get(derecho) < elementos.get(menor)) {
            menor = derecho;
        }
        if (menor != indice) {
            intercambiar(indice, menor);
            hundir(menor);
        }
    }

    private void intercambiar(int i, int j) {
        int temporal = elementos.get(i);
        elementos.set(i, elementos.get(j));
        elementos.set(j, temporal);
    }
}

// ---------------------------------------------------------------
// 4. HEAP MAXIMO (solo cambian las comparaciones)
// ---------------------------------------------------------------
class HeapMax {
    private List<Integer> elementos = new ArrayList<>();

    public void insertar(int valor) {
        elementos.add(valor);
        int actual = elementos.size() - 1;

        while (actual > 0) {
            int padre = (actual - 1) / 2;
            if (elementos.get(actual) > elementos.get(padre)) {
                intercambiar(actual, padre);
                actual = padre;
            } else {
                break;
            }
        }
    }

    public int extraerMaximo() {
        int raiz = elementos.get(0);
        int ultimo = elementos.remove(elementos.size() - 1);

        if (!elementos.isEmpty()) {
            elementos.set(0, ultimo);
            hundir(0);
        }
        return raiz;
    }

    private void hundir(int indice) {
        int mayor = indice;
        int izquierdo = 2 * indice + 1;
        int derecho = 2 * indice + 2;

        if (izquierdo < elementos.size()
                && elementos.get(izquierdo) > elementos.get(mayor)) {
            mayor = izquierdo;
        }
        if (derecho < elementos.size()
                && elementos.get(derecho) > elementos.get(mayor)) {
            mayor = derecho;
        }
        if (mayor != indice) {
            intercambiar(indice, mayor);
            hundir(mayor);
        }
    }

    private void intercambiar(int i, int j) {
        int temporal = elementos.get(i);
        elementos.set(i, elementos.get(j));
        elementos.set(j, temporal);
    }
}

// ---------------------------------------------------------------
// PRUEBAS
// ---------------------------------------------------------------
public class EjemplosClase8_generico {
    public static void main(String[] args) {
        System.out.println("=== Arbol binario ===");
        Arbol arbol = new Arbol();
        for (int n : new int[] { 56, 22, 81, 10, 30, 77, 92 }) {
            arbol.insertar(n);
        }
        System.out.println("Raiz: " + arbol.raiz.show());
        System.out.println("Hijo izquierdo: " + arbol.raiz.izquierdo.show());
        System.out.println("Hijo derecho: " + arbol.raiz.derecho.show());

        System.out.println("\n=== Grafo ===");
        Grafo grafo = new Grafo();
        for (int n : new int[] { 1, 2, 3, 4 }) {
            grafo.agregarNodo(n);
        }
        grafo.relacionar(1, 2);
        grafo.relacionar(1, 3);
        grafo.relacionar(3, 4);
        System.out.println("Vecinos de 1: " + grafo.obtenerVecinos(1));
        System.out.println("Vecinos de 3: " + grafo.obtenerVecinos(3));
        System.out.println("Vecinos de 99 (no existe): " + grafo.obtenerVecinos(99));

        System.out.println("\n=== Heap minimo ===");
        Heap heap = new Heap();
        for (int n : new int[] { 8, 3, 5, 1 }) {
            heap.insertar(n);
        }
        System.out.print("Extraccion:");
        for (int i = 0; i < 4; i++) {
            System.out.print(" " + heap.extraerMinimo());
        }
        System.out.println();

        System.out.println("\n=== Heap maximo ===");
        HeapMax heapMax = new HeapMax();
        for (int n : new int[] { 8, 3, 5, 1 }) {
            heapMax.insertar(n);
        }
        System.out.print("Extraccion:");
        for (int i = 0; i < 4; i++) {
            System.out.print(" " + heapMax.extraerMaximo());
        }
        System.out.println();
    }
}
