package org.example.Factory;

import org.example.dao.mysql.MySQLClienteDAO;
import org.example.dao.ClienteDAO;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLDAOFactory extends DAOFactory {
    public static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    public static final String DBURL = "jdbc:mysql://localhost:3306/practico_db";
    public static final String USER = "root";
    public static final String PASS = "";

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
}
