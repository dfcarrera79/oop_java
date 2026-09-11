package com.univ2026.proformas.aplicacion;

import com.univ2026.proformas.dominio.Estado;
import com.univ2026.proformas.dominio.cliente.Cliente;
import com.univ2026.proformas.dominio.cliente.RegistroClientes;
import com.univ2026.proformas.dominio.producto.CatalogoProductos;
import com.univ2026.proformas.dominio.producto.Producto;
import com.univ2026.proformas.dominio.proforma.ItemProforma;
import com.univ2026.proformas.dominio.proforma.Proforma;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Coordina las operaciones de una sesion sin conocer la consola. */
public class AplicacionProformas {
    private final CatalogoProductos productos;
    private final RegistroClientes clientes;

    /** Recibe contratos para mantener JDBC fuera de la capa de aplicacion. */
    public AplicacionProformas(CatalogoProductos productos, RegistroClientes clientes) {
        if (productos == null || clientes == null) {
            throw new IllegalArgumentException("Los catalogos no pueden ser null");
        }
        this.productos = productos;
        this.clientes = clientes;
    }

    public void registrarProducto(Producto producto) {
        productos.registrar(producto);
    }

    public void registrarCliente(Cliente cliente) {
        clientes.registrar(cliente);
    }

    public List<Producto> listarProductos() {
        return productos.listar();
    }

    public List<Cliente> listarClientes() {
        return clientes.listar();
    }

    public Producto darBajaProducto(String codigo) {
        return productos.cambiarEstado(codigo, Estado.INACTIVO);
    }

    public Cliente darBajaCliente(String identificacion) {
        return clientes.cambiarEstado(identificacion, Estado.INACTIVO);
    }

    public List<Producto> buscarProductos(String texto) {
        return productos.buscar(texto);
    }

    public List<Cliente> buscarClientes(String texto) {
        return clientes.buscar(texto);
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

    public Proforma crearProformaDemo() {
        Cliente cliente = listarClientes().stream()
                .filter(item -> item.getEstado() == Estado.ACTIVO)
                .findFirst()
                .orElse(null);
        Producto producto = listarProductos().stream()
                .filter(item -> item.getEstado() == Estado.ACTIVO)
                .findFirst()
                .orElse(null);
        if (cliente == null || producto == null) {
            throw new IllegalStateException("Se necesita al menos un cliente y un producto activos");
        }

        Proforma proforma = new Proforma("DEMO-001", cliente);
        proforma.agregarItem(new ItemProforma(producto, 1, cliente.getTipo()));
        return proforma;
    }
}
