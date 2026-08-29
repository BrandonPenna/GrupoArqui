package org.example.Services;

import org.example.Factory.DAOFactory;
import org.example.dao.ClienteDAO;
import org.example.entity.Cliente;

import java.util.Map;

public class ClienteServices {
    private final ClienteDAO clienteDAO;

    public ClienteServices() {
        DAOFactory daoFactory = DAOFactory.getDAOFactory(1);
        this.clienteDAO = daoFactory.getClienteDAO();
    }

    public Map<Cliente, Double> getClientesOrdenadosPorFacturacion() {
        return clienteDAO.getClientesOrdenadosPorFacturacion();
    }
}