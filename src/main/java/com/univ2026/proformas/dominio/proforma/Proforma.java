package com.univ2026.proformas.dominio.proforma;

import com.univ2026.proformas.dominio.cliente.Cliente;
import com.univ2026.proformas.dominio.valor.Monto;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Documento cotizado con snapshots historicos de cliente e items. */
public class Proforma {
    private String numero;
    private LocalDate fecha;
    private Cliente cliente;
    private String observaciones;
    private String instruccionesPago;
    private final List<ItemProforma> items = new ArrayList<>();

    public Proforma(Cliente cliente, LocalDate fecha, String observaciones, String instruccionesPago) {
        setCliente(cliente);
        setFecha(fecha);
        setObservaciones(observaciones);
        setInstruccionesPago(instruccionesPago);
    }

    /** Constructor conservado para documentos ya numerados. */
    public Proforma(String numero, Cliente cliente) {
        this(cliente, LocalDate.now(), "", "");
        asignarNumero(numero);
    }

    public String getNumero() {
        return numero;
    }

    public void asignarNumero(String numero) {
        if (numero == null || numero.isBlank()) {
            throw new IllegalArgumentException("El numero no puede estar vacio");
        }
        String nuevoNumero = numero.trim();
        if (this.numero != null && !this.numero.equals(nuevoNumero)) {
            throw new IllegalStateException("No se puede cambiar el numero de una proforma");
        }
        this.numero = nuevoNumero;
    }

    public void setNumero(String numero) {
        asignarNumero(numero);
    }

    public Cliente getCliente() {
        return copiarCliente(cliente);
    }

    public void setCliente(Cliente cliente) {
        if (cliente == null) {
            throw new IllegalArgumentException("El cliente no puede ser null");
        }
        this.cliente = copiarCliente(cliente);
    }

    private static Cliente copiarCliente(Cliente cliente) {
        return new Cliente(
                cliente.getIdentificacion(),
                cliente.getNombre(),
                cliente.getDireccion(),
                cliente.getTelefono(),
                cliente.getEmail(),
                cliente.getTipo(),
                cliente.getEstado());
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha no puede ser null");
        }
        this.fecha = fecha;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = textoOpcional(observaciones, "Las observaciones no pueden ser null");
    }

    public String getInstruccionesPago() {
        return instruccionesPago;
    }

    public void setInstruccionesPago(String instruccionesPago) {
        this.instruccionesPago = textoOpcional(instruccionesPago, "Las instrucciones de pago no pueden ser null");
    }

    public List<ItemProforma> getItems() {
        return List.copyOf(items);
    }

    public void agregarItem(ItemProforma item) {
        if (item == null) {
            throw new IllegalArgumentException("El item no puede ser null");
        }
        items.add(item);
    }

    public ItemProforma quitarItem(int indice) {
        return items.remove(indice);
    }

    public BigDecimal calcularSubtotal() {
        return sumar(items.stream().map(ItemProforma::calcularSubtotal).toList());
    }

    public BigDecimal calcularImpuesto() {
        return sumar(items.stream().map(ItemProforma::calcularImpuesto).toList());
    }

    public BigDecimal calcularTotal() {
        return sumar(items.stream().map(ItemProforma::calcularTotal).toList());
    }

    private static BigDecimal sumar(List<BigDecimal> valores) {
        return Monto.normalizar(valores.stream().reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    private static String textoOpcional(String valor, String mensaje) {
        if (valor == null) {
            throw new IllegalArgumentException(mensaje);
        }
        return valor.trim();
    }
}
