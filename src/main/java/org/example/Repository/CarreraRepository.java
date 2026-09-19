package org.example.Repository;


import org.example.Entity.Carrera;

public class CarreraRepository extends BaseJPARepository<Carrera, Integer> {

    private static CarreraRepository instance;

    private CarreraRepository() {
        super(Carrera.class, Integer.class);
    }

    public static CarreraRepository getInstance() {
        if (instance == null) {
            instance = new CarreraRepository();
        }
        return instance;
    }
}