package madstodolist.model;

import madstodolist.repository.EquipoRepository;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "equipos")
public class Equipo {

    private String nombre;

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
    public void setId(Long id){
        this.id = id;
    }

    public boolean equals(Object o){
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        Equipo equipo = (Equipo) o;
        if(id != null && equipo.id != null){
            return Objects.equals(id, equipo.id);
        }
        return nombre.equals(equipo.nombre);
    }

    public int hashCode(){
        return Objects.hash(nombre);
    }
}
