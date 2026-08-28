package org.example.dao;

import org.example.entity.Factura;
import org.example.entity.Producto;

import java.util.List;

public interface FacturaDAO {
    public void insertFactura(Factura f);
    public void insertAll(List<Factura> facturas);
}
