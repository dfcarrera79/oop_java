package com.univ2026.proformas.modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.univ2026.proformas.modelo.valor.Email;
import com.univ2026.proformas.modelo.valor.RUC;
import org.junit.jupiter.api.Test;

class ClienteTest {
    @Test
    void creaClienteConValoresValidos() {
        Cliente cliente = new Cliente("1100001234", "Ana Torres", "Av. Loja", "0991234567", "ana@correo.com", true);

        assertEquals(new RUC("1100001234"), cliente.getIdentificacion());
        assertEquals("Ana Torres", cliente.getNombre());
        assertEquals("Av. Loja", cliente.getDireccion());
        assertEquals("0991234567", cliente.getTelefono());
        assertEquals(new Email("ana@correo.com"), cliente.getEmail());
        assertTrue(cliente.isActivo());
    }

    @Test
    void usaContactoVacioPorDefectoYNormalizaTextos() {
        Cliente cliente = new Cliente("  1100001235  ", "  Luis  ");

        assertEquals(new RUC("1100001235"), cliente.getIdentificacion());
        assertEquals("Luis", cliente.getNombre());
        assertEquals("", cliente.getDireccion());
        assertEquals("", cliente.getTelefono());
        assertEquals(new Email(""), cliente.getEmail());
        assertTrue(cliente.isActivo());
    }

    @Test
    void permiteCambiosValidos() {
        Cliente cliente = new Cliente("1100001236", "Luis");

        cliente.setEmail("  luis@correo.com  ");
        cliente.setActivo(false);

        assertEquals(new Email("luis@correo.com"), cliente.getEmail());
        assertFalse(cliente.isActivo());
    }

    @Test
    void rechazaIdentificacionYNombreVacios() {
        IllegalArgumentException identificacionError =
                assertThrows(IllegalArgumentException.class, () -> new Cliente("", "Luis"));
        IllegalArgumentException nombreError =
                assertThrows(IllegalArgumentException.class, () -> new Cliente("1100001237", " "));

        assertEquals("La identificacion no puede estar vacia", identificacionError.getMessage());
        assertEquals("El nombre no puede estar vacio", nombreError.getMessage());
    }

    @Test
    void permiteEmailVacio() {
        Cliente cliente = new Cliente("1100001238", "Luis");

        cliente.setEmail("   ");

        assertEquals(new Email(""), cliente.getEmail());
    }

    @Test
    void rechazaEmailMalFormado() {
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> new Cliente("1100001239", "Luis", "", "", "correo-sin-arroba", true));

        assertEquals("El email no tiene un formato valido", error.getMessage());
    }
}
