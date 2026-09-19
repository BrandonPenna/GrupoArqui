package org.example.Entity;

public class Estudiante {
    private int idCliente;
    private String nombre;
    private String email;
    public Estudiante(int idCliente, String nombre, String email) {
        this.setIdCliente(idCliente);
        this.setNombre(nombre);
        this.setEmail(email);
    }

    public int getIdCliente() {
        return idCliente;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
