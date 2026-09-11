package com.univ2026.proformas.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.univ2026.proformas.modelo.valor.Email;
import com.univ2026.proformas.modelo.valor.Monto;
import com.univ2026.proformas.modelo.valor.RUC;
import org.junit.jupiter.api.Test;

class AbstraccionTest {
    @Test
    void productoYClienteUsanModelosDeValor() {
        Producto producto = new Producto("P-001", "Mouse", "", new Monto("25.50"), 12.0, true);
        Cliente cliente =
                new Cliente(new RUC("1100001234"), "Ana", "Av. Loja", "0991234567", new Email("ana@correo.com"), true);

        assertInstanceOf(Monto.class, producto.getPrecio());
        assertInstanceOf(RUC.class, cliente.getIdentificacion());
        assertInstanceOf(Email.class, cliente.getEmail());
    }

    @Test
    void entidadesComparanPorIdentidadLogica() {
        Producto primero = new Producto("P-001", "Mouse", "", 10.0, 0.0, true);
        Producto segundo = new Producto("P-001", "Otro nombre", "", 99.0, 12.0, false);
        Cliente clienteUno = new Cliente("1100001234", "Ana");
        Cliente clienteDos = new Cliente("1100001234", "Otro nombre");
        Producto otroProducto = new Producto("P-002", "Mouse");
        Cliente otroCliente = new Cliente("1100009999", "Ana");

        assertEquals(primero, segundo);
        assertEquals(primero.hashCode(), segundo.hashCode());
        assertEquals(clienteUno, clienteDos);
        assertEquals(clienteUno.hashCode(), clienteDos.hashCode());
        assertNotEquals(primero, otroProducto);
        assertNotEquals(clienteUno, otroCliente);
        assertNotEquals(primero, clienteUno);
        assertNotEquals(primero, null);
    }

    @Test
    void registroImplementaContratoYRetornaLaMismaInstancia() {
        RegistroClientes registro = new RegistroClientesEnMemoria();
        Cliente cliente = new Cliente("1100001234", "Ana");

        registro.registrar(cliente);

        assertSame(cliente, registro.buscar(new RUC("1100001234")));
        assertSame(cliente, registro.buscar("1100001234"));
        assertNull(registro.buscar("1100009999"));
    }

    @Test
    void registroRechazaIdentificacionesDuplicadas() {
        RegistroClientes registro = new RegistroClientesEnMemoria();
        registro.registrar(new Cliente("1100001234", "Ana"));

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class, () -> registro.registrar(new Cliente("1100001234", "Otra persona")));

        assertEquals("ya existe un cliente con identificacion 1100001234", error.getMessage());
    }
}
