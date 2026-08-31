package org.example.Services;

import org.example.Factory.DAOFactory;
import org.example.dao.ProductoDAO;
import org.example.entity.Producto;
import org.example.entity.ProductoRecaudado;

public class ProductoServices {
    private final ProductoDAO productoDAO;

    public ProductoServices() {
        DAOFactory daoFactory = DAOFactory.getDAOFactory(1);
        this.productoDAO = daoFactory.getProductoDAO();
    }
    public ProductoRecaudado getProductoMasRecaudado() {
        return productoDAO.getProductoMasRecaudado();
    }
}
