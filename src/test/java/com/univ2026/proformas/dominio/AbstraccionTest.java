package com.univ2026.proformas.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.univ2026.proformas.dominio.cliente.Cliente;
import com.univ2026.proformas.dominio.cliente.TipoCliente;
import com.univ2026.proformas.dominio.producto.Producto;
import com.univ2026.proformas.dominio.valor.Email;
import com.univ2026.proformas.dominio.valor.Monto;
import com.univ2026.proformas.dominio.valor.RUC;
import org.junit.jupiter.api.Test;

class AbstraccionTest {
    @Test
    void productoYClienteUsanModelosDeValor() {
        Producto producto = new Producto("P-001", "Mouse", "", new Monto("25.50"), 15.0, Estado.ACTIVO, null);
        Cliente cliente = new Cliente(
                new RUC("1100001234"),
                "Ana",
                "Av. Loja",
                "0991234567",
                new Email("ana@correo.com"),
                TipoCliente.PUBLICO,
                Estado.ACTIVO);

        assertInstanceOf(Monto.class, producto.getPrecio());
        assertInstanceOf(RUC.class, cliente.getIdentificacion());
        assertInstanceOf(Email.class, cliente.getEmail());
    }

    @Test
    void entidadesComparanPorIdentidadLogica() {
        Producto primero = new Producto("P-001", "Mouse", "", 10.0, 0.0, Estado.ACTIVO, null);
        Producto segundo = new Producto("P-001", "Otro nombre", "", 99.0, 15.0, Estado.INACTIVO, null);
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
}
