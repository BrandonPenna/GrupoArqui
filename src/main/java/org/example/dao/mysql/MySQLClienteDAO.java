package org.example.dao.mysql;

import org.example.Factory.MySQLDAOFactory;
import org.example.dao.ClienteDAO;
import org.example.entity.Cliente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class MySQLClienteDAO implements ClienteDAO {

    @Override
    public void insertCliente(Cliente cliente) {

        String insert = "INSERT INTO cliente (idCliente, nombre, email) VALUES (?, ?, ?)";

        Connection conn = MySQLDAOFactory.createConnection();

        if (conn == null) return;

        try {
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(insert)) {

                ps.setInt(1, cliente.getIdCliente());
                ps.setString(2, cliente.getNombre());
                ps.setString(3, cliente.getEmail());

                ps.executeUpdate();

                conn.commit();

            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
            }

        } catch (SQLException e) {
            e.printStackTrace();

        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void insertAll(List<Cliente> clientes) {

        String insert = "INSERT INTO cliente (idCliente, nombre, email) VALUES (?, ?, ?)";

        Connection conn = MySQLDAOFactory.createConnection();

        if (conn == null) return;

        try {
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(insert)) {

                for (Cliente cliente : clientes) {

                    ps.setInt(1, cliente.getIdCliente());
                    ps.setString(2, cliente.getNombre());
                    ps.setString(3, cliente.getEmail());

                    ps.addBatch();
                }

                ps.executeBatch();

                conn.commit();

            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
            }

        } catch (SQLException e) {
            e.printStackTrace();

        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}