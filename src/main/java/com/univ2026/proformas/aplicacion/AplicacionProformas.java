package com.univ2026.proformas.aplicacion;

import com.univ2026.proformas.dominio.Estado;
import com.univ2026.proformas.dominio.cliente.Cliente;
import com.univ2026.proformas.dominio.cliente.RegistroClientes;
import com.univ2026.proformas.dominio.pago.CuentaPago;
import com.univ2026.proformas.dominio.pago.RepositorioCuentasPago;
import com.univ2026.proformas.dominio.producto.CatalogoProductos;
import com.univ2026.proformas.dominio.producto.Producto;
import com.univ2026.proformas.dominio.producto.Talla;
import com.univ2026.proformas.dominio.proforma.ItemProforma;
import com.univ2026.proformas.dominio.proforma.Proforma;
import com.univ2026.proformas.dominio.proforma.RepositorioProformas;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Fachada de casos de uso, independiente de consola y JDBC. */
public class AplicacionProformas {
    private final CatalogoProductos productos;
    private final RegistroClientes clientes;
    private final RepositorioCuentasPago cuentas;
    private final RepositorioProformas proformas;

    public AplicacionProformas(
            CatalogoProductos productos,
            RegistroClientes clientes,
            RepositorioCuentasPago cuentas,
            RepositorioProformas proformas) {
        if (productos == null || clientes == null || cuentas == null || proformas == null) {
            throw new IllegalArgumentException("Los repositorios no pueden ser null");
        }
        this.productos = productos;
        this.clientes = clientes;
        this.cuentas = cuentas;
        this.proformas = proformas;
    }

    public void registrarProducto(Producto producto) {
        productos.registrar(producto);
    }

    public Producto actualizarProducto(Producto producto) {
        return productos.actualizar(producto);
    }

    public void eliminarProducto(String codigo) {
        productos.eliminar(codigo);
    }

    public Producto buscarProductoPorCodigo(String codigo) {
        return productos.buscarPorCodigo(codigo);
    }

    public List<Producto> listarProductos() {
        return productos.listar();
    }

    public List<Producto> buscarProductos(String texto) {
        return productos.buscar(texto);
    }

    public Producto darBajaProducto(String codigo) {
        return productos.cambiarEstado(codigo, Estado.INACTIVO);
    }

    public void registrarCliente(Cliente cliente) {
        clientes.registrar(cliente);
    }

    public Cliente actualizarCliente(Cliente cliente) {
        return clientes.actualizar(cliente);
    }

    public void eliminarCliente(String identificacion) {
        clientes.eliminar(identificacion);
    }

    public Cliente buscarClientePorIdentificacion(String identificacion) {
        return clientes.buscarPorIdentificacion(identificacion);
    }

    public List<Cliente> listarClientes() {
        return clientes.listar();
    }

    public List<Cliente> buscarClientes(String texto) {
        return clientes.buscar(texto);
    }

    public Cliente darBajaCliente(String identificacion) {
        return clientes.cambiarEstado(identificacion, Estado.INACTIVO);
    }

    public CuentaPago registrarCuenta(CuentaPago cuenta) {
        return cuentas.registrar(cuenta);
    }

    public CuentaPago actualizarCuenta(CuentaPago cuenta) {
        return cuentas.actualizar(cuenta);
    }

    public void eliminarCuenta(long id) {
        cuentas.eliminar(id);
    }

    public List<CuentaPago> listarCuentas() {
        return cuentas.listar();
    }

    public List<CuentaPago> buscarCuentas(String texto) {
        return cuentas.buscar(texto);
    }

    public Proforma crearProforma(
            String identificacionCliente, LocalDate fecha, String observaciones, String instruccionesPago) {
        Cliente cliente = clientes.buscarPorIdentificacion(identificacionCliente);
        if (cliente == null) {
            throw new IllegalArgumentException("No existe el cliente " + identificacionCliente);
        }
        if (cliente.getEstado() != Estado.ACTIVO) {
            throw new IllegalArgumentException("El cliente debe estar activo");
        }
        return new Proforma(cliente, fecha, observaciones, instruccionesPago);
    }

    public ItemProforma agregarItem(Proforma proforma, String codigoProducto, int cantidad) {
        return agregarItem(proforma, codigoProducto, cantidad, null);
    }

    /** Agrega un item con talla elegida; null conserva la talla configurada en el producto. */
    public ItemProforma agregarItem(Proforma proforma, String codigoProducto, int cantidad, Talla talla) {
        if (proforma == null || proforma.getCliente() == null) {
            throw new IllegalArgumentException("Se requiere un cliente antes de construir items");
        }
        Producto producto = productos.buscarPorCodigo(codigoProducto);
        if (producto == null) {
            throw new IllegalArgumentException("No existe el producto " + codigoProducto);
        }
        ItemProforma item =
                new ItemProforma(producto, cantidad, proforma.getCliente().getTipo(), talla);
        proforma.agregarItem(item);
        return item;
    }

    public Proforma guardarProforma(Proforma proforma) {
        return proformas.guardar(proforma);
    }

    public List<Proforma> listarProformas() {
        return proformas.listar();
    }

    public List<Proforma> buscarProformas(String texto) {
        return proformas.buscar(texto);
    }

    public void eliminarProforma(String numero) {
        proformas.eliminar(numero);
    }

    public Map<String, Producto> indexarProductosPorCodigo() {
        return productos.indexarPorCodigo();
    }

    public Set<String> listarCodigosProducto() {
        return productos.listarCodigos();
    }

    public Map<String, Cliente> indexarClientesPorIdentificacion() {
        return clientes.indexarPorIdentificacion();
    }

    public Set<String> listarIdentificacionesCliente() {
        return clientes.listarIdentificaciones();
    }
}
