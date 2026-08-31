
package org.example.entity;

import lombok.Data;

@Data
public class ProductoRecaudado{
    private int idProducto;
    private String nombre;
    private float valor;
    private int recaudadoTotal;

    public ProductoRecaudado(int idProducto, String nombre, float valor,int recaudadoTotal) {
        this.idProducto = idProducto;
        this.nombre = nombre;
        this.valor = valor;
        this.recaudadoTotal = recaudadoTotal;
    }
}
