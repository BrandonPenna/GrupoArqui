package org.example.Factory;

import org.example.dao.ClienteDAO;
import org.example.dao.FacturaDAO;
import org.example.dao.ProductoDAO;
import org.example.dao.Producto_facturaDAO;

public abstract class DAOFactory {
    public static final int MYSQL_JDBC = 1;

    public abstract ClienteDAO getClienteDAO();
    public abstract FacturaDAO getFacturaDAO();
    public abstract ProductoDAO getProductoDAO();
    public abstract Producto_facturaDAO getProducto_facturaDAO();

    public static DAOFactory getDAOFactory(int whichFactory){
        switch (whichFactory){
            case MYSQL_JDBC: return MySQLDAOFactory.getInstance();
            default: return null;
        }
    }
}
