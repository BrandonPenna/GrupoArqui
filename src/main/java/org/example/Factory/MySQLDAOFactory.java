package org.example.Factory;

import org.example.dao.FacturaDAO;
import org.example.dao.ProductoDAO;
import org.example.dao.Producto_facturaDAO;
import org.example.dao.mysql.MySQLClienteDAO;
import org.example.dao.ClienteDAO;
import org.example.dao.mysql.MySQLFacturaDAO;
import org.example.dao.mysql.MySQLProductoDAO;
import org.example.dao.mysql.MySQLProducto_facturaDAO;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLDAOFactory extends DAOFactory {
    public static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    public static final String DBURL = "jdbc:mysql://localhost:3306/practico_db?createDatabaseIfNotExist=true";
    public static final String USER = "root";
    public static final String PASS = "";

    private static MySQLDAOFactory instance = null;



    public static synchronized MySQLDAOFactory getInstance() {
        if (instance == null) {
            instance = new MySQLDAOFactory();
        }
        return instance;
    }

    public static Connection createConnection() {
        try {
            Class.forName(DRIVER).getDeclaredConstructor().newInstance();
            return DriverManager.getConnection(DBURL, USER, PASS);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException | ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ClienteDAO getClienteDAO() {
        return new MySQLClienteDAO();
    }
    public FacturaDAO getFacturaDAO() {
        return new MySQLFacturaDAO();
    }

    public ProductoDAO getProductoDAO() {
        return new MySQLProductoDAO();
    }

    public Producto_facturaDAO getProducto_facturaDAO() {
        return new MySQLProducto_facturaDAO();
    }
}
