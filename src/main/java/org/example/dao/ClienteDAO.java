package org.example.dao;

import org.example.entity.Cliente;

import java.util.List;

public interface ClienteDAO {
    public void insertCliente(Cliente c);
    public void insertAll(List<Cliente> clientes);
}
