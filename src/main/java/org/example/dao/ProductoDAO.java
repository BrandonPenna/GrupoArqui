package org.example.dao;

import org.example.entity.Producto;
import org.example.entity.ProductoRecaudado;

import java.util.List;

public interface ProductoDAO {

    public void insertProducto(Producto p);
    public void insertAll(List<Producto> productos);
    ProductoRecaudado getProductoMasRecaudado();

}
