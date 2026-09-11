package com.univ2026.proformas.modelo.valor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ModelosValorTest {
    @Test
    void emailNormalizaYComparaPorValor() {
        Email primero = new Email("  ana@correo.com  ");
        Email segundo = new Email("ana@correo.com");

        assertEquals("ana@correo.com", primero.valor());
        assertEquals("ana@correo.com", primero.toString());
        assertEquals(primero, segundo);
        assertEquals(primero.hashCode(), segundo.hashCode());
        assertNotEquals(primero, new Email("otro@correo.com"));
    }

    @Test
    void emailPermiteValorVacio() {
        assertEquals("", new Email("   ").valor());
    }

    @ParameterizedTest
    @ValueSource(strings = {"correo-sin-arroba", "ana@correo", "@correo.com"})
    void emailRechazaFormatosInvalidos(String valor) {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> new Email(valor));

        assertEquals("El email no tiene un formato valido", error.getMessage());
    }

    @Test
    void rucAceptaIdentificacionesDeDiezOTreceDigitos() {
        assertEquals("1100001234", new RUC(" 1100001234 ").valor());
        assertEquals("1790012345001", new RUC("1790012345001").toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"110000123", "11000012345", "179001234500", "ABC0012345"})
    void rucRechazaFormatosInvalidos(String valor) {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> new RUC(valor));

        assertEquals("La identificacion debe contener 10 o 13 digitos", error.getMessage());
    }

    @Test
    void montoRedondeaHalfUpYComparaElValorNormalizado() {
        Monto monto = new Monto("10.125");

        assertEquals(new BigDecimal("10.13"), monto.valor());
        assertEquals("10.13", monto.toString());
        assertEquals(monto, new Monto("10.130"));
        assertEquals(monto.hashCode(), new Monto(10.13).hashCode());
    }

    @Test
    void montoRechazaValoresInvalidosNegativosYNoFinitos() {
        assertEquals(
                "El monto debe ser un numero valido",
                assertThrows(IllegalArgumentException.class, () -> new Monto("NaN"))
                        .getMessage());
        assertEquals(
                "El monto no puede ser negativo",
                assertThrows(IllegalArgumentException.class, () -> new Monto("-1"))
                        .getMessage());
        assertEquals(
                "El monto debe ser finito",
                assertThrows(IllegalArgumentException.class, () -> new Monto(Double.POSITIVE_INFINITY))
                        .getMessage());
    }
}
