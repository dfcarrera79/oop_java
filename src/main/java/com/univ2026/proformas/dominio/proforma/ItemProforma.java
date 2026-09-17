package com.univ2026.proformas.dominio.proforma;

import com.univ2026.proformas.dominio.Estado;
import com.univ2026.proformas.dominio.cliente.TipoCliente;
import com.univ2026.proformas.dominio.producto.AtributosDigitales;
import com.univ2026.proformas.dominio.producto.AtributosFisicos;
import com.univ2026.proformas.dominio.producto.Producto;
import com.univ2026.proformas.dominio.producto.Talla;
import com.univ2026.proformas.dominio.valor.Monto;
import java.math.BigDecimal;

/** Linea de proforma cuyos datos comerciales son un snapshot historico. */
public class ItemProforma {
    private Producto producto;
    private int cantidad;
    private BigDecimal descuentoPct;
    private TipoCliente tipoCliente;
    private String tipoProducto;
    private Talla talla;

    public ItemProforma(Producto producto, int cantidad) {
        this(producto, cantidad, BigDecimal.ZERO);
    }

    public ItemProforma(Producto producto, int cantidad, TipoCliente tipoCliente) {
        this(producto, cantidad, tipoCliente, null);
    }

    /** Crea una linea usando la talla elegida o la talla fisica del producto cuando es null. */
    public ItemProforma(Producto producto, int cantidad, TipoCliente tipoCliente, Talla talla) {
        this(producto, cantidad, tipoCliente == null ? BigDecimal.ZERO : tipoCliente.getDescuento());
        this.tipoCliente = tipoCliente;
        if (talla != null) {
            setTalla(talla);
        }
    }

    public ItemProforma(Producto producto, int cantidad, BigDecimal descuentoPct) {
        setProducto(producto);
        setCantidad(cantidad);
        setDescuentoPct(descuentoPct);
    }

    /** Reconstruye un snapshot sin consultar el catalogo actual. */
    public static ItemProforma restaurar(
            String codigo,
            String nombre,
            String descripcion,
            String tipoProducto,
            String talla,
            TipoCliente tipoCliente,
            BigDecimal precioBase,
            int ivaPct,
            int cantidad,
            BigDecimal descuentoPct) {
        ItemProforma item = new ItemProforma(
                new Producto(codigo, nombre, descripcion, new Monto(precioBase), ivaPct, Estado.ACTIVO, null),
                cantidad,
                descuentoPct);
        item.tipoProducto = textoOpcional(tipoProducto);
        item.talla = talla == null || talla.isBlank() ? null : Talla.valueOf(talla);
        item.tipoCliente = tipoCliente;
        return item;
    }

    public Producto getProducto() {
        return new Producto(
                producto.getCodigo(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getPrecio(),
                producto.getIvaPct(),
                Estado.ACTIVO,
                producto.getExtras());
    }

    public void setProducto(Producto producto) {
        if (producto == null) {
            throw new IllegalArgumentException("El producto no puede ser null");
        }
        if (producto.getEstado() != Estado.ACTIVO) {
            throw new IllegalArgumentException("No se puede agregar un producto inactivo");
        }
        this.producto = new Producto(
                producto.getCodigo(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getPrecio(),
                producto.getIvaPct(),
                Estado.ACTIVO,
                producto.getExtras());
        if (producto.getExtras() instanceof AtributosFisicos fisicos) {
            tipoProducto = "FISICO";
            talla = fisicos.talla();
        } else if (producto.getExtras() instanceof AtributosDigitales) {
            tipoProducto = "DIGITAL";
            talla = null;
        } else {
            tipoProducto = "GENERAL";
            talla = null;
        }
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser positiva");
        }
        this.cantidad = cantidad;
    }

    public BigDecimal getDescuentoPct() {
        return descuentoPct;
    }

    public TipoCliente getTipoCliente() {
        return tipoCliente;
    }

    public void setTipoCliente(TipoCliente tipoCliente) {
        this.tipoCliente = tipoCliente;
        setDescuentoPct(tipoCliente == null ? BigDecimal.ZERO : tipoCliente.getDescuento());
    }

    public void setDescuentoPct(BigDecimal descuentoPct) {
        if (descuentoPct == null || descuentoPct.signum() < 0 || descuentoPct.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("El descuento debe estar entre 0 y 100");
        }
        this.descuentoPct = descuentoPct.stripTrailingZeros();
    }

    public String getTipoProducto() {
        return tipoProducto;
    }

    public Talla getTalla() {
        return talla;
    }

    public void setTalla(Talla talla) {
        this.talla = talla;
    }

    public BigDecimal calcularDescuento() {
        BigDecimal bruto = producto.getPrecio().valor().multiply(BigDecimal.valueOf(cantidad));
        return Monto.normalizar(bruto.multiply(descuentoPct).movePointLeft(2));
    }

    public BigDecimal calcularSubtotal() {
        BigDecimal bruto = producto.getPrecio().valor().multiply(BigDecimal.valueOf(cantidad));
        return Monto.normalizar(bruto.subtract(calcularDescuento()));
    }

    public BigDecimal calcularImpuesto() {
        return Monto.normalizar(calcularSubtotal()
                .multiply(BigDecimal.valueOf(producto.getIvaPct()))
                .movePointLeft(2));
    }

    public BigDecimal calcularTotal() {
        return Monto.normalizar(calcularSubtotal().add(calcularImpuesto()));
    }

    private static String textoOpcional(String valor) {
        return valor == null ? "" : valor.trim();
    }
}
