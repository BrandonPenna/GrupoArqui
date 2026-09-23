package org.example.Services;

import org.example.dao.ProductoDAO;

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
