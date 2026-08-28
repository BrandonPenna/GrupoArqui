package org.example.dao;

import org.example.entity.Producto;

import java.util.List;

public interface ProductoDAO {

    public void insertProducto(Producto p);
    public void insertAll(List<Producto> productos);
}
