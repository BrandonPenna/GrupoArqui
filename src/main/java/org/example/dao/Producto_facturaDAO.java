package org.example.dao;

import org.example.entity.Factura_producto;

import java.util.List;

public interface Producto_facturaDAO {
    public void insertProductoFactura(Factura_producto f);
    public void insertAll(List<Factura_producto> producto_facturas);
}
