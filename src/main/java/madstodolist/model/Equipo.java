package madstodolist.model;

import madstodolist.repository.EquipoRepository;

import javax.persistence.*;

@Entity
@Table(name = "equipos")
public class Equipo {

    String nombre;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Constructor vacío necesario para JPA/Hibernate
    public Equipo() {}

    public Equipo(String nombre){
        this.nombre = nombre;
    }

    public String getNombre(){
        return nombre;
    }
    public Long getId(){
        return id;
    }
}
