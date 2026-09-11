package com.univ2026.proformas;

import com.univ2026.proformas.modelo.Cliente;
import com.univ2026.proformas.modelo.ItemProforma;
import com.univ2026.proformas.modelo.Producto;
import com.univ2026.proformas.modelo.Proforma;
import com.univ2026.proformas.modelo.RegistroClientes;
import com.univ2026.proformas.modelo.RegistroClientesEnMemoria;
import java.util.ArrayList;
import java.util.List;

/** Coordina las operaciones de una sesion sin conocer la consola. */
public class AplicacionProformas {
    private final List<Producto> productos = new ArrayList<>();
    private final RegistroClientes clientes = new RegistroClientesEnMemoria();

    public void registrarProducto(Producto producto) {
        if (producto == null) {
            throw new IllegalArgumentException("El producto no puede ser null");
        }
        productos.add(producto);
    }

    public void registrarCliente(Cliente cliente) {
        clientes.registrar(cliente);
    }

    public List<Producto> listarProductos() {
        return List.copyOf(productos);
    }

    public List<Cliente> listarClientes() {
        return clientes.listar();
    }

    public Producto darBajaProducto(String codigo) {
        String codigoBuscado = codigo == null ? "" : codigo.trim();
        Producto producto = productos.stream()
                .filter(item -> item.getCodigo().equals(codigoBuscado))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No existe el producto " + codigoBuscado));
        producto.setActivo(false);
        return producto;
    }

    public Cliente darBajaCliente(String identificacion) {
        Cliente cliente = clientes.buscar(identificacion);
        if (cliente == null) {
            throw new IllegalArgumentException("No existe el cliente " + identificacion);
        }
        cliente.setActivo(false);
        return cliente;
    }

    public Proforma crearProformaDemo() {
        Cliente cliente =
                listarClientes().stream().filter(Cliente::isActivo).findFirst().orElse(null);
        Producto producto =
                productos.stream().filter(Producto::isActivo).findFirst().orElse(null);
        if (cliente == null || producto == null) {
            throw new IllegalStateException("Se necesita al menos un cliente y un producto activos");
        }

        Proforma proforma = new Proforma("DEMO-001", cliente);
        proforma.agregarItem(new ItemProforma(producto, 1));
        return proforma;
    }
}
