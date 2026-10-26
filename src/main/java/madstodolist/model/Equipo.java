package madstodolist.model;

import madstodolist.repository.EquipoRepository;

import javax.persistence.*;
import java.util.Objects;
import java.util.Set;
import java.util.HashSet;
import java.io.Serializable;

@Entity
@Table(name = "equipos")
public class Equipo implements Serializable {

    private String nombre;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Declaramos el tipo de recuperación como LAZY.
    // No haría falta porque es el tipo por defecto en una
    // relación a muchos.
    // Al recuperar un equipo NO SE RECUPERA AUTOMÁTICAMENTE
    // la lista de usuarios. Sólo se recupera cuando se accede al
    // atributo 'usuarios'; entonces se genera una query en la
    // BD que devuelve todos los usuarios del equipo y rellena el
    // atributo.

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "equipo_usuario",
            joinColumns = { @JoinColumn(name = "fk_equipo") },
            inverseJoinColumns = {@JoinColumn(name = "fk_usuario")})
    Set<Usuario> usuarios = new HashSet<>();

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

    public Set<Usuario> getUsuarios(){
        return usuarios;
    }

    public void addUsuario(Usuario usuario){
        this.getUsuarios().add(usuario);
        usuario.getEquipos().add(this);
    }

    public void removeUsuario(Usuario usuario){
        this.getUsuarios().remove(usuario);
        usuario.getEquipos().remove(this);
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
