package com.univ2026.proformas.dominio.producto;

/** Marca los atributos opcionales que especializan un producto por composicion. */
public sealed interface AtributosProducto permits AtributosFisicos, AtributosDigitales {}
